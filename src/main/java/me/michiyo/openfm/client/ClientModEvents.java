package me.michiyo.openfm.client;

import me.michiyo.openfm.OpenFM;
import me.michiyo.openfm.client.screen.RadioScreen;
import me.michiyo.openfm.reg.ModBlockEntities;
import me.michiyo.openfm.reg.ModMenus;
import me.michiyo.openfm.client.render.RadioBlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.gui.screens.MenuScreens;

@Mod.EventBusSubscriber(modid = OpenFM.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent e) {
        e.enqueueWork(() -> {
            MenuScreens.register(ModMenus.RADIO_MENU.get(), RadioScreen::new);

            // Block Entity Renderer (screen text)
            BlockEntityRenderers.register(ModBlockEntities.RADIO_BE.get(), RadioBlockEntityRenderer::new);
        });
    }


    private ClientModEvents() {}
}
