package me.michiyo.openfm.fabric;

import dan200.computercraft.api.peripheral.PeripheralLookup;
import me.michiyo.openfm.compat.cc.OpenFMRadioPeripheral;
import me.michiyo.openfm.reg.ModBlockEntities;

final class FabricComputerCraft {
    static void register() {
        PeripheralLookup.get().registerForBlockEntity(
                (radio, side) -> new OpenFMRadioPeripheral(radio), ModBlockEntities.RADIO_BE.get());
    }

    private FabricComputerCraft() {}
}

