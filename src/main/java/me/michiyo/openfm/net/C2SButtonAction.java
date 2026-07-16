package me.michiyo.openfm.net;

import me.michiyo.openfm.OpenFM;
import me.michiyo.openfm.world.be.RadioBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class C2SButtonAction implements CustomPacketPayload {
    public static final Type<C2SButtonAction> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OpenFM.MODID, "button_action")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SButtonAction> STREAM_CODEC =
            StreamCodec.of((buf, msg) -> encode(msg, buf), C2SButtonAction::decode);
    public enum Action {
        VOL_DOWN, VOL_UP,
        TOGGLE_LOCK,
        TOGGLE_PLAY,      // NEW
        CLEAR_URL,
        SAVE_TO_CARD,
        LOAD_FROM_CARD,
        DELETE_SAVED
    }

    public final BlockPos pos;
    public final Action action;

    public C2SButtonAction(BlockPos pos, Action action) {
        this.pos = pos;
        this.action = action;
    }

    public static void encode(C2SButtonAction msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeVarInt(msg.action.ordinal());
    }

    public static C2SButtonAction decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        int ord = buf.readVarInt();
        Action[] vals = Action.values();
        if (ord < 0 || ord >= vals.length) ord = 0;
        return new C2SButtonAction(pos, vals[ord]);
    }

    public static void handle(C2SButtonAction msg, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;

        var level = player.serverLevel();
        var be = level.getBlockEntity(msg.pos);
        if (!(be instanceof RadioBlockEntity radio)) return;

            boolean hasOwner = radio.getOwnerUuid() != null;
            boolean isOwner = hasOwner && radio.getOwnerUuid().equals(player.getUUID());

            // Special-case TOGGLE_LOCK so non-owners can't unlock
            if (msg.action == Action.TOGGLE_LOCK) {
                if (!radio.isLocked()) {
                    radio.ensureOwner(player.getUUID());
                    radio.setLocked(true);
                } else {
                    if (!isOwner) return;
                    radio.setLocked(false);
                }

                radio.setChanged();
                S2CSyncFields.syncToTracking(level, msg.pos, radio);
            return;
        }

            // For all other actions, enforce lock
            if (radio.isLocked() && hasOwner && !isOwner) {
            return;
        }

            // If unowned, first editor becomes owner
            radio.ensureOwner(player.getUUID());

            switch (msg.action) {
                case VOL_DOWN -> radio.setVolume(radio.getVolume() - 1);
                case VOL_UP -> radio.setVolume(radio.getVolume() + 1);

                case TOGGLE_PLAY -> {
                    // Only allow play if there is a URL and volume > 0 (volume can be changed separately)
                    boolean canPlay = radio.getStreamUrl() != null && !radio.getStreamUrl().isEmpty() && radio.getVolume() > 0;
                    if (!radio.isPlaying()) {
                        if (!canPlay) return;
                        radio.setPlaying(true);
                    } else {
                        radio.setPlaying(false);
                    }
                }

                case CLEAR_URL -> {
                    radio.setStreamUrl("");
                    // If you clear URL, it should stop too.
                    radio.setPlaying(false);
                }

                case SAVE_TO_CARD -> MemoryCardLogic.saveToCard(radio);
                case LOAD_FROM_CARD -> MemoryCardLogic.loadFromCard(radio);
                case DELETE_SAVED -> MemoryCardLogic.deleteFromCard(radio);
                default -> {}
            }

        radio.setChanged();
        S2CSyncFields.syncToTracking(level, msg.pos, radio);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
