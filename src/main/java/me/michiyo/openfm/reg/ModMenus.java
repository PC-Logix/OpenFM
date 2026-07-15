package me.michiyo.openfm.reg;

import me.michiyo.openfm.OpenFM;
import me.michiyo.openfm.world.menu.RadioMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> REG =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, OpenFM.MODID);

    public static final RegistryObject<MenuType<RadioMenu>> RADIO_MENU =
            REG.register("radio_menu", () -> IForgeMenuType.create(RadioMenu::new));
}
