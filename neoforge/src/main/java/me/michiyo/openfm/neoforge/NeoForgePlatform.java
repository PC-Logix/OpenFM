package me.michiyo.openfm.neoforge;

import me.michiyo.openfm.platform.OpenFMPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;

public final class NeoForgePlatform implements OpenFMPlatform {
    @Override public String loaderName() { return "NeoForge"; }
    @Override public boolean isModLoaded(String modId) { return ModList.get().isLoaded(modId); }
    @Override public boolean isPhysicalClient() { return FMLEnvironment.dist == Dist.CLIENT; }
    @Override public void sendToServer(CustomPacketPayload payload) { PacketDistributor.sendToServer(payload); }
    @Override public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }
    @Override public void sendToTracking(ServerLevel level, BlockPos pos, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersTrackingChunk(level, level.getChunkAt(pos).getPos(), payload);
    }
    @Override public void openMenu(ServerPlayer player, MenuProvider provider, BlockPos pos) {
        player.openMenu(provider, buffer -> buffer.writeBlockPos(pos));
    }
    @Override public double getRadioVolume() { return NeoForgeClientConfig.RADIO_VOLUME.get(); }
    @Override public void setRadioVolume(double volume) { NeoForgeClientConfig.RADIO_VOLUME.set(volume); }
}

