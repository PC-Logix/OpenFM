package me.michiyo.openfm.compat.oc;

import li.cil.oc.api.Network;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedBlockEntity;
import me.michiyo.openfm.world.be.RadioBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class OpenFMRadioDriver extends DriverSidedBlockEntity {
    @Override
    public Class<?> getBlockEntityClass() {
        return RadioBlockEntity.class;
    }

    @Override
    public ManagedEnvironment createEnvironment(Level level, BlockPos pos, Direction side) {
        if (level.getBlockEntity(pos) instanceof RadioBlockEntity radio) {
            return new Environment(radio);
        }
        return null;
    }

    public static void queue(ServerLevel level, BlockPos pos, String event, Object[] args) {
        if (!(level.getBlockEntity(pos) instanceof RadioBlockEntity radio)) return;
        Environment.queue(radio, event, args);
    }

    public static final class Environment extends AbstractManagedEnvironment implements NamedBlock {
        private static final ConcurrentHashMap<RadioBlockEntity, Set<Environment>> ENVIRONMENTS =
                new ConcurrentHashMap<>();

        private final RadioBlockEntity radio;

        private Environment(RadioBlockEntity radio) {
            this.radio = radio;
            setNode(Network.newNode(this, Visibility.Network)
                    .withComponent("openfm_radio")
                    .create());
            ENVIRONMENTS.computeIfAbsent(radio, ignored -> ConcurrentHashMap.newKeySet()).add(this);
        }

        private static void queue(RadioBlockEntity radio, String event, Object[] args) {
            Set<Environment> environments = ENVIRONMENTS.get(radio);
            if (environments == null) return;

            Object[] signal = new Object[args.length + 1];
            signal[0] = event;
            System.arraycopy(args, 0, signal, 1, args.length);
            for (Environment environment : environments) {
                Node node = environment.node();
                if (node != null) node.sendToReachable("computer.signal", signal);
            }
        }

        @Override
        public String preferredName() {
            return "openfm_radio";
        }

        @Override
        public int priority() {
            return 0;
        }

        @Override
        public void onDisconnect(Node node) {
            if (node != node()) return;
            Set<Environment> environments = ENVIRONMENTS.get(radio);
            if (environments == null) return;
            environments.remove(this);
            if (environments.isEmpty()) ENVIRONMENTS.remove(radio, environments);
        }

        private static Object[] ok() {
            return new Object[] { true };
        }

        private static Object[] fail(String message) {
            return new Object[] { false, message };
        }

        private boolean locked() {
            return radio.cc_isLockedForCC();
        }

        @Callback(doc = "function(url:string):boolean, string -- Set the radio stream URL.")
        public Object[] setURL(Context context, Arguments args) {
            if (locked()) return fail("locked");
            radio.cc_setStreamUrl(args.checkString(0));
            return ok();
        }

        @Callback(doc = "function():boolean, string -- Start playing the configured stream.")
        public Object[] start(Context context, Arguments args) {
            if (locked()) return fail("locked");
            return radio.cc_start() ? ok() : fail("missing url or volume is 0");
        }

        @Callback(doc = "function():boolean, string -- Stop playback.")
        public Object[] stop(Context context, Arguments args) {
            if (locked()) return fail("locked");
            radio.cc_stop();
            return ok();
        }

        @Callback(doc = "function(url:string):boolean, string -- Set a stream URL and start playback.")
        public Object[] play(Context context, Arguments args) {
            if (locked()) return fail("locked");
            String url = args.checkString(0);
            if (url.isBlank()) return fail("missing url");
            radio.cc_setStreamUrl(url);
            return radio.cc_start() ? ok() : fail("volume is 0");
        }

        @Callback(doc = "function():boolean, string -- Increase volume by one step.")
        public Object[] volUp(Context context, Arguments args) {
            if (locked()) return fail("locked");
            radio.cc_setVolume(radio.cc_getVolume() + 1);
            return ok();
        }

        @Callback(doc = "function():boolean, string -- Decrease volume by one step.")
        public Object[] volDown(Context context, Arguments args) {
            if (locked()) return fail("locked");
            radio.cc_setVolume(radio.cc_getVolume() - 1);
            return ok();
        }

        @Callback(doc = "function():number -- Get the volume from zero to ten.")
        public Object[] getVol(Context context, Arguments args) {
            return new Object[] { radio.cc_getVolume() };
        }

        @Callback(doc = "function():string -- Get the configured stream URL.")
        public Object[] getURL(Context context, Arguments args) {
            return new Object[] { radio.cc_getStreamUrl() };
        }

        @Callback(doc = "function():boolean -- Return whether the radio is playing.")
        public Object[] isPlaying(Context context, Arguments args) {
            return new Object[] { radio.isPlaying() };
        }

        @Callback(doc = "function():boolean -- Return whether the radio is locked.")
        public Object[] getLockState(Context context, Arguments args) {
            return new Object[] { radio.isLocked() };
        }

        @Callback(doc = "function():string -- Get the current stream title.")
        public Object[] getNowPlaying(Context context, Arguments args) {
            return new Object[] { radio.getNowPlaying() };
        }

        @Callback(doc = "function(color:number):boolean, string -- Set the display color as 0xRRGGBB.")
        public Object[] setScreenColor(Context context, Arguments args) {
            if (locked()) return fail("locked");
            radio.cc_setScreenColor(args.checkInteger(0));
            return ok();
        }

        @Callback(doc = "function():number -- Get the display color as 0xRRGGBB.")
        public Object[] getScreenColor(Context context, Arguments args) {
            return new Object[] { radio.cc_getScreenColor() };
        }

        @Callback(doc = "function(text:string):boolean, string -- Set the radio display text.")
        public Object[] setScreenText(Context context, Arguments args) {
            if (locked()) return fail("locked");
            radio.cc_setScreenText(args.checkString(0));
            return ok();
        }

        @Callback(doc = "function():string -- Get the radio display text.")
        public Object[] getScreenText(Context context, Arguments args) {
            return new Object[] { radio.cc_getScreenText() };
        }
    }
}
