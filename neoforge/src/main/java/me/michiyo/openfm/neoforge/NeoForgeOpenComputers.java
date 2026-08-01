package me.michiyo.openfm.neoforge;

import li.cil.oc.api.Driver;
import me.michiyo.openfm.compat.oc.OpenFMRadioDriver;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

final class NeoForgeOpenComputers {
    static void register(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> Driver.add(new OpenFMRadioDriver()));
    }

    private NeoForgeOpenComputers() {}
}
