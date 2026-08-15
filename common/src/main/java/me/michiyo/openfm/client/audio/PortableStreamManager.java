package me.michiyo.openfm.client.audio;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PortableStreamManager {
    private static final class Desired {
        final UUID playerId;
        volatile ResourceKey<Level> dimension;
        volatile String url;
        volatile int volume;
        volatile boolean privateOnly;

        Desired(UUID playerId, ResourceKey<Level> dimension, String url, int volume, boolean privateOnly) {
            this.playerId = playerId;
            this.dimension = dimension;
            this.url = url;
            this.volume = volume;
            this.privateOnly = privateOnly;
        }
    }

    private static final Map<UUID, Desired> DESIRED = new ConcurrentHashMap<>();
    private static final Map<UUID, RadioStreamPlayer> PLAYERS = new ConcurrentHashMap<>();
    private static final Map<UUID, String> NOW_PLAYING = new ConcurrentHashMap<>();
    private static Level lastLevel;

    private PortableStreamManager() {}

    public static String getNowPlaying(UUID playerId) {
        return playerId == null ? null : NOW_PLAYING.get(playerId);
    }

    public static void applyState(UUID playerId, ResourceKey<Level> dimension, String url,
                                  int volume, boolean playing, boolean privateOnly) {
        if (playerId == null || dimension == null || !playing || url == null || url.trim().isEmpty() || volume <= 0) {
            if (playerId != null) {
                stopPlayer(playerId);
                DESIRED.remove(playerId);
                NOW_PLAYING.remove(playerId);
            }
            return;
        }

        Desired desired = DESIRED.computeIfAbsent(playerId,
                id -> new Desired(id, dimension, url.trim(), Math.max(0, Math.min(10, volume)), privateOnly));
        desired.dimension = dimension;
        desired.url = url.trim();
        desired.volume = Math.max(0, Math.min(10, volume));
        desired.privateOnly = privateOnly;

        RadioStreamPlayer player = PLAYERS.get(playerId);
        if (player != null && !player.matches(desired.url)) {
            stopPlayer(playerId);
            NOW_PLAYING.remove(playerId);
        }
    }

    public static void clientTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            stopAll();
            return;
        }
        if (lastLevel != null && lastLevel != mc.level) stopAll();
        lastLevel = mc.level;

        Vec3 listener = mc.gameRenderer.getMainCamera().getPosition();
        for (var it = DESIRED.entrySet().iterator(); it.hasNext();) {
            var entry = it.next();
            UUID playerId = entry.getKey();
            Desired desired = entry.getValue();

            if (!mc.level.dimension().equals(desired.dimension) ||
                    (desired.privateOnly && !mc.player.getUUID().equals(playerId))) {
                stopPlayer(playerId);
                it.remove();
                NOW_PLAYING.remove(playerId);
                continue;
            }

            Player source = mc.level.getPlayerByUUID(playerId);
            if (source == null) {
                stopPlayer(playerId);
                NOW_PLAYING.remove(playerId);
                continue;
            }

            Vec3 sourcePos = source.position();
            RadioStreamPlayer player = PLAYERS.get(playerId);
            if (player == null || player.isStopped()) {
                stopPlayer(playerId);
                NOW_PLAYING.remove(playerId);
                String streamUrl = desired.url;
                player = new RadioStreamPlayer(streamUrl, sourcePos, desired.volume, title -> {
                    Desired current = DESIRED.get(playerId);
                    if (title != null && !title.isBlank() &&
                            current != null && streamUrl.equals(current.url)) {
                        NOW_PLAYING.put(playerId, title);
                    }
                });
                PLAYERS.put(playerId, player);
                player.start();
            } else {
                player.setWorldPos(sourcePos);
                player.setVolume(desired.volume);
                player.tick(listener);
            }
        }

        for (var it = PLAYERS.entrySet().iterator(); it.hasNext();) {
            var entry = it.next();
            if (!DESIRED.containsKey(entry.getKey()) || entry.getValue().isStopped()) {
                entry.getValue().stop();
                it.remove();
                NOW_PLAYING.remove(entry.getKey());
            }
        }
    }

    public static void stopAll() {
        for (RadioStreamPlayer player : PLAYERS.values()) player.stop();
        PLAYERS.clear();
        DESIRED.clear();
        NOW_PLAYING.clear();
        lastLevel = null;
    }

    private static void stopPlayer(UUID playerId) {
        RadioStreamPlayer player = PLAYERS.remove(playerId);
        if (player != null) player.stop();
    }
}
