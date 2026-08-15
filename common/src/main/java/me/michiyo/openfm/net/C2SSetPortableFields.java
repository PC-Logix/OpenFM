package me.michiyo.openfm.net;

import me.michiyo.openfm.OpenFM;
import me.michiyo.openfm.world.item.PortablePlayerItem;
import me.michiyo.openfm.world.menu.PortablePlayerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class C2SSetPortableFields implements CustomPacketPayload {
    public static final Type<C2SSetPortableFields> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OpenFM.MODID, "set_portable_fields"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SSetPortableFields> STREAM_CODEC =
            StreamCodec.of((buf, msg) -> encode(msg, buf), C2SSetPortableFields::decode);

    public final int containerId;
    public final String url;

    public C2SSetPortableFields(int containerId, String url) {
        this.containerId = containerId;
        this.url = url == null ? "" : url;
    }

    private static void encode(C2SSetPortableFields msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.containerId);
        buf.writeUtf(msg.url, 2048);
    }

    private static C2SSetPortableFields decode(FriendlyByteBuf buf) {
        return new C2SSetPortableFields(buf.readVarInt(), buf.readUtf(2048));
    }

    public static void handle(C2SSetPortableFields msg, ServerPlayer player) {
        if (player.containerMenu.containerId != msg.containerId ||
                !(player.containerMenu instanceof PortablePlayerMenu menu) || !menu.stillValid(player)) return;
        PortablePlayerItem.setUrl(menu.portable, msg.url);
        PortablePlayerSync.sync(player);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
