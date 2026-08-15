package me.michiyo.openfm.net;

import me.michiyo.openfm.platform.Services;
import me.michiyo.openfm.world.item.PortablePlayerItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PortablePlayerSync {
    private record State(ResourceKey<Level> dimension, String url, int volume,
                         boolean playing, boolean privateOnly) {}

    private static final Map<UUID, State> LAST_STATES = new HashMap<>();

    public static void sync(ServerPlayer player) {
        S2CPortableState state = stateFor(player, PortablePlayerItem.findHeldStack(player));
        LAST_STATES.put(player.getUUID(), stateOf(state));
        broadcast(player, state);
    }

    public static void tick(ServerPlayer player) {
        State previous = LAST_STATES.get(player.getUUID());
        S2CPortableState current = !player.isAlive() || player.isRemoved()
                ? clearState(player)
                : stateFor(player, PortablePlayerItem.findHeldStack(player));
        State next = stateOf(current);
        LAST_STATES.put(player.getUUID(), next);

        if (previous != null && previous.playing && !next.playing) {
            PortablePlayerItem.stopPlayback(player);
        }

        if ((previous == null && next.playing) || (previous != null && !previous.equals(next))) {
            broadcast(player, current);
        }
    }

    public static void onDisconnect(ServerPlayer player) {
        LAST_STATES.remove(player.getUUID());
        PortablePlayerItem.stopPlayback(player);
        broadcast(player, clearState(player));
    }

    public static void syncAllTo(ServerPlayer recipient) {
        for (ServerPlayer source : recipient.server.getPlayerList().getPlayers()) {
            var stack = PortablePlayerItem.findHeldStack(source);
            if (!stack.isEmpty() && PortablePlayerItem.isPlaying(stack)) {
                Services.PLATFORM.sendToPlayer(recipient, stateFor(source, stack));
            }
        }
    }

    private static S2CPortableState stateFor(ServerPlayer player, net.minecraft.world.item.ItemStack stack) {
        return new S2CPortableState(
                player.getUUID(),
                player.level().dimension(),
                stack.isEmpty() ? "" : PortablePlayerItem.getUrl(stack),
                stack.isEmpty() ? 0 : PortablePlayerItem.getVolume(stack),
                !stack.isEmpty() && PortablePlayerItem.isPlaying(stack),
                !stack.isEmpty() && PortablePlayerItem.isPrivate(stack));
    }

    private static S2CPortableState clearState(ServerPlayer player) {
        return new S2CPortableState(player.getUUID(), player.level().dimension(), "", 0, false, false);
    }

    private static State stateOf(S2CPortableState state) {
        return new State(state.dimension, state.url, state.volume, state.playing, state.privateOnly);
    }

    private static void broadcast(ServerPlayer source, S2CPortableState state) {
        for (ServerPlayer recipient : source.server.getPlayerList().getPlayers()) {
            Services.PLATFORM.sendToPlayer(recipient, state);
        }
    }

    private PortablePlayerSync() {}
}
