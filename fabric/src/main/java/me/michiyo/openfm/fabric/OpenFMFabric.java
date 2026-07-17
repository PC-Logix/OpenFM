package me.michiyo.openfm.fabric;

import me.michiyo.openfm.OpenFM;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class OpenFMFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        OpenFM.init(new FabricRegistrar());
        FabricNetwork.registerServer();

        if (FabricLoader.getInstance().isModLoaded("computercraft")) {
            FabricComputerCraft.register();
        }
    }
}

