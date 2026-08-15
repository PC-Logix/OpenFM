package me.michiyo.openfm.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import me.michiyo.openfm.net.C2SButtonAction;
import me.michiyo.openfm.net.C2SSetFields;
import me.michiyo.openfm.net.Net;
import me.michiyo.openfm.net.S2CSyncFields;
import me.michiyo.openfm.world.menu.RadioMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class RadioScreen extends AbstractContainerScreen<RadioMenu> {
    private static final ResourceLocation GUI_TEX = ResourceLocation.fromNamespaceAndPath("openfm", "textures/gui/gui_radio.png");
    private static final ResourceLocation BTN_TEX = ResourceLocation.fromNamespaceAndPath("openfm", "textures/gui/buttons.png");

    private static final int TEX_W = 256;
    private static final int TEX_H = 256;

    // The GUI sheet is 256x256, but the "container" area we want centered is the classic 256x166.
    // This matches how your 1.12 GUI was positioned (width/2-128, height/2-83).
    private static final int GUI_W = 256;
    private static final int GUI_H = 166;

    private static final int BRAND_Y = 115;

    // 1.12-relative positions (relative to GUI top-left)
    private static final int URL_X = 27;
    private static final int URL_Y = 39;
    private static final int URL_W = 203;
    private static final int URL_H = 14;

    private static final int COLOR_X = 24;
    private static final int COLOR_Y = 92;
    private static final int COLOR_W = 63;
    private static final int COLOR_H = 14;

    private static final int TEXT_X = 108;
    private static final int TEXT_Y = 92;
    private static final int TEXT_W = 128;
    private static final int TEXT_H = 14;

    // Volume display position from 1.12 (volumeBox at width/2-6, height/2-46)
    private static final int VOL_TXT_X = 126;
    private static final int VOL_TXT_Y = 28;

    private EditBox urlBox;
    private EditBox colorBox;
    private EditBox textBox;

    private SpriteButton playBtn;
    private SpriteButton volDownBtn;
    private SpriteButton volUpBtn;
    private SpriteButton clearBtn;
    private SpriteButton pasteBtn;
    private SpriteButton saveBtn;
    private SpriteButton deleteBtn;
    private SpriteButton lockBtn;
    private SpriteButton saveToCardBtn;
    private SpriteButton loadFromCardBtn;
    private SpriteButton closeBtn;


    private boolean locked = false;
    private int volume = 5;

    private boolean lastUrlFocus, lastColorFocus, lastTextFocus;

    // purely visual until real playing state is synced
    private boolean playingSprite = false;

    private boolean debugGui = false; // set false when done

    public RadioScreen(RadioMenu menu, net.minecraft.world.entity.player.Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = GUI_W;
        this.imageHeight = GUI_H;
    }

    @Override
    protected void init() {
        super.init();

        int left = this.leftPos;
        int top = this.topPos;

        // Textboxes
        urlBox = new EditBox(this.font, left + URL_X, top + URL_Y, URL_W, URL_H, Component.literal("Stream URL"));
        urlBox.setMaxLength(2048);
        this.addRenderableWidget(urlBox);

        colorBox = new EditBox(this.font, left + COLOR_X, top + COLOR_Y, COLOR_W, COLOR_H, Component.literal("Screen Color"));
        colorBox.setMaxLength(7);
        this.addRenderableWidget(colorBox);

        textBox = new EditBox(this.font, left + TEXT_X, top + TEXT_Y, TEXT_W, TEXT_H, Component.literal("Screen Text"));
        textBox.setMaxLength(256);
        this.addRenderableWidget(textBox);

        // Initialize from BE snapshot
        urlBox.setValue("");
        colorBox.setValue("");
        textBox.setValue("");

        playBtn = this.addRenderableWidget(new SpriteButton(
                left + 116, top + 61,
                new Sprite(0, 0, 24, 24),
                Component.literal("Play / Stop"),
                () -> {
                    playingSprite = !playingSprite;
                    playBtn.setSprite(playingSprite ? new Sprite(24, 0, 24, 24) : new Sprite(0, 0, 24, 24));
                    sendButton(C2SButtonAction.Action.TOGGLE_PLAY);
                    sendFieldsToServer();
                }
        ));

        volUpBtn = this.addRenderableWidget(new SpriteButton(
                left + 145, top + 26,
                new Sprite(58, 24, 10, 10),
                Component.literal("Volume Up"),
                () -> sendButton(C2SButtonAction.Action.VOL_UP)
        ));

        volDownBtn = this.addRenderableWidget(new SpriteButton(
                left + 103, top + 26,
                new Sprite(48, 24, 10, 10),
                Component.literal("Volume Down"),
                () -> sendButton(C2SButtonAction.Action.VOL_DOWN)
        ));

        clearBtn = this.addRenderableWidget(new SpriteButton(
                left + 66, top + 64,
                new Sprite(48, 0, 48, 8),
                Component.literal("Clear"),
                () -> sendButton(C2SButtonAction.Action.CLEAR_URL)
        ));

        pasteBtn = this.addRenderableWidget(new SpriteButton(
                left + 65, top + 74,
                new Sprite(96, 1, 49, 8),
                Component.literal("Paste"),
                () -> {
                    String clip = Minecraft.getInstance().keyboardHandler.getClipboard();
                    if (clip != null) {
                        urlBox.setValue(clip);
                        sendFieldsToServer();
                    }
                }
        ));

        closeBtn = this.addRenderableWidget(new SpriteButton(
                left + 228, top + 26,
                new Sprite(68, 24, 7, 8),
                Component.literal("Close"),
                () -> Minecraft.getInstance().player.closeContainer()
        ));

        saveBtn = this.addRenderableWidget(new SpriteButton(
                left + 144, top + 66,
                new Sprite(145, 0, 42, 6),
                Component.literal("Save"),
                this::sendFieldsToServer
        ));

        deleteBtn = this.addRenderableWidget(new SpriteButton(
                left + 144, top + 73,
                new Sprite(187, 0, 54, 8),
                Component.literal("Delete"),
                () -> sendFieldsThen(C2SButtonAction.Action.DELETE_SAVED)
        ));

        lockBtn = this.addRenderableWidget(new SpriteButton(
                left + 224, top + 68,
                new Sprite(115, 24, 12, 16),
                Component.literal("Lock / Unlock"),
                () -> sendFieldsThen(C2SButtonAction.Action.TOGGLE_LOCK)
        ));

        saveToCardBtn = this.addRenderableWidget(new SpriteButton(
                left + 33, top + 60,
                new Sprite(127, 24, 8, 8),
                Component.literal("Write to Card"),
                () -> sendFieldsThen(C2SButtonAction.Action.SAVE_TO_CARD)
        ));

        // old y=height/2-4 => rel y=79
        loadFromCardBtn = this.addRenderableWidget(new SpriteButton(
                left + 33, top + 70,
                new Sprite(135, 24, 8, 8),
                Component.literal("Read from Card"),
                () -> sendFieldsThen(C2SButtonAction.Action.LOAD_FROM_CARD)
        ));

        locked = menu.isLocked();
        volume = menu.getVolume();
        refreshLockSprite();
        refreshPlaySprite();
    }

    private void refreshPlaySprite() {
        if (playBtn == null) return;

        boolean isPlaying = false;
        if (menu != null) {
            if (menu.radio != null) {
                isPlaying = menu.radio.isPlaying();
            } else {
                // fallback: keep last known sprite
                isPlaying = playingSprite;
            }
        }

        playingSprite = isPlaying;
        playBtn.setSprite(isPlaying ? new Sprite(24, 0, 24, 24) : new Sprite(0, 0, 24, 24));
    }


    @Override
    protected void containerTick() {
        super.containerTick();

        boolean urlF = urlBox.isFocused();
        boolean colF = colorBox.isFocused();
        boolean txtF = textBox.isFocused();

        // If any field just LOST focus, push the values
        if ((lastUrlFocus && !urlF) || (lastColorFocus && !colF) || (lastTextFocus && !txtF)) {
            sendFieldsToServer();
        }

        lastUrlFocus = urlF;
        lastColorFocus = colF;
        lastTextFocus = txtF;


        // Always read the *live* menu data (it will update once container data sync arrives)
        this.locked = menu.isLocked();
        refreshLockSprite();
        refreshPlaySprite();

        // Keep fields synced from BE, but don't stomp while the user is editing
        if (menu.radio != null) {
            if (!urlBox.isFocused()) {
                String v = menu.radio.getStreamUrl();
                if (!v.equals(urlBox.getValue())) urlBox.setValue(v);
            }
            if (!colorBox.isFocused()) {
                String v = menu.radio.getScreenColor();
                if (!v.equals(colorBox.getValue())) colorBox.setValue(v);
            }
            if (!textBox.isFocused()) {
                String v = menu.radio.getScreenText();
                if (!v.equals(textBox.getValue())) textBox.setValue(v);
            }
        }
    }


    private void refreshLockSprite() {
        if (lockBtn == null) return;
        lockBtn.setSprite(locked ? new Sprite(103, 24, 12, 16) : new Sprite(115, 24, 12, 16));
    }

    private void sendButton(C2SButtonAction.Action action) {
        Net.sendToServer(new C2SButtonAction(menu.pos, action));
    }

    private void sendFieldsThen(C2SButtonAction.Action action) {
        sendFieldsToServer();
        sendButton(action);
    }

    private void sendFieldsToServer() {
        Net.sendToServer(new C2SSetFields(menu.pos, urlBox.getValue(), colorBox.getValue(), textBox.getValue()));
    }

    public void applyServerSync(S2CSyncFields pkt) {
        if (!pkt.pos.equals(menu.pos)) return;

        locked = pkt.locked;
        volume = pkt.volume;

        if (!urlBox.isFocused()) urlBox.setValue(pkt.url);
        if (!colorBox.isFocused()) colorBox.setValue(pkt.screenColor);
        if (!textBox.isFocused()) textBox.setValue(pkt.screenText);

        refreshLockSprite();
        refreshPlaySprite();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // If the user is typing in any text box, do NOT let the inventory key (E) close the screen.
        if (Minecraft.getInstance().options.keyInventory.matches(keyCode, scanCode)) return true;

        // ENTER saves fields
        if (keyCode == 257 || keyCode == 335) {
            sendFieldsToServer();
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        // Normal text entry
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void removed() {
        super.removed();
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        // Draw ONLY the classic 256x166 "panel" portion of the 256x256 sheet.
        g.blit(GUI_TEX, leftPos, topPos, 0, 0, GUI_W, GUI_H, TEX_W, TEX_H);

        if (debugGui) {
            int ox = leftPos;
            int oy = topPos;

            // origin marker
            g.fill(ox - 1, oy - 1, ox + 2, oy + 2, 0xFFFF0000);

            // show container origin coords
            g.drawString(this.font, "leftPos=" + ox + " topPos=" + oy, ox + 4, oy + 4, 0xFFFFFF, false);
            g.fill(leftPos + 45, topPos + 52, leftPos + 47, topPos + 54, 0xFF00FF00); // card slot
            // draw slot markers
            for (int i = 0; i < this.menu.slots.size(); i++) {
                var s = this.menu.slots.get(i);
                int sx = ox + s.x;
                int sy = oy + s.y;
                g.fill(sx, sy, sx + 16, sy + 16, 0x40FF00FF); // translucent magenta box
                g.drawString(this.font, String.valueOf(i), sx + 1, sy + 1, 0xFFFFFF, false);
            }
        }

        // Volume number: put it where the old volumeBox lived.
        g.drawString(this.font, String.valueOf(menu.getVolume()), leftPos + VOL_TXT_X, topPos + VOL_TXT_Y, 0xFFFFFF, false);
        g.drawCenteredString(this.font, Component.literal("OpenFM"),
                leftPos + GUI_W / 2,
                topPos + BRAND_Y,
                getScreenTextColor());
    }

    private int getScreenTextColor() {
        String value = colorBox == null ? "" : colorBox.getValue();
        if (value.startsWith("#")) value = value.substring(1);
        if (!value.matches("(?i)[0-9a-f]{6}")) value = menu.radio.getScreenColor();

        try {
            return Integer.parseInt(value, 16) & 0xFFFFFF;
        } catch (NumberFormatException ignored) {
            return 0x0AFF0A;
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);

        if (debugGui) {
            int rx = mouseX - leftPos;
            int ry = mouseY - topPos;
            g.drawString(this.font, "relMouse=" + rx + "," + ry, leftPos + 4, topPos + 16, 0xFFFFFF, false);
        }

        SpriteButton hovered = findHoveredSpriteButton();
        if (hovered != null) {
            g.renderTooltip(this.font, hovered.tooltip, mouseX, mouseY);
        }

        this.renderTooltip(g, mouseX, mouseY);
    }

    private SpriteButton findHoveredSpriteButton() {
        SpriteButton[] buttons = {
                playBtn, volDownBtn, volUpBtn, clearBtn, pasteBtn, saveBtn, deleteBtn,
                lockBtn, saveToCardBtn, loadFromCardBtn, closeBtn
        };
        for (SpriteButton button : buttons) {
            if (button != null && button.isHovered()) return button;
        }
        return null;
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // no vanilla labels
    }

    private record Sprite(int u, int v, int w, int h) {}

    private static final class SpriteButton extends AbstractWidget {
        private Sprite sprite;
        private final Runnable onPress;
        private final Component tooltip;

        SpriteButton(int x, int y, Sprite sprite, Component tooltip, Runnable onPress) {
            super(x, y, sprite.w(), sprite.h(), Component.empty());
            this.sprite = sprite;
            this.onPress = onPress;
            this.tooltip = tooltip;
        }

        void setSprite(Sprite sprite) {
            this.sprite = sprite;
            this.width = sprite.w();
            this.height = sprite.h();
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            if (this.active) this.onPress.run();
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            boolean hovered = this.isHoveredOrFocused();

            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(hovered ? 1f : 0.9f, hovered ? 1f : 0.9f, hovered ? 1f : 0.9f, 1f);

            g.blit(BTN_TEX, this.getX(), this.getY(), sprite.u(), sprite.v(), sprite.w(), sprite.h(), TEX_W, TEX_H);

            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.disableBlend();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput out) {
            // optional
        }
    }
}
