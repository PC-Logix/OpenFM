package me.michiyo.openfm.net;

import me.michiyo.openfm.client.screen.RadioScreen;
import me.michiyo.openfm.world.be.RadioBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class S2CSyncFields {
    public final BlockPos pos;
    public final String url;
    public final String screenColor;
    public final String screenText;
    public final int volume;
    public final boolean locked;
    public final boolean playing; // NEW

    public S2CSyncFields(BlockPos pos, String url, String screenColor, String screenText, int volume, boolean locked, boolean playing) {
        this.pos = pos;
        this.url = url;
        this.screenColor = screenColor;
        this.screenText = screenText;
        this.volume = volume;
        this.locked = locked;
        this.playing = playing;
    }

    public static void encode(S2CSyncFields msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeUtf(msg.url, 2048);
        buf.writeUtf(msg.screenColor, 64);
        buf.writeUtf(msg.screenText, 256);
        buf.writeVarInt(msg.volume);
        buf.writeBoolean(msg.locked);
        buf.writeBoolean(msg.playing); // NEW
    }

    public static S2CSyncFields decode(FriendlyByteBuf buf) {
        return new S2CSyncFields(
                buf.readBlockPos(),
                buf.readUtf(2048),
                buf.readUtf(64),
                buf.readUtf(256),
                buf.readVarInt(),
                buf.readBoolean(),
                buf.readBoolean()
        );
    }

    public static void handle(S2CSyncFields msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            var be = mc.level.getBlockEntity(msg.pos);
            if (be instanceof RadioBlockEntity radio) {
                // IMPORTANT: do not call the normal setters; apply without causing sync loops
                radio.applyClientSync(msg.url, msg.screenColor, msg.screenText, msg.volume, msg.locked, msg.playing);
            }

            if (mc.screen instanceof RadioScreen screen) {
                screen.applyServerSync(msg);
            }
        });

        //System.out.println("[OpenFM] S2C received for " + msg.pos + " color=" + msg.screenColor + " text=" + msg.screenText + " playing=" + msg.playing);
        ctx.setPacketHandled(true);
    }

    public static void syncToTracking(ServerLevel level, BlockPos pos, RadioBlockEntity radio) {
        S2CSyncFields pkt = new S2CSyncFields(
                pos,
                radio.getStreamUrl(),
                radio.getScreenColor(),
                radio.getScreenText(),
                radio.getVolume(),
                radio.isLocked(),
                radio.isPlaying()
        );
        Net.CH.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)), pkt);
    }

    public static void syncToPlayer(net.minecraft.server.level.ServerPlayer player, BlockPos pos, RadioBlockEntity radio) {
        S2CSyncFields pkt = new S2CSyncFields(
                pos,
                radio.getStreamUrl(),
                radio.getScreenColor(),
                radio.getScreenText(),
                radio.getVolume(),
                radio.isLocked(),
                radio.isPlaying()
        );
        Net.CH.send(PacketDistributor.PLAYER.with(() -> player), pkt);
    }
}
