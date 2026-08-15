package me.michiyo.openfm.fabric;

import me.michiyo.openfm.OpenFM;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import me.michiyo.openfm.net.PortablePlayerSync;

public final class OpenFMFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        OpenFM.init(new FabricRegistrar());
        FabricNetwork.registerServer();
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                PortablePlayerSync.syncAllTo(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                PortablePlayerSync.onDisconnect(handler.player));
        ServerTickEvents.END_SERVER_TICK.register(server ->
                server.getPlayerList().getPlayers().forEach(PortablePlayerSync::tick));

        if (FabricLoader.getInstance().isModLoaded("computercraft")) {
            FabricComputerCraft.register();
        }
    }
}

