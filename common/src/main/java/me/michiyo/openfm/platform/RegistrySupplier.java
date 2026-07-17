package me.michiyo.openfm.platform;

import java.util.function.Supplier;

@FunctionalInterface
public interface RegistrySupplier<T> extends Supplier<T> {
}

