package me.michiyo.openfm.reg;

import me.michiyo.openfm.platform.OpenFMRegistrar;
import me.michiyo.openfm.platform.RegistrySupplier;
import me.michiyo.openfm.world.item.OpenFMTunerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public final class ModItems {
    public static RegistrySupplier<Item> RADIO_BLOCK_ITEM;
    public static RegistrySupplier<Item> SPEAKER_BLOCK_ITEM;
    public static RegistrySupplier<Item> MEMORY_CARD;
    public static RegistrySupplier<Item> TUNER;

    public static void register(OpenFMRegistrar registrar) {
        RADIO_BLOCK_ITEM = registrar.registerItem("radio_block",
                () -> new BlockItem(ModBlocks.RADIO_BLOCK.get(), new Item.Properties()));
        SPEAKER_BLOCK_ITEM = registrar.registerItem("speaker_block",
                () -> new BlockItem(ModBlocks.SPEAKER_BLOCK.get(), new Item.Properties()));
        MEMORY_CARD = registrar.registerItem("memory_card",
                () -> new Item(new Item.Properties().stacksTo(1)));
        TUNER = registrar.registerItem("tuner",
                () -> new OpenFMTunerItem(new Item.Properties().stacksTo(1)));
    }

    private ModItems() {}
}

