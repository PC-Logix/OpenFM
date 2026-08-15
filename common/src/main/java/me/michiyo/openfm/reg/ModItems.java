package me.michiyo.openfm.reg;

import me.michiyo.openfm.platform.OpenFMRegistrar;
import me.michiyo.openfm.platform.RegistrySupplier;
import me.michiyo.openfm.world.item.OpenFMTunerItem;
import me.michiyo.openfm.world.item.HeadphonesItem;
import me.michiyo.openfm.world.item.PortablePlayerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public final class ModItems {
    public static RegistrySupplier<Item> RADIO_BLOCK_ITEM;
    public static RegistrySupplier<Item> SPEAKER_BLOCK_ITEM;
    public static RegistrySupplier<Item> MEMORY_CARD;
    public static RegistrySupplier<Item> TUNER;
    public static RegistrySupplier<Item> PORTABLE_PLAYER;
    public static RegistrySupplier<Item> HEADPHONES;

    public static void register(OpenFMRegistrar registrar) {
        RADIO_BLOCK_ITEM = registrar.registerItem("radio_block",
                () -> new BlockItem(ModBlocks.RADIO_BLOCK.get(), new Item.Properties()));
        SPEAKER_BLOCK_ITEM = registrar.registerItem("speaker_block",
                () -> new BlockItem(ModBlocks.SPEAKER_BLOCK.get(), new Item.Properties()));
        MEMORY_CARD = registrar.registerItem("memory_card",
                () -> new Item(new Item.Properties().stacksTo(1)));
        TUNER = registrar.registerItem("tuner",
                () -> new OpenFMTunerItem(new Item.Properties().stacksTo(1)));
        PORTABLE_PLAYER = registrar.registerItem("portable_player",
                () -> new PortablePlayerItem(new Item.Properties()));
        HEADPHONES = registrar.registerItem("headphones",
                () -> new HeadphonesItem(new Item.Properties()));
    }

    private ModItems() {}
}

