package me.michiyo.openfm.net;

import me.michiyo.openfm.world.be.RadioBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class MemoryCardLogic {
    private static final String ROOT = "OpenFMCard";

    public static void saveToCard(RadioBlockEntity radio) {
        ItemStack card = radio.getInventory().getItem(0);
        if (card.isEmpty()) return;

        CompoundTag tag = card.getOrCreateTag();
        CompoundTag root = new CompoundTag();

        root.putString("StreamUrl", radio.getStreamUrl());
        root.putInt("Volume", radio.getVolume());
        root.putString("ScreenColor", radio.getScreenColor());
        root.putString("ScreenText", radio.getScreenText());

        tag.put(ROOT, root);
        card.setTag(tag);
        radio.setChanged();
    }

    public static void loadFromCard(RadioBlockEntity radio) {
        ItemStack card = radio.getInventory().getItem(0);
        if (card.isEmpty() || !card.hasTag()) return;

        CompoundTag tag = card.getTag();
        if (tag == null || !tag.contains(ROOT)) return;

        CompoundTag root = tag.getCompound(ROOT);

        radio.setStreamUrl(root.getString("StreamUrl"));
        radio.setVolume(root.getInt("Volume"));
        radio.setScreenColor(root.getString("ScreenColor"));
        radio.setScreenText(root.getString("ScreenText"));
    }

    public static void deleteFromCard(RadioBlockEntity radio) {
        ItemStack card = radio.getInventory().getItem(0);
        if (card.isEmpty() || !card.hasTag()) return;

        CompoundTag tag = card.getTag();
        if (tag == null) return;

        tag.remove(ROOT);
        card.setTag(tag);
        radio.setChanged();
    }

    private MemoryCardLogic() {}
}
