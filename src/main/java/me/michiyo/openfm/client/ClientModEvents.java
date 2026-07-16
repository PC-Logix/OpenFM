package me.michiyo.openfm.client;

import me.michiyo.openfm.OpenFM;
import me.michiyo.openfm.client.screen.RadioScreen;
import me.michiyo.openfm.reg.ModBlockEntities;
import me.michiyo.openfm.reg.ModMenus;
import me.michiyo.openfm.client.render.RadioBlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = OpenFM.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent e) {
        e.enqueueWork(() -> {
            // Block Entity Renderer (screen text)
            BlockEntityRenderers.register(ModBlockEntities.RADIO_BE.get(), RadioBlockEntityRenderer::new);
        });
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.RADIO_MENU.get(), RadioScreen::new);
    }

    private ClientModEvents() {}
}
