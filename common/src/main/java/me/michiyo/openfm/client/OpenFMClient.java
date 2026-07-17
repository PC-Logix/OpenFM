package me.michiyo.openfm.client;

import me.michiyo.openfm.client.screen.OpenFMAudioSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public final class OpenFMClient {
    public static void addSettingsButton(Screen screen, Consumer<Button> addButton) {
        if (!(screen instanceof OptionsScreen optionsScreen)) return;

        int x = screen.width / 2 - 155;
        int y = screen.height - 27;
        Button button = Button.builder(Component.literal("OpenFM Audio..."), ignored ->
                Minecraft.getInstance().setScreen(new OpenFMAudioSettingsScreen(optionsScreen)))
                .bounds(x, y, 150, 20)
                .build();
        addButton.accept(button);
    }

    private OpenFMClient() {}
}

