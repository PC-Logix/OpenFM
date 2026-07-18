package me.michiyo.openfm.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class TexturedButton extends Button {
    private final Identifier tex;
    private final int u;
    private final int v;
    private final int hoverVOffset;
    private final int texW;
    private final int texH;

    public TexturedButton(
            int x, int y, int width, int height,
            int u, int v, int hoverVOffset,
            Identifier tex, int texW, int texH,
            OnPress onPress
    ) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.tex = tex;
        this.u = u;
        this.v = v;
        this.hoverVOffset = hoverVOffset;
        this.texW = texW;
        this.texH = texH;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        int drawV = this.v + (this.isHoveredOrFocused() ? this.hoverVOffset : 0);
        g.blit(RenderPipelines.GUI_TEXTURED, this.tex, this.getX(), this.getY(), this.u, drawV, this.width, this.height, this.texW, this.texH);
    }
}
