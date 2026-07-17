package me.michiyo.openfm.reg;

import me.michiyo.openfm.platform.OpenFMRegistrar;
import me.michiyo.openfm.platform.RegistrySupplier;
import me.michiyo.openfm.world.menu.RadioMenu;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {
    public static RegistrySupplier<MenuType<RadioMenu>> RADIO_MENU;

    public static void register(OpenFMRegistrar registrar) {
        RADIO_MENU = registrar.registerRadioMenu("radio_menu");
    }

    private ModMenus() {}
}

