package me.michiyo.openfm.neoforge;

import me.michiyo.openfm.OpenFM;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(OpenFM.MODID)
public final class OpenFMNeoForge {
    public OpenFMNeoForge(IEventBus modBus, ModContainer modContainer) {
        NeoForgeRegistrar registrar = new NeoForgeRegistrar();
        OpenFM.init(registrar);
        registrar.bind(modBus);

        modContainer.registerConfig(ModConfig.Type.CLIENT, NeoForgeClientConfig.SPEC);
        modBus.addListener(NeoForgeNetwork::register);

        if (ModList.get().isLoaded("computercraft")) {
            modBus.addListener(NeoForgeComputerCraft::register);
        }
        if (ModList.get().isLoaded("opencomputers")) {
            modBus.addListener(NeoForgeOpenComputers::register);
        }
        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForgeClientEvents.register(modBus);
        }
    }
}

