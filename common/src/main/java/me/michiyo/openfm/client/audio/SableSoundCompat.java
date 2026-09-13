package me.michiyo.openfm.client.audio;

import dev.ryanhcode.sable.companion.SableCompanion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Converts Sable's logical sublevel coordinates to physical world coordinates.
 * Sable Companion supplies an identity implementation when Sable is absent.
 */
final class SableSoundCompat {
    private SableSoundCompat() {}

    static Vec3 physicalPosition(Level level, Vec3 position) {
        if (level == null || position == null) return position;
        return SableCompanion.INSTANCE.projectOutOfSubLevel(level, position);
    }
}
