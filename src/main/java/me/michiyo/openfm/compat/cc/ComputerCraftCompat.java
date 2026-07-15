package me.michiyo.openfm.compat.cc;

import dan200.computercraft.api.peripheral.IPeripheral;
import me.michiyo.openfm.OpenFM;
import me.michiyo.openfm.world.be.RadioBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.event.AttachCapabilitiesEvent;

import javax.annotation.Nullable;

public final class ComputerCraftCompat {
    private ComputerCraftCompat() {}

    public static final Capability<IPeripheral> CAPABILITY_PERIPHERAL =
            CapabilityManager.get(new CapabilityToken<>() {});

    private static final ResourceLocation KEY =
            new ResourceLocation(OpenFM.MODID, "computercraft_peripheral");

    public static void attachPeripherals(AttachCapabilitiesEvent<BlockEntity> event) {
        BlockEntity be = event.getObject();
        if (!(be instanceof RadioBlockEntity radio)) return;

        PeripheralProvider provider = new PeripheralProvider(() -> new OpenFMRadioPeripheral(radio));
        event.addCapability(KEY, provider);
        event.addListener(provider::invalidate);
    }

    private static final class PeripheralProvider implements ICapabilityProvider {
        private final NonNullSupplier<IPeripheral> factory;
        private @Nullable LazyOptional<IPeripheral> cached;

        private PeripheralProvider(NonNullSupplier<IPeripheral> factory) {
            this.factory = factory;
        }

        private void invalidate() {
            if (cached != null) cached.invalidate();
            cached = null;
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
            if (capability != CAPABILITY_PERIPHERAL) return LazyOptional.empty();

            LazyOptional<IPeripheral> cur = cached;
            if (cur == null) {
                cur = LazyOptional.of(factory);
                cached = cur;
            }
            return cur.cast();
        }
    }
}
