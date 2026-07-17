package me.michiyo.openfm.compat.cc;

import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public final class CCEvents {
    private CCEvents() {}

    private record Key(ResourceKey<Level> dim, BlockPos pos) {
        Key {
            pos = pos.immutable();
        }
    }

    private static final ConcurrentHashMap<Key, Set<IComputerAccess>> COMPUTERS = new ConcurrentHashMap<>();

    public static void register(ServerLevel level, BlockPos pos, IComputerAccess computer) {
        Key key = new Key(level.dimension(), pos);
        COMPUTERS.computeIfAbsent(key, k -> new CopyOnWriteArraySet<>()).add(computer);
    }

    public static void unregister(ServerLevel level, BlockPos pos, IComputerAccess computer) {
        Key key = new Key(level.dimension(), pos);
        Set<IComputerAccess> set = COMPUTERS.get(key);
        if (set == null) return;

        set.remove(computer);
        if (set.isEmpty()) COMPUTERS.remove(key);
    }

    public static void unregisterAll(ServerLevel level, BlockPos pos) {
        Key key = new Key(level.dimension(), pos);
        COMPUTERS.remove(key);
    }

    public static void queue(ServerLevel level, BlockPos pos, String event, Object... args) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(pos, "pos");
        Objects.requireNonNull(event, "event");

        Key key = new Key(level.dimension(), pos);
        Set<IComputerAccess> set = COMPUTERS.get(key);
        if (set == null || set.isEmpty()) return;

        for (IComputerAccess computer : set) {
            try {
                computer.queueEvent(event, args);
            } catch (Throwable ignored) {
                // if a computer is unloading, CC can throw here; ignore
            }
        }
    }
}
