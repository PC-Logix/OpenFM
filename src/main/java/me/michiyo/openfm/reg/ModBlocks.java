package me.michiyo.openfm.reg;

import me.michiyo.openfm.OpenFM;
import me.michiyo.openfm.world.block.RadioBlock;
import me.michiyo.openfm.world.block.SpeakerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks REG = DeferredRegister.createBlocks(OpenFM.MODID);

    public static final DeferredBlock<Block> RADIO_BLOCK = REG.register("radio_block",
            () -> new RadioBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F).requiresCorrectToolForDrops())
    );

    public static final DeferredBlock<Block> SPEAKER_BLOCK = REG.register("speaker_block",
            () -> new SpeakerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F).requiresCorrectToolForDrops())
    );
}
