package me.michiyo.openfm.client;

import me.michiyo.openfm.OpenFM;
import me.michiyo.openfm.client.screen.OpenFMAudioSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = OpenFM.MODID, value = Dist.CLIENT)
public final class OpenFMClient {

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof OptionsScreen optionsScreen)) return;

        // Place it near the bottom; tweak to taste.
        int x = event.getScreen().width / 2 - 155;
        int y = event.getScreen().height - 27;

        Button btn = Button.builder(Component.literal("OpenFM Audio..."), b -> {
            Minecraft.getInstance().setScreen(new OpenFMAudioSettingsScreen(optionsScreen));
        }).bounds(x, y, 150, 20).build();

        event.addListener(btn);
    }

    private OpenFMClient() {}
}
