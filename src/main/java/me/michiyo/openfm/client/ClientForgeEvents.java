package me.michiyo.openfm.client;

import me.michiyo.openfm.OpenFM;
import me.michiyo.openfm.client.audio.RadioStreamManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OpenFM.MODID, value = Dist.CLIENT) // FORGE bus (default)
public final class ClientForgeEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        RadioStreamManager.clientTick();
    }

    private ClientForgeEvents() {}
}
