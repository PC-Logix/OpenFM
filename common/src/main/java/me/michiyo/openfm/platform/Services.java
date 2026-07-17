package me.michiyo.openfm.platform;

import java.util.ServiceLoader;

public final class Services {
    public static final OpenFMPlatform PLATFORM = ServiceLoader.load(OpenFMPlatform.class)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No OpenFM platform service was found"));

    private Services() {}
}

