package me.michiyo.openfm.world.be;

import me.michiyo.openfm.client.audio.RadioStreamManager;
import me.michiyo.openfm.net.S2CSyncFields;
import me.michiyo.openfm.reg.ModBlockEntities;
import me.michiyo.openfm.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

public class RadioBlockEntity extends BlockEntity {
    public static final String DEFAULT_COLOR = "0AFF0A";
    public static final String DEFAULT_TEXT  = "OpenFM";

    private UUID ownerUuid = null;
    private boolean locked = false;

    // explicit playback state
    private boolean playing = false;
    private String nowPlaying = "";

    private int volume = 5; // 0..10
    private String streamUrl = "";
    private String screenColor = DEFAULT_COLOR;
    private String screenText = DEFAULT_TEXT;

    boolean newPlaying;

    private final SimpleContainer inv = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            RadioBlockEntity.this.markDirtyAndSync();
        }
    };

    public RadioBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RADIO_BE.get(), pos, state);
    }

    private void markDirtyAndSync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static String sanitizeUrl(String url) {
        return (url == null) ? "" : url.trim();
    }

    private static String sanitizeColor(String c) {
        String s = (c == null) ? "" : c.trim();
        if (s.isEmpty()) return DEFAULT_COLOR;
        if (s.startsWith("#")) s = s.substring(1);
        s = s.toUpperCase();
        if (!s.matches("^[0-9A-F]{6}$")) return DEFAULT_COLOR;
        return s;
    }

    private static String sanitizeScreenText(String t) {
        String s = (t == null) ? "" : t;
        if (s.trim().isEmpty()) return DEFAULT_TEXT;
        return s;
    }

    public UUID getOwnerUuid() { return ownerUuid; }

    public void ensureOwner(UUID uuid) {
        if (ownerUuid == null && uuid != null) {
            ownerUuid = uuid;
            markDirtyAndSync();
        }
    }

    public boolean isLocked() { return locked; }

    public void setLocked(boolean locked) {
        this.locked = locked;
        cc_event("openfm_lock_changed", worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), this.locked);
        markDirtyAndSync();
    }

    public boolean isPlaying() { return playing; }

    public void setPlaying(boolean playing) {
        if (this.playing == playing) return;

        boolean wasPlaying = this.playing;
        this.playing = playing;

        if (!wasPlaying && this.playing) {
            cc_event("openfm_start",
                    worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                    cc_getStreamUrl(), this.volume
            );
        } else if (wasPlaying && !this.playing) {
            cc_event("openfm_stop",
                    worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()
            );
            clearNowPlaying();
        }

        markDirtyAndSync();
    }


    public int getVolume() { return volume; }

    public void setVolume(int v) {
        int nv = clamp(v, 0, 10);
        if (nv == this.volume) return;
        this.volume = nv;
        cc_event("openfm_volume_changed", worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), this.volume);
        markDirtyAndSync();
    }

    public String getStreamUrl() { return streamUrl; }

    public void setStreamUrl(String url) {
        String nu = sanitizeUrl(url);
        if (nu.equals(this.streamUrl)) return;
        this.streamUrl = nu;
        clearNowPlaying();
        cc_event("openfm_url_changed", worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), cc_getStreamUrl());

        markDirtyAndSync();
    }

    public String getScreenColor() { return screenColor; }

    public void setScreenColor(String c) {
        String nc = sanitizeColor(c);
        if (nc.equals(this.screenColor)) return;
        this.screenColor = nc;
        cc_event("openfm_screen_changed", worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), this.screenColor, this.screenText);
        markDirtyAndSync();
    }

    public String getScreenText() { return screenText; }

    public void setScreenText(String t) {
        String nt = sanitizeScreenText(t);
        if (nt.equals(this.screenText)) return;
        this.screenText = nt;
        cc_event("openfm_screen_changed", worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), this.screenColor, this.screenText);
        markDirtyAndSync();
    }

    public SimpleContainer getInventory() { return inv; }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.storeNullable("Owner", UUIDUtil.CODEC, ownerUuid);
        output.putBoolean("Locked", locked);
        output.putBoolean("Playing", playing);
        output.putInt("Volume", volume);
        output.putString("StreamUrl", streamUrl);
        output.putString("ScreenColor", screenColor);
        output.putString("ScreenText", screenText);

        ItemStack stack = inv.getItem(0);
        if (!stack.isEmpty()) output.child("Inv").store("MemoryCard", ItemStack.CODEC, stack);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        ownerUuid = input.read("Owner", UUIDUtil.CODEC).orElse(null);
        locked = input.getBooleanOr("Locked", false);
        playing = input.getBooleanOr("Playing", false);
        volume = clamp(input.getIntOr("Volume", 5), 0, 10);
        streamUrl = sanitizeUrl(input.getStringOr("StreamUrl", ""));
        screenColor = sanitizeColor(input.getStringOr("ScreenColor", DEFAULT_COLOR));
        screenText = sanitizeScreenText(input.getStringOr("ScreenText", DEFAULT_TEXT));

        inv.setItem(0, ItemStack.EMPTY);
        inv.setItem(0, input.childOrEmpty("Inv").read("MemoryCard", ItemStack.CODEC).orElse(ItemStack.EMPTY));
        clientApplyAudioState();
    }

    // ---- client audio wiring (RESPECT playing) ----

    private void clientApplyAudioState() {
        if (level == null || !level.isClientSide()) return;

        if (!playing) {
            RadioStreamManager.applyState(worldPosition, "", 0);
            return;
        }

        if (streamUrl == null || streamUrl.isEmpty() || volume <= 0) {
            RadioStreamManager.applyState(worldPosition, "", 0);
            return;
        }

        RadioStreamManager.applyState(worldPosition, streamUrl, volume);
    }

    public void applyClientSync(String url, String color, String text, int volume, boolean locked, boolean playing) {
        this.streamUrl = (url == null) ? "" : url.trim();
        this.screenColor = sanitizeColor(color);
        this.screenText = sanitizeScreenText(text);
        this.volume = clamp(volume, 0, 10);
        this.locked = locked;
        this.playing = playing;

        if (level != null && level.isClientSide()) {
            clientApplyAudioState();
        }
    }

    @Override
    public void setRemoved() {
        if (level != null && level.isClientSide()) {
            RadioStreamManager.applyState(worldPosition, "", 0);
        }
        super.setRemoved();
    }

    // ---- BE sync plumbing ----

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // ---- ComputerCraft (CC:Tweaked) control ----

    /**
     * CC has no player context. If the block is locked and has an owner, treat CC control as denied.
     */
    public boolean cc_isLockedForCC() {
        return this.locked && this.ownerUuid != null;
    }

    /**
     * Set the stream URL from CC.
     */
    public void cc_setStreamUrl(String url) {
        setStreamUrl(url);

        // If URL is cleared while playing, stop.
        if ((this.streamUrl == null || this.streamUrl.isEmpty()) && this.playing) {
            this.playing = false;
        }

        cc_sync();
    }

    /**
     * Start playback from CC.
     * @return true if playback was started (or already playing), false if it cannot play.
     */
    public boolean cc_start() {
        boolean canPlay = this.streamUrl != null && !this.streamUrl.isEmpty() && this.volume > 0;
        if (!canPlay) return false;

        if (!this.playing) {
            this.playing = true;
            cc_sync();
        }

        return true;
    }

    /** Stop playback from CC. */
    public void cc_stop() {
        if (this.playing) {
            this.playing = false;
            cc_sync();
        }
    }

    public int cc_getVolume() {
        return this.volume;
    }

    public void cc_setVolume(int vol) {
        int nv = Math.max(0, Math.min(10, vol));
        if (nv == this.volume) return;
        this.volume = nv;

        // If volume drops to 0, stop.
        if (this.volume <= 0 && this.playing) {
            this.playing = false;
        }

        cc_sync();
    }

    public int cc_getScreenColor() {
        return Integer.parseInt(this.screenColor, 16);
    }

    public void cc_setScreenColor(int rgb) {
        this.screenColor = String.format("%06X", (rgb & 0xFFFFFF));
        cc_sync();
    }

    public String cc_getScreenText() {
        return this.screenText;
    }

    public void cc_setScreenText(String text) {
        this.screenText = (text == null || text.trim().isEmpty()) ? DEFAULT_TEXT : text;
        cc_sync();
    }

    private void cc_sync() {
        setChanged();

        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);

            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                me.michiyo.openfm.net.S2CSyncFields.syncToTracking(serverLevel, worldPosition, this);
            }
        }
    }

    public String cc_getStreamUrl() {
        return this.streamUrl == null ? "" : this.streamUrl;
    }

    public String getNowPlaying() {
        return this.nowPlaying;
    }

    public void acceptNowPlaying(String title) {
        String next = title == null ? "" : title.trim().replace('\r', ' ').replace('\n', ' ');
        if (next.equals(this.nowPlaying)) return;
        this.nowPlaying = next;
        cc_event("openfm_now_playing",
                worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), this.nowPlaying);
    }

    private void clearNowPlaying() {
        if (this.nowPlaying.isEmpty()) return;
        this.nowPlaying = "";
        cc_event("openfm_now_playing",
                worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), "");
    }

    private void cc_event(String event, Object... args) {
        if (this.level == null || this.level.isClientSide()) return;
        if (!(this.level instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;
        if (!Services.PLATFORM.isModLoaded("computercraft")) return;

        try {
            Class<?> cls = Class.forName("me.michiyo.openfm.compat.cc.CCEvents");
            java.lang.reflect.Method m = cls.getMethod(
                    "queue",
                    net.minecraft.server.level.ServerLevel.class,
                    net.minecraft.core.BlockPos.class,
                    String.class,
                    Object[].class
            );
            m.invoke(null, serverLevel, this.worldPosition, event, args);
        } catch (Throwable ignored) {
            // swallow: CC missing, classloader timing, etc.
        }
    }
}
