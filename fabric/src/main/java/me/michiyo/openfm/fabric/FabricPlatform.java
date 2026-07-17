package me.michiyo.openfm.fabric;

import me.michiyo.openfm.platform.OpenFMPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;

public final class FabricPlatform implements OpenFMPlatform {
    @Override public String loaderName() { return "Fabric"; }
    @Override public boolean isModLoaded(String modId) { return FabricLoader.getInstance().isModLoaded(modId); }
    @Override public boolean isPhysicalClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }
    @Override public void sendToServer(CustomPacketPayload payload) { FabricClientBridge.send(payload); }
    @Override public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }
    @Override public void sendToTracking(ServerLevel level, BlockPos pos, CustomPacketPayload payload) {
        PlayerLookup.tracking(level, pos).forEach(player -> ServerPlayNetworking.send(player, payload));
    }
    @Override public void openMenu(ServerPlayer player, MenuProvider provider, BlockPos pos) {
        player.openMenu(new ExtendedScreenHandlerFactory<BlockPos>() {
            @Override public BlockPos getScreenOpeningData(ServerPlayer player) { return pos; }
            @Override public net.minecraft.network.chat.Component getDisplayName() { return provider.getDisplayName(); }
            @Override public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
                    int id, net.minecraft.world.entity.player.Inventory inventory,
                    net.minecraft.world.entity.player.Player menuPlayer) {
                return provider.createMenu(id, inventory, menuPlayer);
            }
        });
    }
    @Override public double getRadioVolume() { return FabricClientConfig.getVolume(); }
    @Override public void setRadioVolume(double volume) { FabricClientConfig.setVolume(volume); }
}

