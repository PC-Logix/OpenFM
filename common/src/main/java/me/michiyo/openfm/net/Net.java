package me.michiyo.openfm.net;

import me.michiyo.openfm.platform.Services;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class Net {
    public static void sendToServer(CustomPacketPayload payload) {
        Services.PLATFORM.sendToServer(payload);
    }

    private Net() {}
}

