package me.michiyo.openfm.reg;

import me.michiyo.openfm.OpenFM;
import me.michiyo.openfm.world.be.RadioBlockEntity;
import me.michiyo.openfm.world.be.SpeakerBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REG =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, OpenFM.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RadioBlockEntity>> RADIO_BE = REG.register("radio_be",
            () -> BlockEntityType.Builder.of(RadioBlockEntity::new, ModBlocks.RADIO_BLOCK.get()).build(null)
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SpeakerBlockEntity>> SPEAKER_BE = REG.register("speaker_be",
            () -> BlockEntityType.Builder.of(SpeakerBlockEntity::new, ModBlocks.SPEAKER_BLOCK.get()).build(null)
    );
}
