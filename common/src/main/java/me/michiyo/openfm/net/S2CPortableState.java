package me.michiyo.openfm.net;

import me.michiyo.openfm.OpenFM;
import me.michiyo.openfm.client.audio.PortableStreamManager;
import me.michiyo.openfm.client.screen.PortablePlayerScreen;
import me.michiyo.openfm.world.item.PortablePlayerItem;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.UUID;

public final class S2CPortableState implements CustomPacketPayload {
    public static final Type<S2CPortableState> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OpenFM.MODID, "portable_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CPortableState> STREAM_CODEC =
            StreamCodec.of((buf, msg) -> encode(msg, buf), S2CPortableState::decode);

    public final UUID playerId;
    public final ResourceKey<Level> dimension;
    public final String url;
    public final int volume;
    public final boolean playing;
    public final boolean privateOnly;

    public S2CPortableState(UUID playerId, ResourceKey<Level> dimension, String url,
                            int volume, boolean playing, boolean privateOnly) {
        this.playerId = playerId;
        this.dimension = dimension;
        this.url = url == null ? "" : url;
        this.volume = volume;
        this.playing = playing;
        this.privateOnly = privateOnly;
    }

    private static void encode(S2CPortableState msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.playerId);
        buf.writeResourceLocation(msg.dimension.location());
        buf.writeUtf(msg.url, 2048);
        buf.writeVarInt(msg.volume);
        buf.writeBoolean(msg.playing);
        buf.writeBoolean(msg.privateOnly);
    }

    private static S2CPortableState decode(FriendlyByteBuf buf) {
        return new S2CPortableState(
                buf.readUUID(),
                ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation()),
                buf.readUtf(2048),
                buf.readVarInt(),
                buf.readBoolean(),
                buf.readBoolean());
    }

    public static void handle(S2CPortableState msg) {
        PortableStreamManager.applyState(msg.playerId, msg.dimension, msg.url,
                msg.volume, msg.playing, msg.privateOnly);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getUUID().equals(msg.playerId)) {
            ItemStackHolder.update(msg);
            if (mc.screen instanceof PortablePlayerScreen screen) screen.applyServerSync(msg);
        }
    }

    private static final class ItemStackHolder {
        private static void update(S2CPortableState msg) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            var stack = PortablePlayerItem.findHeldStack(mc.player);
            if (stack.isEmpty()) return;
            PortablePlayerItem.setUrl(stack, msg.url);
            PortablePlayerItem.setVolume(stack, msg.volume);
            PortablePlayerItem.setPlaying(stack, msg.playing);
            PortablePlayerItem.setPrivate(stack, msg.privateOnly);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
