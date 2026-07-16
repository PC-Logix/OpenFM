package me.michiyo.openfm.net;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class Net {
    private static final String PROTOCOL = "1";

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL);
        registrar.playToServer(C2SButtonAction.TYPE, C2SButtonAction.STREAM_CODEC, C2SButtonAction::handle);
        registrar.playToServer(C2SSetFields.TYPE, C2SSetFields.STREAM_CODEC, C2SSetFields::handle);
        registrar.playToServer(C2SLinkSpeaker.TYPE, C2SLinkSpeaker.STREAM_CODEC, C2SLinkSpeaker::handle);
        registrar.playToClient(S2CSyncFields.TYPE, S2CSyncFields.STREAM_CODEC, S2CSyncFields::handle);
    }

    public static void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    private Net() {}
}
