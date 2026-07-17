package me.michiyo.openfm.fabric;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

@Environment(EnvType.CLIENT)
final class FabricClientBridge {
    static void send(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    private FabricClientBridge() {}
}

