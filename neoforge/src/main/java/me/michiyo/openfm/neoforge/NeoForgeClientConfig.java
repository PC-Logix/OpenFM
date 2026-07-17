package me.michiyo.openfm.neoforge;

import net.neoforged.neoforge.common.ModConfigSpec;

final class NeoForgeClientConfig {
    static final ModConfigSpec SPEC;
    static final ModConfigSpec.DoubleValue RADIO_VOLUME;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("audio");
        RADIO_VOLUME = builder
                .comment("Per-client OpenFM radio volume multiplier.", "0.0 = mute, 1.0 = full volume.")
                .defineInRange("radioVolume", 1.0d, 0.0d, 1.0d);
        builder.pop();
        SPEC = builder.build();
    }

    private NeoForgeClientConfig() {}
}

