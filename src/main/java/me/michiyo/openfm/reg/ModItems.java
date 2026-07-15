package me.michiyo.openfm.reg;

import me.michiyo.openfm.OpenFM;
import me.michiyo.openfm.world.item.OpenFMTunerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> REG = DeferredRegister.create(ForgeRegistries.ITEMS, OpenFM.MODID);

    public static final RegistryObject<Item> RADIO_BLOCK_ITEM = REG.register("radio_block",
            () -> new BlockItem(ModBlocks.RADIO_BLOCK.get(), new Item.Properties())
    );

    public static final RegistryObject<Item> SPEAKER_BLOCK_ITEM = REG.register("speaker_block",
            () -> new BlockItem(ModBlocks.SPEAKER_BLOCK.get(), new Item.Properties())
    );

    public static final RegistryObject<Item> MEMORY_CARD = REG.register("memory_card",
            () -> new Item(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<Item> TUNER = REG.register("tuner",
           () -> new OpenFMTunerItem(new Item.Properties().stacksTo(1))
    );
}
