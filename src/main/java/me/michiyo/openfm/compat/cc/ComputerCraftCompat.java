package me.michiyo.openfm.compat.cc;

import dan200.computercraft.api.peripheral.PeripheralCapability;
import me.michiyo.openfm.reg.ModBlockEntities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class ComputerCraftCompat {
    private ComputerCraftCompat() {}

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                PeripheralCapability.get(),
                ModBlockEntities.RADIO_BE.get(),
                (radio, side) -> new OpenFMRadioPeripheral(radio)
        );
    }
}
