package me.michiyo.openfm.world.be;

import me.michiyo.openfm.reg.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class SpeakerBlockEntity extends BlockEntity {
    private static final String NBT_LINKED_DIM = "LinkedDim";
    private static final String NBT_LINKED_POS = "LinkedPos";
    private static final String NBT_ACTIVE_URL = "ActiveUrl";
    private static final String NBT_ACTIVE_VOL = "ActiveVol";

    private ResourceKey<Level> linkedDim = null;
    private BlockPos linkedRadioPos = null;

    // mirrored fields (synced to client)
    private String activeUrl = "";
    private int activeVol = 0; // 0..10 like radio

    // client-side bookkeeping (safe as long as only touched when level.isClientSide)
    private BlockPos lastClientLinkedRadioPos = null;

    public SpeakerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPEAKER_BE.get(), pos, state);
    }

    public ResourceKey<Level> getLinkedDim() { return linkedDim; }
    public BlockPos getLinkedRadioPos() { return linkedRadioPos; }

    public String getActiveUrl() { return activeUrl; }
    public int getActiveVol() { return activeVol; }

    public boolean isLinked() {
        return linkedDim != null && linkedRadioPos != null;
    }

    public void setLink(ResourceKey<Level> dim, BlockPos radioPos) {
        this.linkedDim = dim;
        this.linkedRadioPos = (radioPos != null) ? radioPos.immutable() : null;
        this.setChanged();
        syncToClient();
        clientRefreshEmitterRegistration(); // safe: no-op on server
    }

    public void clearLink() {
        this.linkedDim = null;
        this.linkedRadioPos = null;
        setActive("", 0);
        this.setChanged();
        syncToClient();
        clientRefreshEmitterRegistration(); // removes old emitter if needed
    }

    private void setActive(String url, int vol) {
        String u = (url == null) ? "" : url;
        int v = Math.max(0, Math.min(10, vol));

        if (u.equals(this.activeUrl) && v == this.activeVol) return;

        this.activeUrl = u;
        this.activeVol = v;
        this.setChanged();
        syncToClient();

        // Speakers extend range; they should register as emitter when linked regardless of URL/vol.
        clientRefreshEmitterRegistration();
    }

    private void syncToClient() {
        if (level == null) return;
        if (!level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    // -----------------------
    // Client emitter bridging
    // -----------------------

    private static void clientRegisterEmitter(BlockPos radioPos, BlockPos speakerPos) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            me.michiyo.openfm.client.audio.RadioStreamManager.setEmitterForRadio(
                    radioPos,
                    speakerPos,
                    Vec3.atCenterOf(speakerPos)
            );
        });
    }

    private static void clientUnregisterEmitter(BlockPos radioPos, BlockPos speakerPos) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            me.michiyo.openfm.client.audio.RadioStreamManager.clearEmitterForRadio(radioPos, speakerPos);
        });
    }

    /**
     * Ensures this speaker is registered as an emitter for its linked radio (client-only),
     * and removed from any previous linked radio when the link changes.
     *
     * Safe to call from anywhere: it will no-op on server.
     */
    private void clientRefreshEmitterRegistration() {
        if (level == null || !level.isClientSide) return;

        // If link changed, remove from old radio emitter list
        if (lastClientLinkedRadioPos != null && (linkedRadioPos == null || !lastClientLinkedRadioPos.equals(linkedRadioPos))) {
            clientUnregisterEmitter(lastClientLinkedRadioPos, worldPosition);
        }

        // If currently linked, register as an emitter (ALWAYS; do not gate on activeUrl/activeVol)
        if (linkedRadioPos != null) {
            clientRegisterEmitter(linkedRadioPos, worldPosition);
        }

        lastClientLinkedRadioPos = linkedRadioPos;
    }

    private void clientStopEmitter() {
        if (level == null || !level.isClientSide) return;

        if (linkedRadioPos != null) {
            clientUnregisterEmitter(linkedRadioPos, worldPosition);
        }
        if (lastClientLinkedRadioPos != null) {
            clientUnregisterEmitter(lastClientLinkedRadioPos, worldPosition);
        }
        lastClientLinkedRadioPos = null;
    }

    // -----------------------
    // Server-side mirror tick
    // -----------------------

    public static void serverTick(Level level, BlockPos pos, BlockState state, SpeakerBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (!be.isLinked()) {
            // ensure it stops if unlinked
            if (!be.activeUrl.isEmpty() || be.activeVol != 0) be.setActive("", 0);
            return;
        }

        ServerLevel radioLevel = serverLevel.getServer().getLevel(be.linkedDim);
        if (radioLevel == null) {
            be.setActive("", 0);
            return;
        }

        BlockEntity other = radioLevel.getBlockEntity(be.linkedRadioPos);
        if (!(other instanceof RadioBlockEntity radioBe)) {
            be.setActive("", 0);
            return;
        }

        // Mirror radio settings
        be.setActive(radioBe.getStreamUrl(), radioBe.getVolume());
    }

    // -----------------------
    // NBT / networking
    // -----------------------

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        String dimStr = tag.getString(NBT_LINKED_DIM);
        if (dimStr != null && !dimStr.isEmpty()) {
            this.linkedDim = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dimStr));
        } else {
            this.linkedDim = null;
        }

        if (tag.contains(NBT_LINKED_POS)) {
            this.linkedRadioPos = BlockPos.of(tag.getLong(NBT_LINKED_POS));
        } else {
            this.linkedRadioPos = null;
        }

        this.activeUrl = tag.getString(NBT_ACTIVE_URL);
        this.activeVol = tag.getInt(NBT_ACTIVE_VOL);

        // On client, register emitter after load (safe no-op on server)
        clientRefreshEmitterRegistration();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.putString(NBT_LINKED_DIM, linkedDim == null ? "" : linkedDim.location().toString());
        if (linkedRadioPos != null) {
            tag.putLong(NBT_LINKED_POS, linkedRadioPos.asLong());
        }

        tag.putString(NBT_ACTIVE_URL, activeUrl == null ? "" : activeUrl);
        tag.putInt(NBT_ACTIVE_VOL, activeVol);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        // load() is not guaranteed to be called here, so refresh using current fields
        clientRefreshEmitterRegistration();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        clientRefreshEmitterRegistration();
    }

    @Override
    public void setRemoved() {
        clientStopEmitter();
        super.setRemoved();
    }
}
