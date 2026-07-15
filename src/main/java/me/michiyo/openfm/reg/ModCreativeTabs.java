package me.michiyo.openfm.reg;

import me.michiyo.openfm.OpenFM;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> REG =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, OpenFM.MODID);

    public static final RegistryObject<CreativeModeTab> OPENFM_TAB = REG.register("openfm", () ->
            CreativeModeTab.builder()
                    .title(Component.literal("OpenFM"))
                    .icon(() -> new ItemStack(ModItems.RADIO_BLOCK_ITEM.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.RADIO_BLOCK_ITEM.get());
                        output.accept(ModItems.SPEAKER_BLOCK_ITEM.get());
                        output.accept(ModItems.TUNER.get());
                    })
                    .build()
    );
}
