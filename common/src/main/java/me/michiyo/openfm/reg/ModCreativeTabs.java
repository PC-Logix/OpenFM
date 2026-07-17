package me.michiyo.openfm.reg;

import me.michiyo.openfm.platform.OpenFMRegistrar;
import me.michiyo.openfm.platform.RegistrySupplier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModCreativeTabs {
    public static RegistrySupplier<CreativeModeTab> OPENFM_TAB;

    public static void register(OpenFMRegistrar registrar) {
        OPENFM_TAB = registrar.registerCreativeTab(
                "openfm",
                Component.literal("OpenFM"),
                () -> new ItemStack(ModItems.RADIO_BLOCK_ITEM.get()),
                (params, output) -> {
                    output.accept(ModItems.RADIO_BLOCK_ITEM.get());
                    output.accept(ModItems.SPEAKER_BLOCK_ITEM.get());
                    output.accept(ModItems.MEMORY_CARD.get());
                    output.accept(ModItems.TUNER.get());
                });
    }

    private ModCreativeTabs() {}
}
