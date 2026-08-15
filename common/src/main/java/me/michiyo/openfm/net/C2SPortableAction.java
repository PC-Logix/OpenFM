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

public final class C2SPortableAction implements CustomPacketPayload {
    public static final Type<C2SPortableAction> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OpenFM.MODID, "portable_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SPortableAction> STREAM_CODEC =
            StreamCodec.of((buf, msg) -> encode(msg, buf), C2SPortableAction::decode);

    public enum Action { VOL_DOWN, VOL_UP, TOGGLE_PLAY, CLEAR_URL, SAVE_TO_CARD, LOAD_FROM_CARD, DELETE_SAVED }

    public final int containerId;
    public final Action action;

    public C2SPortableAction(int containerId, Action action) {
        this.containerId = containerId;
        this.action = action;
    }

    private static void encode(C2SPortableAction msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.containerId);
        buf.writeVarInt(msg.action.ordinal());
    }

    private static C2SPortableAction decode(FriendlyByteBuf buf) {
        int id = buf.readVarInt();
        int ordinal = buf.readVarInt();
        Action[] actions = Action.values();
        if (ordinal < 0 || ordinal >= actions.length) ordinal = 0;
        return new C2SPortableAction(id, actions[ordinal]);
    }

    public static void handle(C2SPortableAction msg, ServerPlayer player) {
        if (player.containerMenu.containerId != msg.containerId ||
                !(player.containerMenu instanceof PortablePlayerMenu menu) || !menu.stillValid(player)) return;

        var portable = menu.portable;
        switch (msg.action) {
            case VOL_DOWN -> PortablePlayerItem.setVolume(portable, PortablePlayerItem.getVolume(portable) - 1);
            case VOL_UP -> PortablePlayerItem.setVolume(portable, PortablePlayerItem.getVolume(portable) + 1);
            case TOGGLE_PLAY -> { if (!PortablePlayerItem.togglePlayback(portable)) return; }
            case CLEAR_URL -> {
                PortablePlayerItem.setUrl(portable, "");
                PortablePlayerItem.setPlaying(portable, false);
            }
            case SAVE_TO_CARD -> {
                var card = menu.getCard();
                if (card.isEmpty()) return;
                PortablePlayerItem.saveUrlToCard(card, PortablePlayerItem.getUrl(portable), PortablePlayerItem.getVolume(portable));
                menu.saveCard();
            }
            case LOAD_FROM_CARD -> {
                var card = menu.getCard();
                if (card.isEmpty()) return;
                PortablePlayerItem.loadFromCard(portable, card);
            }
            case DELETE_SAVED -> {
                var card = menu.getCard();
                if (card.isEmpty()) return;
                net.minecraft.world.item.component.CustomData.update(
                        net.minecraft.core.component.DataComponents.CUSTOM_DATA, card, tag -> tag.remove("OpenFMCard"));
                menu.saveCard();
            }
        }

        menu.broadcastChanges();
        PortablePlayerSync.sync(player);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
