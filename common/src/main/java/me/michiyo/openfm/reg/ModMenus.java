package me.michiyo.openfm.reg;

import me.michiyo.openfm.platform.OpenFMRegistrar;
import me.michiyo.openfm.platform.RegistrySupplier;
import me.michiyo.openfm.world.menu.RadioMenu;
import net.minecraft.world.inventory.MenuType;
import me.michiyo.openfm.world.menu.PortablePlayerMenu;

public final class ModMenus {
    public static RegistrySupplier<MenuType<RadioMenu>> RADIO_MENU;
    public static RegistrySupplier<MenuType<PortablePlayerMenu>> PORTABLE_MENU;

    public static void register(OpenFMRegistrar registrar) {
        RADIO_MENU = registrar.registerRadioMenu("radio_menu");
        PORTABLE_MENU = registrar.registerPortableMenu("portable_menu");
    }

    private ModMenus() {}
}

