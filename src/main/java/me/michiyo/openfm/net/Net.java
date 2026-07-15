package me.michiyo.openfm.net;

import me.michiyo.openfm.OpenFM;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class Net {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CH = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(OpenFM.MODID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private static int id = 0;

    public static void register() {
        // C2S
        CH.messageBuilder(C2SButtonAction.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SButtonAction::encode)
                .decoder(C2SButtonAction::decode)
                .consumerMainThread(C2SButtonAction::handle)
                .add();

        CH.messageBuilder(C2SSetFields.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SSetFields::encode)
                .decoder(C2SSetFields::decode)
                .consumerMainThread(C2SSetFields::handle)
                .add();

        // NEW: Speaker link/unlink
        CH.messageBuilder(C2SLinkSpeaker.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SLinkSpeaker::encode)
                .decoder(C2SLinkSpeaker::decode)
                .consumerMainThread(C2SLinkSpeaker::handle)
                .add();

        // S2C (THIS is the one that makes your color/text/url sync work)
        CH.messageBuilder(S2CSyncFields.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CSyncFields::encode)
                .decoder(S2CSyncFields::decode)
                .consumerMainThread(S2CSyncFields::handle)
                .add();
    }

    private Net() {}
}
