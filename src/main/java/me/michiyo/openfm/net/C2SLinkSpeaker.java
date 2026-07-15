package me.michiyo.openfm.net;

import me.michiyo.openfm.world.be.RadioBlockEntity;
import me.michiyo.openfm.world.be.SpeakerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SLinkSpeaker {
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

        buf.writeResourceLocation(msg.speakerDim.location());
        buf.writeBlockPos(msg.speakerPos);

        buf.writeResourceLocation(msg.radioDim.location());
        buf.writeBlockPos(msg.radioPos);
    }

    public static C2SLinkSpeaker decode(FriendlyByteBuf buf) {
        boolean unlink = buf.readBoolean();

        ResourceKey<Level> speakerDim = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
        BlockPos speakerPos = buf.readBlockPos();

        ResourceKey<Level> radioDim = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
        BlockPos radioPos = buf.readBlockPos();

        return new C2SLinkSpeaker(unlink, speakerDim, speakerPos, radioDim, radioPos);
    }

    public static void handle(C2SLinkSpeaker msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();

        ctx.enqueueWork(() -> {
            if (ctx.getSender() == null) return;

            ServerLevel speakerLevel = ctx.getSender().server.getLevel(msg.speakerDim);
            if (speakerLevel == null) return;
            if (!speakerLevel.isLoaded(msg.speakerPos)) return;

            BlockEntity be = speakerLevel.getBlockEntity(msg.speakerPos);
            if (!(be instanceof SpeakerBlockEntity speakerBe)) return;

            if (msg.unlink) {
                speakerBe.clearLink();
                sendSpeakerUpdate(speakerLevel, msg.speakerPos);
                return;
            }

            ServerLevel radioLevel = ctx.getSender().server.getLevel(msg.radioDim);
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
        });

        ctx.setPacketHandled(true);
    }

    private static void sendSpeakerUpdate(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        level.sendBlockUpdated(pos, state, state, 3);
    }
}
