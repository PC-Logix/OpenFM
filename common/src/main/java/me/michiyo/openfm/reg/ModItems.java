package me.michiyo.openfm.reg;

import me.michiyo.openfm.OpenFM;
import me.michiyo.openfm.platform.OpenFMRegistrar;
import me.michiyo.openfm.platform.RegistrySupplier;
import me.michiyo.openfm.world.item.OpenFMTunerItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public final class ModItems {
    public static RegistrySupplier<Item> RADIO_BLOCK_ITEM;
    public static RegistrySupplier<Item> SPEAKER_BLOCK_ITEM;
    public static RegistrySupplier<Item> MEMORY_CARD;
    public static RegistrySupplier<Item> TUNER;

    public static void register(OpenFMRegistrar registrar) {
        RADIO_BLOCK_ITEM = registrar.registerItem("radio_block",
                () -> new BlockItem(ModBlocks.RADIO_BLOCK.get(), properties("radio_block")));
        SPEAKER_BLOCK_ITEM = registrar.registerItem("speaker_block",
                () -> new BlockItem(ModBlocks.SPEAKER_BLOCK.get(), properties("speaker_block")));
        MEMORY_CARD = registrar.registerItem("memory_card",
                () -> new Item(properties("memory_card").stacksTo(1)));
        TUNER = registrar.registerItem("tuner",
                () -> new OpenFMTunerItem(properties("tuner").stacksTo(1)));
    }

    private static Item.Properties properties(String name) {
        ResourceKey<Item> key = ResourceKey.create(
                Registries.ITEM, Identifier.fromNamespaceAndPath(OpenFM.MODID, name));
        return new Item.Properties().setId(key);
    }

    private ModItems() {}
}

