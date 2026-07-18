package me.michiyo.openfm.fabric;

import me.michiyo.openfm.net.C2SButtonAction;
import me.michiyo.openfm.net.C2SLinkSpeaker;
import me.michiyo.openfm.net.C2SNowPlaying;
import me.michiyo.openfm.net.C2SSetFields;
import me.michiyo.openfm.net.S2CSyncFields;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

final class FabricNetwork {
    static void registerServer() {
        PayloadTypeRegistry.playC2S().register(C2SButtonAction.TYPE, C2SButtonAction.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2SSetFields.TYPE, C2SSetFields.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2SLinkSpeaker.TYPE, C2SLinkSpeaker.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2SNowPlaying.TYPE, C2SNowPlaying.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2CSyncFields.TYPE, S2CSyncFields.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(C2SButtonAction.TYPE, (message, context) ->
                context.server().execute(() -> C2SButtonAction.handle(message, context.player())));
        ServerPlayNetworking.registerGlobalReceiver(C2SSetFields.TYPE, (message, context) ->
                context.server().execute(() -> C2SSetFields.handle(message, context.player())));
        ServerPlayNetworking.registerGlobalReceiver(C2SLinkSpeaker.TYPE, (message, context) ->
                context.server().execute(() -> C2SLinkSpeaker.handle(message, context.player())));
        ServerPlayNetworking.registerGlobalReceiver(C2SNowPlaying.TYPE, (message, context) ->
                context.server().execute(() -> C2SNowPlaying.handle(message, context.player())));
    }

    static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(S2CSyncFields.TYPE, (message, context) ->
                context.client().execute(() -> S2CSyncFields.handle(message)));
    }

    private FabricNetwork() {}
}

