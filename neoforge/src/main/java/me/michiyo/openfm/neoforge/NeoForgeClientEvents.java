package me.michiyo.openfm.neoforge;

import me.michiyo.openfm.client.ClientEvents;
import me.michiyo.openfm.client.OpenFMClient;
import me.michiyo.openfm.client.render.RadioBlockEntityRenderer;
import me.michiyo.openfm.client.screen.RadioScreen;
import me.michiyo.openfm.client.screen.PortablePlayerScreen;
import me.michiyo.openfm.reg.ModBlockEntities;
import me.michiyo.openfm.reg.ModMenus;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

final class NeoForgeClientEvents {
    static void register(IEventBus modBus) {
        modBus.addListener(NeoForgeClientEvents::clientSetup);
        modBus.addListener(NeoForgeClientEvents::registerScreens);
        NeoForge.EVENT_BUS.addListener(NeoForgeClientEvents::clientTick);
        NeoForge.EVENT_BUS.addListener(NeoForgeClientEvents::renderGui);
        NeoForge.EVENT_BUS.addListener(NeoForgeClientEvents::screenInit);
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> BlockEntityRenderers.register(
                ModBlockEntities.RADIO_BE.get(), RadioBlockEntityRenderer::new));
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.RADIO_MENU.get(), RadioScreen::new);
        event.register(ModMenus.PORTABLE_MENU.get(), PortablePlayerScreen::new);
    }

    private static void clientTick(ClientTickEvent.Post event) {
        ClientEvents.clientTick();
    }

    private static void renderGui(RenderGuiEvent.Post event) {
        ClientEvents.renderHud(event.getGuiGraphics());
    }

    private static void screenInit(ScreenEvent.Init.Post event) {
        OpenFMClient.addSoundOptions(event.getScreen());
    }

    private NeoForgeClientEvents() {}
}

