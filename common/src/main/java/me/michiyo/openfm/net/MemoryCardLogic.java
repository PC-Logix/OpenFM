package me.michiyo.openfm.net;

import me.michiyo.openfm.world.be.RadioBlockEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class MemoryCardLogic {
    private static final String ROOT = "OpenFMCard";

    public static void saveToCard(RadioBlockEntity radio) {
        ItemStack card = radio.getInventory().getItem(0);
        if (card.isEmpty()) return;

        CompoundTag root = new CompoundTag();

        root.putString("StreamUrl", radio.getStreamUrl());
        root.putInt("Volume", radio.getVolume());
        root.putString("ScreenColor", radio.getScreenColor());
        root.putString("ScreenText", radio.getScreenText());

        CustomData.update(DataComponents.CUSTOM_DATA, card, tag -> tag.put(ROOT, root));
        radio.setChanged();
    }

    public static void loadFromCard(RadioBlockEntity radio) {
        ItemStack card = radio.getInventory().getItem(0);
        if (card.isEmpty()) return;

        CompoundTag tag = card.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains(ROOT)) return;

        CompoundTag root = tag.getCompound(ROOT).orElseGet(CompoundTag::new);

        radio.setStreamUrl(root.getString("StreamUrl").orElse(""));
        radio.setVolume(root.getInt("Volume").orElse(5));
        radio.setScreenColor(root.getString("ScreenColor").orElse(RadioBlockEntity.DEFAULT_COLOR));
        radio.setScreenText(root.getString("ScreenText").orElse(RadioBlockEntity.DEFAULT_TEXT));
    }

    public static void deleteFromCard(RadioBlockEntity radio) {
        ItemStack card = radio.getInventory().getItem(0);
        if (card.isEmpty()) return;

        CustomData.update(DataComponents.CUSTOM_DATA, card, tag -> tag.remove(ROOT));
        radio.setChanged();
    }

    private MemoryCardLogic() {}
}
