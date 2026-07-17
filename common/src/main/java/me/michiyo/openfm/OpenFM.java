package me.michiyo.openfm;

import me.michiyo.openfm.platform.OpenFMRegistrar;
import me.michiyo.openfm.reg.ModBlockEntities;
import me.michiyo.openfm.reg.ModBlocks;
import me.michiyo.openfm.reg.ModCreativeTabs;
import me.michiyo.openfm.reg.ModItems;
import me.michiyo.openfm.reg.ModMenus;

public final class OpenFM {
    public static final String MODID = "openfm";
    private static boolean initialized;

    public static synchronized void init(OpenFMRegistrar registrar) {
        if (initialized) return;

        ModBlocks.register(registrar);
        ModItems.register(registrar);
        ModBlockEntities.register(registrar);
        ModMenus.register(registrar);
        ModCreativeTabs.register(registrar);
        initialized = true;
    }

    private OpenFM() {}
}

