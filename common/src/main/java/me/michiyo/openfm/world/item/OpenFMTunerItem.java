package me.michiyo.openfm.world.item;

import me.michiyo.openfm.net.C2SLinkSpeaker;
import me.michiyo.openfm.net.Net;
import me.michiyo.openfm.world.be.RadioBlockEntity;
import me.michiyo.openfm.world.be.SpeakerBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class OpenFMTunerItem extends Item {
    private static final String NBT_SPK_DIM = "openfm:speaker_dim";
    private static final String NBT_SPK_POS = "openfm:speaker_pos";

    public OpenFMTunerItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos clicked = ctx.getClickedPos();
        ItemStack stack = ctx.getItemInHand();

        if (level.getBlockEntity(clicked) instanceof SpeakerBlockEntity) {
            // Shift+click speaker = unlink
            if (ctx.getPlayer() != null && ctx.getPlayer().isShiftKeyDown()) {
                // send packet client-side
                if (level.isClientSide()) {
                    ResourceKey<Level> dim = level.dimension();
                    Net.sendToServer(C2SLinkSpeaker.unlink(dim, clicked));
                }

                clearSpeakerSelection(stack);

                if (level.isClientSide() && ctx.getPlayer() != null) {
                    ctx.getPlayer().sendOverlayMessage(
                            Component.literal("Speaker unlinked.").withStyle(ChatFormatting.GRAY));
                }

                return success(level);
            }

            // Normal click speaker = select it in the tuner
            setSpeakerSelection(stack, level.dimension(), clicked);

            if (level.isClientSide() && ctx.getPlayer() != null) {
                ctx.getPlayer().sendOverlayMessage(
                        Component.literal("Speaker selected. Now click a Radio.").withStyle(ChatFormatting.GRAY));
            }

            return success(level);
        }

        if (level.getBlockEntity(clicked) instanceof RadioBlockEntity) {
            // click radio with tuner that has a selected speaker -> link
            SpeakerSel sel = getSpeakerSelection(stack);
            if (sel == null) {
                if (level.isClientSide() && ctx.getPlayer() != null) {
                    ctx.getPlayer().sendOverlayMessage(
                            Component.literal("No speaker selected. Click a Speaker first.").withStyle(ChatFormatting.RED));
                }
                return success(level);
            }

            // send packet client-side
            if (level.isClientSide()) {
                Net.sendToServer(C2SLinkSpeaker.link(sel.dim, sel.pos, level.dimension(), clicked));
            }

            clearSpeakerSelection(stack);

            if (level.isClientSide() && ctx.getPlayer() != null) {
                ctx.getPlayer().sendOverlayMessage(
                        Component.literal("Linked speaker to radio.").withStyle(ChatFormatting.GREEN));
            }

            return success(level);
        }

        return InteractionResult.PASS;
    }

    private static void setSpeakerSelection(ItemStack stack, ResourceKey<Level> dim, BlockPos pos) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putString(NBT_SPK_DIM, dim.identifier().toString());
            tag.putLong(NBT_SPK_POS, pos.asLong());
        });
    }

    private static void clearSpeakerSelection(ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.remove(NBT_SPK_DIM);
            tag.remove(NBT_SPK_POS);
        });
    }

    private static SpeakerSel getSpeakerSelection(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains(NBT_SPK_DIM) || !tag.contains(NBT_SPK_POS)) return null;

        String dimStr = tag.getString(NBT_SPK_DIM).orElse("");
        if (dimStr.isEmpty()) return null;

        ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, Identifier.parse(dimStr));
        BlockPos pos = BlockPos.of(tag.getLong(NBT_SPK_POS).orElse(0L));
        return new SpeakerSel(dim, pos);
    }

    private static InteractionResult success(Level level) {
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    private record SpeakerSel(ResourceKey<Level> dim, BlockPos pos) {}
}
