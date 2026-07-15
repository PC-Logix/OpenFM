package me.michiyo.openfm.reg;

import me.michiyo.openfm.OpenFM;
import me.michiyo.openfm.world.block.RadioBlock;
import me.michiyo.openfm.world.block.SpeakerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> REG = DeferredRegister.create(ForgeRegistries.BLOCKS, OpenFM.MODID);

    public static final RegistryObject<Block> RADIO_BLOCK = REG.register("radio_block",
            () -> new RadioBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F).requiresCorrectToolForDrops())
    );

    public static final RegistryObject<Block> SPEAKER_BLOCK = REG.register("speaker_block",
            () -> new SpeakerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F).requiresCorrectToolForDrops())
    );
}
