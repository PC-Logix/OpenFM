package me.michiyo.openfm.net;

import me.michiyo.openfm.OpenFM;
import me.michiyo.openfm.world.be.RadioBlockEntity;
import me.michiyo.openfm.world.be.SpeakerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class C2SNowPlaying implements CustomPacketPayload {
    private static final int MAX_TITLE_LENGTH = 256;
    private static final int MAX_URL_LENGTH = 2048;
    private static final double HEARING_VALIDATION_RANGE = 48.0;
    private static final long MIN_REPORT_INTERVAL_NANOS = 250_000_000L;
    private static final Map<UUID, Long> LAST_REPORT_TIME = new HashMap<>();

    public static final Type<C2SNowPlaying> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(OpenFM.MODID, "now_playing")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SNowPlaying> STREAM_CODEC =
            StreamCodec.of((buf, msg) -> encode(msg, buf), C2SNowPlaying::decode);

    private final ResourceKey<Level> radioDimension;
    private final BlockPos radioPos;
    private final String streamUrl;
    private final String title;

    public C2SNowPlaying(ResourceKey<Level> radioDimension, BlockPos radioPos, String streamUrl, String title) {
        this.radioDimension = radioDimension;
        this.radioPos = radioPos.immutable();
        this.streamUrl = streamUrl == null ? "" : streamUrl;
        this.title = sanitizeTitle(title == null ? "" : title);
    }

    private static void encode(C2SNowPlaying msg, FriendlyByteBuf buf) {
        buf.writeIdentifier(msg.radioDimension.identifier());
        buf.writeBlockPos(msg.radioPos);
        buf.writeUtf(msg.streamUrl, MAX_URL_LENGTH);
        buf.writeUtf(msg.title, MAX_TITLE_LENGTH);
    }

    private static C2SNowPlaying decode(FriendlyByteBuf buf) {
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, buf.readIdentifier());
        return new C2SNowPlaying(
                dimension,
                buf.readBlockPos(),
                buf.readUtf(MAX_URL_LENGTH),
                buf.readUtf(MAX_TITLE_LENGTH)
        );
    }

    public static void handle(C2SNowPlaying msg, ServerPlayer player) {
        long now = System.nanoTime();
        Long previousTime = LAST_REPORT_TIME.get(player.getUUID());
        if (previousTime != null && now - previousTime < MIN_REPORT_INTERVAL_NANOS) return;
        LAST_REPORT_TIME.put(player.getUUID(), now);

        ServerLevel radioLevel = player.level().getServer().getLevel(msg.radioDimension);
        if (radioLevel == null || !radioLevel.isLoaded(msg.radioPos)) return;

        BlockEntity blockEntity = radioLevel.getBlockEntity(msg.radioPos);
        if (!(blockEntity instanceof RadioBlockEntity radio)) return;
        if (!radio.isPlaying() || radio.getVolume() <= 0) return;
        if (!radio.getStreamUrl().equals(msg.streamUrl)) return;
        if (!canHearRadio(player, msg.radioDimension, msg.radioPos)) return;

        String title = sanitizeTitle(msg.title);
        if (title.isEmpty()) return;
        radio.acceptNowPlaying(title);
    }

    private static boolean canHearRadio(ServerPlayer player, ResourceKey<Level> radioDimension, BlockPos radioPos) {
        double maxDistanceSquared = HEARING_VALIDATION_RANGE * HEARING_VALIDATION_RANGE;
        if (player.level().dimension().equals(radioDimension)
                && Vec3.atCenterOf(radioPos).distanceToSqr(player.position()) <= maxDistanceSquared) {
            return true;
        }

        ServerLevel playerLevel = player.level();
        BlockPos playerPos = player.blockPosition();
        int chunkRadius = (int) Math.ceil(HEARING_VALIDATION_RANGE / 16.0);
        int centerChunkX = SectionPos.blockToSectionCoord(playerPos.getX());
        int centerChunkZ = SectionPos.blockToSectionCoord(playerPos.getZ());

        for (int chunkX = centerChunkX - chunkRadius; chunkX <= centerChunkX + chunkRadius; chunkX++) {
            for (int chunkZ = centerChunkZ - chunkRadius; chunkZ <= centerChunkZ + chunkRadius; chunkZ++) {
                LevelChunk chunk = playerLevel.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) continue;

                for (BlockEntity nearby : chunk.getBlockEntities().values()) {
                    if (!(nearby instanceof SpeakerBlockEntity speaker)) continue;
                    if (!radioDimension.equals(speaker.getLinkedDim())) continue;
                    if (!radioPos.equals(speaker.getLinkedRadioPos())) continue;
                    if (Vec3.atCenterOf(speaker.getBlockPos()).distanceToSqr(player.position()) <= maxDistanceSquared) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static String sanitizeTitle(String title) {
        String sanitized = title.trim().replace('\r', ' ').replace('\n', ' ');
        if (sanitized.length() > MAX_TITLE_LENGTH) {
            int end = MAX_TITLE_LENGTH;
            if (Character.isHighSurrogate(sanitized.charAt(end - 1))) end--;
            sanitized = sanitized.substring(0, end);
        }
        return sanitized;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
