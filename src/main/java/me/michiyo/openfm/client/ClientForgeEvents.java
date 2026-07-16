package me.michiyo.openfm.client;

import me.michiyo.openfm.OpenFM;
import me.michiyo.openfm.client.audio.RadioStreamManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = OpenFM.MODID, value = Dist.CLIENT)
public final class ClientForgeEvents {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post e) {
        RadioStreamManager.clientTick();
    }

    private ClientForgeEvents() {}
}
