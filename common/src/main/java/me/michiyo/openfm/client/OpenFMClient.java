package me.michiyo.openfm.client;

import me.michiyo.openfm.config.OpenFMClientConfig;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import net.minecraft.network.chat.Component;

public final class OpenFMClient {
    public static void addSoundOptions(Screen screen) {
        if (!(screen instanceof SoundOptionsScreen)) return;

        for (var child : screen.children()) {
            if (child instanceof OptionsList optionsList) {
                optionsList.addBig(createRadioVolumeOption());
                return;
            }
        }
    }

    private static OptionInstance<Double> createRadioVolumeOption() {
        return new OptionInstance<>(
                "openfm.options.radio_volume",
                OptionInstance.noTooltip(),
                (caption, value) -> Component.literal(
                        "OpenFM Radio Volume: " + (int) Math.round(value * 100.0) + "%"),
                OptionInstance.UnitDouble.INSTANCE,
                OpenFMClientConfig.RADIO_VOLUME.get(),
                OpenFMClientConfig.RADIO_VOLUME::set
        );
    }

    private OpenFMClient() {}
}

