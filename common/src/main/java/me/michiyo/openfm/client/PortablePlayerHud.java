package me.michiyo.openfm.client;

import me.michiyo.openfm.client.audio.PortableStreamManager;
import me.michiyo.openfm.world.item.PortablePlayerItem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class PortablePlayerHud {
    // Leave room for vanilla's selected-item label at height - 59.
    private static final int TITLE_Y_FROM_BOTTOM = 68;
    private static final int MAX_TITLE_WIDTH = 220;
    private static final int SCROLL_SPEED_PIXELS_PER_SECOND = 30;
    private static final int SCROLL_PAUSE_MILLIS = 1200;

    public static void render(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return;

        ItemStack held = PortablePlayerItem.findHeldStack(minecraft.player);
        if (held.isEmpty() || !PortablePlayerItem.isPlaying(held)) return;

        String title = PortableStreamManager.getNowPlaying(minecraft.player.getUUID());
        if (title == null || title.isBlank()) return;

        int maxWidth = Math.max(1, Math.min(MAX_TITLE_WIDTH, graphics.guiWidth() - 20));
        String display = title.trim();
        int titleWidth = minecraft.font.width(display);
        int titleY = graphics.guiHeight() - TITLE_Y_FROM_BOTTOM;

        if (titleWidth <= maxWidth) {
            graphics.drawCenteredString(minecraft.font, Component.literal(display),
                    graphics.guiWidth() / 2, titleY, 0xFFFFFF);
            return;
        }

        int overflow = titleWidth - maxWidth;
        long scrollMillis = (long) overflow * 1000L / SCROLL_SPEED_PIXELS_PER_SECOND;
        long cycleMillis = SCROLL_PAUSE_MILLIS * 2L + scrollMillis;
        long phase = Util.getMillis() % cycleMillis;
        int offset;
        if (phase < SCROLL_PAUSE_MILLIS) {
            offset = 0;
        } else if (phase < SCROLL_PAUSE_MILLIS + scrollMillis) {
            offset = (int) ((phase - SCROLL_PAUSE_MILLIS)
                    * SCROLL_SPEED_PIXELS_PER_SECOND / 1000L);
        } else {
            offset = overflow;
        }

        int left = (graphics.guiWidth() - maxWidth) / 2;
        graphics.enableScissor(left, titleY, left + maxWidth, titleY + minecraft.font.lineHeight);
        try {
            graphics.drawString(minecraft.font, display, left - offset, titleY, 0xFFFFFF);
        } finally {
            graphics.disableScissor();
        }
    }

    private PortablePlayerHud() {}
}
