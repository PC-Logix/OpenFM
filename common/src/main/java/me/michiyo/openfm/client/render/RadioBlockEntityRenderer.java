package me.michiyo.openfm.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.michiyo.openfm.world.be.RadioBlockEntity;
import me.michiyo.openfm.client.audio.RadioStreamManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RadioBlockEntityRenderer implements BlockEntityRenderer<RadioBlockEntity, RadioBlockEntityRenderer.State> {
    private final Font font;

    public RadioBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.font = ctx.font();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(
            RadioBlockEntity be,
            State state,
            float partialTick,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTick, cameraPosition, breakProgress);
        String text = RadioStreamManager.getNowPlaying(be.getBlockPos());
        if (text == null || text.trim().isEmpty()) {
            text = be.getScreenText();
        }
        if (text == null || text.trim().isEmpty()) text = "OpenFM";
        text = text.trim().replace("\r", " ").replace("\n", " ");
        if (text.codePointCount(0, text.length()) > 128) {
            text = text.substring(0, text.offsetByCodePoints(0, 128));
        }

        state.text = text;
        state.argb = parseHexColorToARGB(be.getScreenColor());
        state.animationTime = (be.getLevel() != null ? be.getLevel().getGameTime() : 0) + partialTick;

        BlockState blockState = be.getBlockState();

        Direction facing = Direction.NORTH;
        if (blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            facing = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        } else if (blockState.hasProperty(BlockStateProperties.FACING)) {
            facing = blockState.getValue(BlockStateProperties.FACING);
        }
        state.facing = facing;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();

        // Center
        poseStack.translate(0.5D, 0.5D, 0.5D);

        // Rotate with the block
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot()));

        // Your tuned placement (block-space)
        poseStack.translate(0.016D, 0.140D, 0.51D);

        // Your tuned scale
        float scale = 0.017f;
        poseStack.scale(scale, -scale, scale);

        // "Screen" bounds in FONT PIXELS (tune screenW to match your green area)
        final float screenW = 37.5f;
        final float screenX = -screenW / 2.0f; // left edge
        final float y = -this.font.lineHeight / 2.0f;

        String drawText = state.text;
        float x = screenX;

        int textW = this.font.width(state.text);
        if (textW <= screenW) {
            // If it fits, center it (or set x=screenX if you want left-aligned always)
            x = -textW / 2.0f;
        } else {
            // Pixel-smooth marquee: repeat with a gap
            final String gap = "   "; // adjust gap length if you want
            final String marquee = state.text + gap + state.text;

            int singleCyclePx = this.font.width(state.text + gap); // how far until it repeats

            // pixels per tick (20 ticks/sec). 0.6 = 12 px/sec
            float speed = 0.6f;
            float offsetPx = (state.animationTime * speed) % singleCyclePx;

            // Build only the visible slice that fits into screenW
            drawText = sliceByPixelWidth(this.font, marquee, offsetPx, (int) screenW);

            // Always draw starting at left edge of screen
            x = screenX;
        }

        submitNodeCollector.submitText(
                poseStack,
                x,
                y,
                Component.literal(drawText).getVisualOrderText(),
                false,
                Font.DisplayMode.NORMAL,
                15728880,
                state.argb,
                0,
                0
        );

        poseStack.popPose();
    }

    private static String sanitize(String s) {
        s = s.trim();
        s = s.replace("\r", " ").replace("\n", " ");
        return s;
    }

    /**
     * Returns a substring of `s` that begins at pixel offset `startPx` and fits within `maxPx`.
     * This prevents any rendering past the screen width without needing scissor.
     */
    private static String sliceByPixelWidth(Font font, String s, float startPx, int maxPx) {
        if (s == null || s.isEmpty()) return "";

        // Find the start index such that width(s[0..i]) <= startPx < width(s[0..i+1])
        int startIdx = 0;
        float acc = 0.0f;

        while (startIdx < s.length()) {
            int codePoint = s.codePointAt(startIdx);
            String ch = new String(Character.toChars(codePoint));
            int w = font.width(ch);
            if (acc + w > startPx) break;
            acc += w;
            startIdx += Character.charCount(codePoint);
        }

        if (startIdx >= s.length()) return "";

        // Now take as many characters as fit in maxPx
        StringBuilder out = new StringBuilder();
        int used = 0;

        for (int i = startIdx; i < s.length();) {
            int codePoint = s.codePointAt(i);
            String ch = new String(Character.toChars(codePoint));
            int w = font.width(ch);
            if (used + w > maxPx) break;
            out.append(ch);
            used += w;
            i += Character.charCount(codePoint);
        }

        return out.toString();
    }

    private static int parseHexColorToARGB(String hex) {
        if (hex == null) return 0xFFFFFFFF;
        hex = hex.trim();
        if (hex.startsWith("#")) hex = hex.substring(1);
        if (hex.startsWith("0x") || hex.startsWith("0X")) hex = hex.substring(2);

        if (hex.length() != 6) return 0xFFFFFFFF;

        try {
            int rgb = Integer.parseInt(hex, 16) & 0xFFFFFF;
            return 0xFF000000 | rgb;
        } catch (Exception ignored) {
            return 0xFFFFFFFF;
        }
    }

    public static final class State extends BlockEntityRenderState {
        String text = "OpenFM";
        int argb = 0xFFFFFFFF;
        float animationTime;
        Direction facing = Direction.NORTH;
    }
}
