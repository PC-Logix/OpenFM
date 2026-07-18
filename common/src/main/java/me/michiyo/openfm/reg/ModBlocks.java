package me.michiyo.openfm.reg;

import me.michiyo.openfm.OpenFM;
import me.michiyo.openfm.platform.OpenFMRegistrar;
import me.michiyo.openfm.platform.RegistrySupplier;
import me.michiyo.openfm.world.block.RadioBlock;
import me.michiyo.openfm.world.block.SpeakerBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class ModBlocks {
    public static RegistrySupplier<Block> RADIO_BLOCK;
    public static RegistrySupplier<Block> SPEAKER_BLOCK;

    public static void register(OpenFMRegistrar registrar) {
        RADIO_BLOCK = registrar.registerBlock("radio_block",
                () -> new RadioBlock(properties("radio_block")));
        SPEAKER_BLOCK = registrar.registerBlock("speaker_block",
                () -> new SpeakerBlock(properties("speaker_block")));
    }

    private static BlockBehaviour.Properties properties(String name) {
        ResourceKey<Block> key = ResourceKey.create(
                Registries.BLOCK, Identifier.fromNamespaceAndPath(OpenFM.MODID, name));
        return BlockBehaviour.Properties.of()
                .setId(key)
                .mapColor(MapColor.METAL)
                .strength(3.0F)
                .requiresCorrectToolForDrops();
    }

    private ModBlocks() {}
}

