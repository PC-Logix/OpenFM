package me.michiyo.openfm.neoforge;

import me.michiyo.openfm.OpenFM;
import me.michiyo.openfm.net.PortablePlayerSync;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.minecraft.server.level.ServerPlayer;

@Mod(OpenFM.MODID)
public final class OpenFMNeoForge {
    public OpenFMNeoForge(IEventBus modBus, ModContainer modContainer) {
        NeoForgeRegistrar registrar = new NeoForgeRegistrar();
        OpenFM.init(registrar);
        registrar.bind(modBus);

        modContainer.registerConfig(ModConfig.Type.CLIENT, NeoForgeClientConfig.SPEC);
        modBus.addListener(NeoForgeNetwork::register);
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) PortablePlayerSync.syncAllTo(player);
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) PortablePlayerSync.onDisconnect(player);
        });
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) ->
                event.getServer().getPlayerList().getPlayers().forEach(PortablePlayerSync::tick));

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

