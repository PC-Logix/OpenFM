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

public class C2SSetFields implements CustomPacketPayload {
    public static final Type<C2SSetFields> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OpenFM.MODID, "set_fields")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SSetFields> STREAM_CODEC =
            StreamCodec.of((buf, msg) -> encode(msg, buf), C2SSetFields::decode);
    public final BlockPos pos;
    public final String url;
    public final String screenColor;
    public final String screenText;

    public C2SSetFields(BlockPos pos, String url, String screenColor, String screenText) {
        this.pos = pos;
        this.url = url;
        this.screenColor = screenColor;
        this.screenText = screenText;
    }

    public static void encode(C2SSetFields msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeUtf(msg.url == null ? "" : msg.url, 2048);
        buf.writeUtf(msg.screenColor == null ? "" : msg.screenColor, 64);
        buf.writeUtf(msg.screenText == null ? "" : msg.screenText, 256);
    }

    public static C2SSetFields decode(FriendlyByteBuf buf) {
        return new C2SSetFields(
                buf.readBlockPos(),
                buf.readUtf(2048),
                buf.readUtf(64),
                buf.readUtf(256)
        );
    }

    public static void handle(C2SSetFields msg, ServerPlayer player) {
            var level = player.serverLevel();
            var be = level.getBlockEntity(msg.pos);
            if (!(be instanceof RadioBlockEntity radio)) return;

            boolean hasOwner = radio.getOwnerUuid() != null;
            boolean isOwner = hasOwner && radio.getOwnerUuid().equals(player.getUUID());

            if (radio.isLocked() && hasOwner && !isOwner) {
                return;
            }

            // Claim on first edit
            radio.ensureOwner(player.getUUID());

            radio.setStreamUrl(msg.url); // URL can be blank (clear is valid)

            String color = msg.screenColor == null ? "" : msg.screenColor.trim();
            if (!color.isEmpty()) {
                radio.setScreenColor(color);
            }

            String text = msg.screenText == null ? "" : msg.screenText;
            if (!text.trim().isEmpty()) {
                radio.setScreenText(text);
            }

        radio.setChanged();
        S2CSyncFields.syncToTracking(level, msg.pos, radio);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
