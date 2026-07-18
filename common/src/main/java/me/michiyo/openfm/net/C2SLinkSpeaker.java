package me.michiyo.openfm.net;

import me.michiyo.openfm.OpenFM;
import me.michiyo.openfm.world.be.RadioBlockEntity;
import me.michiyo.openfm.world.be.SpeakerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class C2SLinkSpeaker implements CustomPacketPayload {
    public static final Type<C2SLinkSpeaker> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(OpenFM.MODID, "link_speaker")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SLinkSpeaker> STREAM_CODEC =
            StreamCodec.of((buf, msg) -> encode(msg, buf), C2SLinkSpeaker::decode);
    private final boolean unlink;

    private final ResourceKey<Level> speakerDim;
    private final BlockPos speakerPos;

    private final ResourceKey<Level> radioDim;
    private final BlockPos radioPos;

    private C2SLinkSpeaker(boolean unlink, ResourceKey<Level> speakerDim, BlockPos speakerPos, ResourceKey<Level> radioDim, BlockPos radioPos) {
        this.unlink = unlink;
        this.speakerDim = speakerDim;
        this.speakerPos = speakerPos;
        this.radioDim = radioDim;
        this.radioPos = radioPos;
    }

    public static C2SLinkSpeaker link(ResourceKey<Level> speakerDim, BlockPos speakerPos, ResourceKey<Level> radioDim, BlockPos radioPos) {
        return new C2SLinkSpeaker(false, speakerDim, speakerPos, radioDim, radioPos);
    }

    public static C2SLinkSpeaker unlink(ResourceKey<Level> speakerDim, BlockPos speakerPos) {
        return new C2SLinkSpeaker(true, speakerDim, speakerPos, Level.OVERWORLD, BlockPos.ZERO);
    }

    public static void encode(C2SLinkSpeaker msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.unlink);

        buf.writeIdentifier(msg.speakerDim.identifier());
        buf.writeBlockPos(msg.speakerPos);

        buf.writeIdentifier(msg.radioDim.identifier());
        buf.writeBlockPos(msg.radioPos);
    }

    public static C2SLinkSpeaker decode(FriendlyByteBuf buf) {
        boolean unlink = buf.readBoolean();

        ResourceKey<Level> speakerDim = ResourceKey.create(Registries.DIMENSION, buf.readIdentifier());
        BlockPos speakerPos = buf.readBlockPos();

        ResourceKey<Level> radioDim = ResourceKey.create(Registries.DIMENSION, buf.readIdentifier());
        BlockPos radioPos = buf.readBlockPos();

        return new C2SLinkSpeaker(unlink, speakerDim, speakerPos, radioDim, radioPos);
    }

    public static void handle(C2SLinkSpeaker msg, ServerPlayer player) {
            ServerLevel speakerLevel = player.level().getServer().getLevel(msg.speakerDim);
            if (speakerLevel == null) return;
            if (!speakerLevel.isLoaded(msg.speakerPos)) return;

            BlockEntity be = speakerLevel.getBlockEntity(msg.speakerPos);
            if (!(be instanceof SpeakerBlockEntity speakerBe)) return;

            if (msg.unlink) {
                speakerBe.clearLink();
                sendSpeakerUpdate(speakerLevel, msg.speakerPos);
                return;
            }

            ServerLevel radioLevel = player.level().getServer().getLevel(msg.radioDim);
            if (radioLevel == null) return;
            if (!radioLevel.isLoaded(msg.radioPos)) return;

            // Optional validation: ensure the radio BE exists and is a RadioBlockEntity.
            // If you don't care, you can remove this block (speaker tick will self-clear later).
            BlockEntity radioBe = radioLevel.getBlockEntity(msg.radioPos);
            if (!(radioBe instanceof RadioBlockEntity)) {
                speakerBe.clearLink();
                sendSpeakerUpdate(speakerLevel, msg.speakerPos);
                return;
            }

            speakerBe.setLink(msg.radioDim, msg.radioPos);
            sendSpeakerUpdate(speakerLevel, msg.speakerPos);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static void sendSpeakerUpdate(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        level.sendBlockUpdated(pos, state, state, 3);
    }
}
