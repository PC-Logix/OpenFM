package me.michiyo.openfm.reg;

import me.michiyo.openfm.OpenFM;
import me.michiyo.openfm.world.menu.RadioMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> REG =
            DeferredRegister.create(Registries.MENU, OpenFM.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<RadioMenu>> RADIO_MENU =
            REG.register("radio_menu", () -> IMenuTypeExtension.create(RadioMenu::new));
}
