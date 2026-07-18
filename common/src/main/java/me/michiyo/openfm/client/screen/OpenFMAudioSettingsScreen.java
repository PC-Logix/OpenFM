package me.michiyo.openfm.client.screen;

import me.michiyo.openfm.config.OpenFMClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class OpenFMAudioSettingsScreen extends Screen {

    private final Screen parent;
    // Updates continuously while dragging; written to config on mouse release / Done.
    private double pendingRadioVolume;

    // Track last committed value so we don't hammer disk if mouseReleased happens elsewhere.
    private double lastCommittedRadioVolume;

    private final OptionInstance<Double> radioVolumeOption;

    public OpenFMAudioSettingsScreen(Screen parent) {
        super(Component.literal("OpenFM Audio"));
        this.parent = parent;

        this.pendingRadioVolume = OpenFMClientConfig.RADIO_VOLUME.get();
        this.lastCommittedRadioVolume = this.pendingRadioVolume;

        this.radioVolumeOption = new OptionInstance<>(
                "openfm.options.radio_volume",
                OptionInstance.noTooltip(),
                (caption, value) -> Component.literal("OpenFM Radio Volume: " + (int) Math.round(value * 100.0) + "%"),
                OptionInstance.UnitDouble.INSTANCE,
                this.pendingRadioVolume,
                // IMPORTANT: do not write config here; just update local value.
                value -> this.pendingRadioVolume = value
        );
    }

    @Override
    protected void init() {
        this.addRenderableWidget(this.radioVolumeOption.createButton(
                this.minecraft.options, this.width / 2 - 155, 40, 310
        ));

        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(this.width / 2 - 100, this.height - 27, 200, 20)
                .build());
    }

    private void commitIfChanged() {
        // Only commit if changed meaningfully
        if (Math.abs(this.pendingRadioVolume - this.lastCommittedRadioVolume) < 1e-6) return;

        try {
            OpenFMClientConfig.RADIO_VOLUME.set(this.pendingRadioVolume);
            this.lastCommittedRadioVolume = this.pendingRadioVolume;
        } catch (Throwable t) {
            // Don't crash if OneDrive/AV locks the file momentarily.
            System.out.println("[OpenFM] Failed to save radioVolume to config: " + t);
        }
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        boolean handled = super.mouseReleased(event);

        // Save when the mouse is released (i.e., end of a drag), but only if value changed.
        // This runs even if release happens off the slider, which is fine due to the change check.
        if (event.button() == 0) {
            commitIfChanged();
        }

        return handled;
    }

    @Override
    public void onClose() {
        // Ensure final value is persisted even if user closes via ESC.
        commitIfChanged();

        Minecraft mc = this.minecraft;
        if (mc != null) {
            mc.setScreen(this.parent);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        g.centeredText(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);
        super.extractRenderState(g, mouseX, mouseY, partialTick);
    }
}
