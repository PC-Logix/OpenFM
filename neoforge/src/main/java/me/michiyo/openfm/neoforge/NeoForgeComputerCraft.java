package me.michiyo.openfm.neoforge;

import dan200.computercraft.api.peripheral.PeripheralCapability;
import me.michiyo.openfm.compat.cc.OpenFMRadioPeripheral;
import me.michiyo.openfm.reg.ModBlockEntities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

final class NeoForgeComputerCraft {
    static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(PeripheralCapability.get(), ModBlockEntities.RADIO_BE.get(),
                (radio, side) -> new OpenFMRadioPeripheral(radio));
    }

    private NeoForgeComputerCraft() {}
}

