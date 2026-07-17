package me.michiyo.openfm.fabric;

import me.michiyo.openfm.client.ClientEvents;
import me.michiyo.openfm.client.OpenFMClient;
import me.michiyo.openfm.client.render.RadioBlockEntityRenderer;
import me.michiyo.openfm.client.screen.RadioScreen;
import me.michiyo.openfm.reg.ModBlockEntities;
import me.michiyo.openfm.reg.ModMenus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;

public final class OpenFMFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.register(ModBlockEntities.RADIO_BE.get(), RadioBlockEntityRenderer::new);
        MenuScreens.register(ModMenus.RADIO_MENU.get(), RadioScreen::new);
        FabricNetwork.registerClient();
        ClientTickEvents.END_CLIENT_TICK.register(client -> ClientEvents.clientTick());
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) ->
                OpenFMClient.addSettingsButton(screen, button -> Screens.getButtons(screen).add(button)));
    }
}
