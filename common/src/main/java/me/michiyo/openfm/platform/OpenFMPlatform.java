package me.michiyo.openfm.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;

public interface OpenFMPlatform {
    String loaderName();

    boolean isModLoaded(String modId);

    boolean isPhysicalClient();

    void sendToServer(CustomPacketPayload payload);

    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

    void sendToTracking(ServerLevel level, BlockPos pos, CustomPacketPayload payload);

    void openMenu(ServerPlayer player, MenuProvider provider, BlockPos pos);

    double getRadioVolume();

    void setRadioVolume(double volume);
}

