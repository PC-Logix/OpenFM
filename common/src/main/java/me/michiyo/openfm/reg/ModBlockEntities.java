package me.michiyo.openfm.reg;

import me.michiyo.openfm.platform.OpenFMRegistrar;
import me.michiyo.openfm.platform.RegistrySupplier;
import me.michiyo.openfm.world.be.RadioBlockEntity;
import me.michiyo.openfm.world.be.SpeakerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    public static RegistrySupplier<BlockEntityType<RadioBlockEntity>> RADIO_BE;
    public static RegistrySupplier<BlockEntityType<SpeakerBlockEntity>> SPEAKER_BE;

    public static void register(OpenFMRegistrar registrar) {
        RADIO_BE = registrar.registerBlockEntity("radio_be", RadioBlockEntity::new, ModBlocks.RADIO_BLOCK);
        SPEAKER_BE = registrar.registerBlockEntity("speaker_be", SpeakerBlockEntity::new, ModBlocks.SPEAKER_BLOCK);
    }

    private ModBlockEntities() {}
}
