package me.michiyo.openfm.client.audio;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RadioStreamManager {

    private static final class Desired {
        final BlockPos pos;
        volatile String url = "";
        volatile int vol = 0;

        // The actual sound source position (we'll dynamically shift this to nearest emitter)
        volatile Vec3 worldPos = Vec3.ZERO;

        // smoothing to avoid snapping between speakers
        volatile Vec3 lastWorldPos = Vec3.ZERO;

        // simple restart backoff
        volatile long nextRestartAtMs = 0;

        Desired(BlockPos pos) {
            this.pos = pos.immutable();
        }
    }

    private static final Map<BlockPos, Desired> DESIRED = new ConcurrentHashMap<>();
    private static final Map<BlockPos, RadioStreamPlayer> PLAYERS = new ConcurrentHashMap<>();

    // ICY "Now Playing" per radio position (client-side)
    private static final Map<BlockPos, String> NOW_PLAYING = new ConcurrentHashMap<>();

    /**
     * Emitters per radio position:
     *   key   = radio block pos
     *   value = map of emitter pos -> worldPos (usually block center)
     *
     * This is CLIENT ONLY and purely affects where the existing single decoded stream is positioned.
     */
    private static final Map<BlockPos, Map<BlockPos, Vec3>> RADIO_EMITTERS = new ConcurrentHashMap<>();

    private static Level lastLevel = null;

    private RadioStreamManager() {}

    public static String getNowPlaying(BlockPos pos) {
        if (pos == null) return null;
        return NOW_PLAYING.get(pos);
    }

    /**
     * Register/update an emitter for a given radio position.
     * Call this from SpeakerBlockEntity (client-side) whenever it loads/updates.
     */
    public static void setEmitterForRadio(BlockPos radioPos, BlockPos emitterPos, Vec3 worldPos) {
        if (radioPos == null || emitterPos == null) return;

        BlockPos r = radioPos.immutable();
        BlockPos e = emitterPos.immutable();
        Vec3 wp = (worldPos == null) ? Vec3.atCenterOf(e) : worldPos;
        System.out.println("[OpenFM][Emitter] radio=" + radioPos + " + emitter=" + emitterPos);
        RADIO_EMITTERS.computeIfAbsent(r, k -> new ConcurrentHashMap<>()).put(e, wp);
    }

    /**
     * Remove an emitter for a given radio position.
     * Call this from SpeakerBlockEntity (client-side) when the speaker is removed/unlinked.
     */
    public static void clearEmitterForRadio(BlockPos radioPos, BlockPos emitterPos) {
        if (radioPos == null || emitterPos == null) return;

        Map<BlockPos, Vec3> m = RADIO_EMITTERS.get(radioPos);
        if (m != null) {
            m.remove(emitterPos);
            if (m.isEmpty()) RADIO_EMITTERS.remove(radioPos);
        }
    }

    /**
     * Optional: remove ALL emitters for a radio (e.g. when radio is removed).
     */
    public static void clearAllEmittersForRadio(BlockPos radioPos) {
        if (radioPos == null) return;
        RADIO_EMITTERS.remove(radioPos);
    }

    public static void clientTick() {
        Minecraft mc = Minecraft.getInstance();

        long now = System.currentTimeMillis();

        if (mc.level == null || mc.player == null) {
            stopAll();
            lastLevel = mc.level;
            return;
        }

        if (lastLevel != null && lastLevel != mc.level) {
            stopAll();
        }
        lastLevel = mc.level;

        Vec3 listener = mc.gameRenderer.getMainCamera().getPosition();

        // Drive players from desired state
        for (var it = DESIRED.entrySet().iterator(); it.hasNext();) {
            var e = it.next();
            BlockPos pos = e.getKey();
            Desired d = e.getValue();

            // If chunk not loaded, stop and forget desired state
            if (!mc.level.hasChunkAt(pos)) {
                stopPlayer(pos);
                it.remove();
                NOW_PLAYING.remove(pos);
                clearAllEmittersForRadio(pos); // keep this
                continue;
            }

            boolean shouldPlay = d.vol > 0 && d.url != null && !d.url.isEmpty();

            if (!shouldPlay) {
                stopPlayer(pos);
                it.remove();
                NOW_PLAYING.remove(pos);
                // Do NOT clear emitters here
                continue;
            }

            // Pick nearest emitter for this radio (radio itself is always an implicit emitter)
            Vec3 targetPos = blockCenter(pos);

            Map<BlockPos, Vec3> emitters = RADIO_EMITTERS.get(pos);
            if (emitters != null && !emitters.isEmpty()) {
                double bestDist2 = targetPos.distanceToSqr(listener);
                Vec3 bestPos = targetPos;
                //System.out.println("[OpenFM][EmitterPick] radio=" + pos + " usingEmitter=" + (targetPos == null ? "null" : targetPos));

                for (var ee : emitters.entrySet()) {
                    BlockPos epos = ee.getKey();

                    // If emitter chunk not loaded, drop it (prevents stale emitters keeping "range")
                    if (!mc.level.hasChunkAt(epos)) {
                        emitters.remove(epos);
                        continue;
                    }

                    Vec3 wp = ee.getValue();
                    if (wp == null) continue;

                    double d2 = wp.distanceToSqr(listener);
                    if (d2 < bestDist2) {
                        bestDist2 = d2;
                        bestPos = wp;
                    }
                }

                targetPos = bestPos;

                if (emitters.isEmpty()) {
                    RADIO_EMITTERS.remove(pos);
                }
            }

            // Smooth source position a bit to avoid snapping between speakers
            if (d.lastWorldPos == null || d.lastWorldPos.equals(Vec3.ZERO)) {
                d.worldPos = targetPos;
                d.lastWorldPos = targetPos;
            } else {
                Vec3 smooth = lerp(d.lastWorldPos, targetPos, 0.35);
                d.worldPos = smooth;
                d.lastWorldPos = smooth;
            }

            RadioStreamPlayer p = PLAYERS.get(pos);

            // If no player, or player stopped, restart with a small backoff
            if (p == null || p.isStopped()) {
                if (now >= d.nextRestartAtMs) {
                    stopPlayer(pos);

                    System.out.println("[OpenFM][Player] start at " + pos + " url=" + d.url + " vol=" + d.vol);

                    // Create with metadata callback
                    RadioStreamPlayer np = new RadioStreamPlayer(
                            d.url,
                            d.worldPos,
                            d.vol,
                            title -> {
                                if (title == null) return;
                                String t = title.trim();
                                if (t.isEmpty()) return;
                                NOW_PLAYING.put(pos, t);
                            }
                    );

                    PLAYERS.put(pos, np);
                    np.start();

                    d.nextRestartAtMs = now + 2000;
                }
            } else {
                // keep position/volume current without restarting
                p.setWorldPos(d.worldPos);
                p.setVolume(d.vol);
                p.tick(listener);
            }
        }

        // Clean out any players that no longer have desired state
        for (var it = PLAYERS.entrySet().iterator(); it.hasNext();) {
            var e = it.next();
            BlockPos pos = e.getKey();
            RadioStreamPlayer p = e.getValue();

            if (!DESIRED.containsKey(pos) || p.isStopped()) {
                p.stop();
                it.remove();
                NOW_PLAYING.remove(pos);
                // Do NOT clear emitters here
            }
        }
    }

    public static void stopAll() {
        for (RadioStreamPlayer p : PLAYERS.values()) {
            p.stop();
        }
        PLAYERS.clear();
        DESIRED.clear();
        NOW_PLAYING.clear();
        RADIO_EMITTERS.clear();
        lastLevel = null;
    }

    public static void applyState(BlockPos pos, String url, int volume0to10) {
        String safeUrl = (url == null) ? "" : url.trim();
        int vol = Math.max(0, Math.min(10, volume0to10));

        if (safeUrl.isEmpty() || vol == 0) {
            DESIRED.remove(pos);
            stopPlayer(pos);
            NOW_PLAYING.remove(pos);
            // Do NOT clear emitters here; speakers still exist and should keep extending range
            return;
        }

        Desired d = DESIRED.computeIfAbsent(pos, Desired::new);
        d.url = safeUrl;
        d.vol = vol;

        // Default source position is the radio itself (will be overridden in tick if emitters exist)
        d.worldPos = blockCenter(pos);

        //System.out.println("[OpenFM][Desired] pos=" + pos + " url=" + safeUrl + " vol=" + vol);

        // If URL changed, force restart next tick + clear now playing
        RadioStreamPlayer existing = PLAYERS.get(pos);
        if (existing != null && !existing.matches(safeUrl)) {
            stopPlayer(pos);
            NOW_PLAYING.remove(pos);
            d.nextRestartAtMs = 0;
        }
    }

    private static void stopPlayer(BlockPos pos) {
        RadioStreamPlayer p = PLAYERS.remove(pos);
        if (p != null) p.stop();
    }

    private static Vec3 blockCenter(BlockPos pos) {
        return new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    private static Vec3 lerp(Vec3 a, Vec3 b, double t) {
        return new Vec3(
                a.x + (b.x - a.x) * t,
                a.y + (b.y - a.y) * t,
                a.z + (b.z - a.z) * t
        );
    }

    // Keep these for existing call sites (speaker BE used them earlier)
    public static void setDesired(BlockPos pos, String url, int vol, Vec3 worldPos) {
        if (pos == null) return;
        Desired d = DESIRED.computeIfAbsent(pos.immutable(), Desired::new);
        d.url = url == null ? "" : url;
        d.vol = vol;
        d.worldPos = worldPos == null ? Vec3.atCenterOf(pos) : worldPos;

        // reset smoothing when caller explicitly sets worldPos
        d.lastWorldPos = d.worldPos;
    }

    public static void clearDesired(BlockPos pos) {
        if (pos == null) return;
        Desired d = DESIRED.get(pos);
        if (d != null) {
            d.url = "";
            d.vol = 0;
        }
    }
}
