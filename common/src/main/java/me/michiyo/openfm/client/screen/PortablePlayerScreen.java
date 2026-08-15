package me.michiyo.openfm.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import me.michiyo.openfm.net.C2SPortableAction;
import me.michiyo.openfm.net.C2SSetPortableFields;
import me.michiyo.openfm.net.Net;
import me.michiyo.openfm.net.S2CPortableState;
import me.michiyo.openfm.world.menu.PortablePlayerMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PortablePlayerScreen extends AbstractContainerScreen<PortablePlayerMenu> {
    private static final ResourceLocation GUI_TEX = ResourceLocation.fromNamespaceAndPath(
            "openfm", "textures/gui/gui_portable_player.png");
    private static final ResourceLocation BTN_TEX = ResourceLocation.fromNamespaceAndPath(
            "openfm", "textures/gui/buttons.png");
    private static final int TEX_W = 256;
    private static final int TEX_H = 256;
    private static final int GUI_W = 256;
    private static final int GUI_H = 166;
    private static final int BRAND_Y = 87;

    private EditBox urlBox;
    private SpriteButton playBtn;
    private SpriteButton volDownBtn;
    private SpriteButton volUpBtn;
    private SpriteButton clearBtn;
    private SpriteButton pasteBtn;
    private SpriteButton saveBtn;
    private SpriteButton saveToCardBtn;
    private SpriteButton loadBtn;
    private SpriteButton deleteBtn;
    private SpriteButton closeBtn;
    private boolean lastUrlFocus;

    public PortablePlayerScreen(PortablePlayerMenu menu,
                                net.minecraft.world.entity.player.Inventory inv,
                                Component title) {
        super(menu, inv, title);
        imageWidth = GUI_W;
        imageHeight = GUI_H;
    }

    @Override
    protected void init() {
        super.init();
        int left = leftPos;
        int top = topPos;

        urlBox = new EditBox(font, left + 27, top + 39, 203, 14, Component.literal("Stream URL"));
        urlBox.setMaxLength(2048);
        urlBox.setValue(menu.getUrl());
        addRenderableWidget(urlBox);

        playBtn = addButton(left + 116, top + 61, new Sprite(0, 0, 24, 24),
                Component.literal("Play / Stop"), () -> {
                    sendFields();
                    action(C2SPortableAction.Action.TOGGLE_PLAY);
                });
        volUpBtn = addButton(left + 145, top + 26, new Sprite(58, 24, 10, 10),
                Component.literal("Volume Up"), () -> action(C2SPortableAction.Action.VOL_UP));
        volDownBtn = addButton(left + 103, top + 26, new Sprite(48, 24, 10, 10),
                Component.literal("Volume Down"), () -> action(C2SPortableAction.Action.VOL_DOWN));
        clearBtn = addButton(left + 66, top + 64, new Sprite(48, 0, 48, 8),
                Component.literal("Clear"), () -> {
                    urlBox.setValue("");
                    sendFields();
                    action(C2SPortableAction.Action.CLEAR_URL);
                });
        pasteBtn = addButton(left + 65, top + 74, new Sprite(96, 1, 49, 8),
                Component.literal("Paste"), () -> {
                    String clipboard = Minecraft.getInstance().keyboardHandler.getClipboard();
                    if (clipboard != null) urlBox.setValue(clipboard);
                    sendFields();
                });
        saveBtn = addButton(left + 144, top + 66, new Sprite(145, 0, 42, 6),
                Component.literal("Save"), this::sendFields);
        deleteBtn = addButton(left + 144, top + 73, new Sprite(187, 0, 54, 8),
                Component.literal("Delete Card Save"), () -> action(C2SPortableAction.Action.DELETE_SAVED));
        saveToCardBtn = addButton(left + 33, top + 60, new Sprite(127, 24, 8, 8),
                Component.literal("Write to Card"), () -> action(C2SPortableAction.Action.SAVE_TO_CARD));
        loadBtn = addButton(left + 33, top + 70, new Sprite(135, 24, 8, 8),
                Component.literal("Load from Card"), () -> action(C2SPortableAction.Action.LOAD_FROM_CARD));
        closeBtn = addButton(left + 228, top + 26, new Sprite(68, 24, 7, 8),
                Component.literal("Close"), () -> Minecraft.getInstance().player.closeContainer());

        refreshPlaySprite();
    }

    private SpriteButton addButton(int x, int y, Sprite sprite, Component tooltip, Runnable action) {
        return addRenderableWidget(new SpriteButton(x, y, sprite, tooltip, action));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        boolean urlFocused = urlBox.isFocused();
        if (lastUrlFocus && !urlFocused) sendFields();
        lastUrlFocus = urlFocused;

        if (!urlFocused && !menu.getUrl().equals(urlBox.getValue())) urlBox.setValue(menu.getUrl());
        refreshPlaySprite();
    }

    private void refreshPlaySprite() {
        if (playBtn != null) {
            playBtn.setSprite(menu.isPlaying() ? new Sprite(24, 0, 24, 24) : new Sprite(0, 0, 24, 24));
        }
    }

    private void action(C2SPortableAction.Action action) {
        Net.sendToServer(new C2SPortableAction(menu.containerId, action));
    }

    private void sendFields() {
        Net.sendToServer(new C2SSetPortableFields(menu.containerId, urlBox.getValue()));
    }

    public void applyServerSync(S2CPortableState state) {
        if (Minecraft.getInstance().player != null &&
                state.playerId.equals(Minecraft.getInstance().player.getUUID()) && !urlBox.isFocused()) {
            urlBox.setValue(state.url);
        }
        refreshPlaySprite();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Minecraft.getInstance().options.keyInventory.matches(keyCode, scanCode)) return true;
        if (keyCode == 257 || keyCode == 335) sendFields();
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.blit(GUI_TEX, leftPos, topPos, 0, 0, GUI_W, GUI_H, TEX_W, TEX_H);

        g.drawString(font, String.valueOf(menu.getVolume()), leftPos + 126, topPos + 28, 0xFFFFFF, false);
        g.drawCenteredString(this.font, Component.literal("OpenFM"),
                leftPos + GUI_W / 2,
                topPos + BRAND_Y,
                0x0AFF0A);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {}

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);
        SpriteButton[] buttons = { playBtn, volDownBtn, volUpBtn, clearBtn, pasteBtn,
                saveBtn, saveToCardBtn, loadBtn, deleteBtn, closeBtn };
        for (SpriteButton button : buttons) {
            if (button != null && button.isHovered()) {
                g.renderTooltip(font, button.tooltip, mouseX, mouseY);
                break;
            }
        }
        renderTooltip(g, mouseX, mouseY);
    }

    private record Sprite(int u, int v, int w, int h) {}

    private static final class SpriteButton extends AbstractWidget {
        private Sprite sprite;
        private final Runnable onPress;
        private final Component tooltip;

        SpriteButton(int x, int y, Sprite sprite, Component tooltip, Runnable onPress) {
            super(x, y, sprite.w(), sprite.h(), Component.empty());
            this.sprite = sprite;
            this.tooltip = tooltip;
            this.onPress = onPress;
        }

        void setSprite(Sprite sprite) {
            this.sprite = sprite;
            this.width = sprite.w();
            this.height = sprite.h();
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            if (active) onPress.run();
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            boolean hovered = isHoveredOrFocused();
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(hovered ? 1f : 0.9f, hovered ? 1f : 0.9f, hovered ? 1f : 0.9f, 1f);
            g.blit(BTN_TEX, getX(), getY(), sprite.u(), sprite.v(), sprite.w(), sprite.h(), TEX_W, TEX_H);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.disableBlend();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput out) {}
    }
}
