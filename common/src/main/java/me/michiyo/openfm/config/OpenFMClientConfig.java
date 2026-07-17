package me.michiyo.openfm.config;

import me.michiyo.openfm.platform.Services;

public final class OpenFMClientConfig {
    public static final VolumeValue RADIO_VOLUME = new VolumeValue();

    public static final class VolumeValue {
        public double get() {
            return Services.PLATFORM.getRadioVolume();
        }

        public void set(double value) {
            Services.PLATFORM.setRadioVolume(Math.max(0.0, Math.min(1.0, value)));
        }
    }

    private OpenFMClientConfig() {}
}

