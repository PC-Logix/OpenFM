package me.michiyo.openfm.neoforge;

import me.michiyo.openfm.net.C2SButtonAction;
import me.michiyo.openfm.net.C2SLinkSpeaker;
import me.michiyo.openfm.net.C2SSetFields;
import me.michiyo.openfm.net.S2CSyncFields;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

final class NeoForgeNetwork {
    static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToServer(C2SButtonAction.TYPE, C2SButtonAction.STREAM_CODEC,
                (message, context) -> context.enqueueWork(() ->
                        C2SButtonAction.handle(message, (ServerPlayer) context.player())));
        registrar.playToServer(C2SSetFields.TYPE, C2SSetFields.STREAM_CODEC,
                (message, context) -> context.enqueueWork(() ->
                        C2SSetFields.handle(message, (ServerPlayer) context.player())));
        registrar.playToServer(C2SLinkSpeaker.TYPE, C2SLinkSpeaker.STREAM_CODEC,
                (message, context) -> context.enqueueWork(() ->
                        C2SLinkSpeaker.handle(message, (ServerPlayer) context.player())));
        registrar.playToClient(S2CSyncFields.TYPE, S2CSyncFields.STREAM_CODEC,
                (message, context) -> context.enqueueWork(() -> S2CSyncFields.handle(message)));
    }

    private NeoForgeNetwork() {}
}

