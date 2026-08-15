package me.michiyo.openfm.client;

import me.michiyo.openfm.client.audio.RadioStreamManager;
import me.michiyo.openfm.client.audio.PortableStreamManager;
import net.minecraft.client.gui.GuiGraphics;

public final class ClientEvents {
    public static void clientTick() {
        RadioStreamManager.clientTick();
        PortableStreamManager.clientTick();
    }

    public static void renderHud(GuiGraphics graphics) {
        PortablePlayerHud.render(graphics);
    }

    private ClientEvents() {}
}

