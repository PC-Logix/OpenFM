package me.michiyo.openfm;

import me.michiyo.openfm.config.OpenFMClientConfig;
import me.michiyo.openfm.net.Net;
import me.michiyo.openfm.reg.ModBlockEntities;
import me.michiyo.openfm.reg.ModBlocks;
import me.michiyo.openfm.reg.ModItems;
import me.michiyo.openfm.reg.ModMenus;
import me.michiyo.openfm.reg.ModCreativeTabs;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ModConfig;

@Mod(OpenFM.MODID)
public class OpenFM {
    public static final String MODID = "openfm";

    public OpenFM(IEventBus bus, ModContainer modContainer) {
        ModBlocks.REG.register(bus);
        ModItems.REG.register(bus);
        ModBlockEntities.REG.register(bus);
        ModMenus.REG.register(bus);
        ModCreativeTabs.REG.register(bus);

        modContainer.registerConfig(ModConfig.Type.CLIENT, OpenFMClientConfig.SPEC);

        bus.addListener(Net::register);

        if (ModList.get().isLoaded("computercraft")) {
            bus.addListener(me.michiyo.openfm.compat.cc.ComputerCraftCompat::registerCapabilities);
        }
    }
}
