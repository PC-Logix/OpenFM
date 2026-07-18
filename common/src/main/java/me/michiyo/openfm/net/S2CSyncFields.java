package me.michiyo.openfm.net;

import me.michiyo.openfm.OpenFM;
import me.michiyo.openfm.client.screen.RadioScreen;
import me.michiyo.openfm.platform.Services;
import me.michiyo.openfm.world.be.RadioBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;

public class S2CSyncFields implements CustomPacketPayload {
    public static final Type<S2CSyncFields> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(OpenFM.MODID, "sync_fields")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CSyncFields> STREAM_CODEC =
            StreamCodec.of((buf, msg) -> encode(msg, buf), S2CSyncFields::decode);
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

    public static void handle(S2CSyncFields msg) {
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
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
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
        Services.PLATFORM.sendToTracking(level, pos, pkt);
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
        Services.PLATFORM.sendToPlayer(player, pkt);
    }
}
