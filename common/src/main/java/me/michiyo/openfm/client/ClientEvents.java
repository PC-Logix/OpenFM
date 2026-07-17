package me.michiyo.openfm.client;

import me.michiyo.openfm.client.audio.RadioStreamManager;

public final class ClientEvents {
    public static void clientTick() {
        RadioStreamManager.clientTick();
    }

    private ClientEvents() {}
}

