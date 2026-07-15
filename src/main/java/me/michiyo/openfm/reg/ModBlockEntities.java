package me.michiyo.openfm.reg;

import me.michiyo.openfm.OpenFM;
import me.michiyo.openfm.world.be.RadioBlockEntity;
import me.michiyo.openfm.world.be.SpeakerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REG =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, OpenFM.MODID);

    public static final RegistryObject<BlockEntityType<RadioBlockEntity>> RADIO_BE = REG.register("radio_be",
            () -> BlockEntityType.Builder.of(RadioBlockEntity::new, ModBlocks.RADIO_BLOCK.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<SpeakerBlockEntity>> SPEAKER_BE = REG.register("speaker_be",
            () -> BlockEntityType.Builder.of(SpeakerBlockEntity::new, ModBlocks.SPEAKER_BLOCK.get()).build(null)
    );
}
