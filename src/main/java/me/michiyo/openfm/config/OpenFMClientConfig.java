package me.michiyo.openfm.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class OpenFMClientConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.DoubleValue RADIO_VOLUME; // 0.0..1.0

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.push("audio");

        RADIO_VOLUME = b
                .comment("Per-client OpenFM radio volume multiplier (independent of Minecraft Music).",
                        "Applied on top of block volume + distance attenuation.",
                        "0.0 = mute, 1.0 = full volume.")
                .defineInRange("radioVolume", 1.0d, 0.0d, 1.0d);

        b.pop();

        SPEC = b.build();
    }

    private OpenFMClientConfig() {}
}
