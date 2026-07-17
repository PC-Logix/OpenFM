package me.michiyo.openfm.reg;

import me.michiyo.openfm.platform.OpenFMRegistrar;
import me.michiyo.openfm.platform.RegistrySupplier;
import me.michiyo.openfm.world.block.RadioBlock;
import me.michiyo.openfm.world.block.SpeakerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class ModBlocks {
    public static RegistrySupplier<Block> RADIO_BLOCK;
    public static RegistrySupplier<Block> SPEAKER_BLOCK;

    public static void register(OpenFMRegistrar registrar) {
        RADIO_BLOCK = registrar.registerBlock("radio_block",
                () -> new RadioBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F).requiresCorrectToolForDrops()));
        SPEAKER_BLOCK = registrar.registerBlock("speaker_block",
                () -> new SpeakerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F).requiresCorrectToolForDrops()));
    }

    private ModBlocks() {}
}

