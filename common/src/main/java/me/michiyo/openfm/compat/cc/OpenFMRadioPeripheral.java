package me.michiyo.openfm.compat.cc;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import me.michiyo.openfm.world.be.RadioBlockEntity;
import net.minecraft.server.level.ServerLevel;

import org.jspecify.annotations.Nullable;

public final class OpenFMRadioPeripheral implements IPeripheral {
    private final RadioBlockEntity radio;

    public OpenFMRadioPeripheral(RadioBlockEntity radio) {
        this.radio = radio;
    }

    @Override
    public String getType() {
        return "openfm_radio";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof OpenFMRadioPeripheral o && o.radio == this.radio;
    }

    // ---- attach/detach for event delivery ----

    @Override
    public void attach(IComputerAccess computer) {
        if (radio.getLevel() instanceof ServerLevel level) {
            CCEvents.register(level, radio.getBlockPos(), computer);
        }
    }

    @Override
    public void detach(IComputerAccess computer) {
        if (radio.getLevel() instanceof ServerLevel level) {
            CCEvents.unregister(level, radio.getBlockPos(), computer);
        }
    }

    // ---- results helpers (Lua-friendly) ----

    private static Object[] ok() {
        return new Object[] { true };
    }

    private static Object[] fail(String msg) {
        return new Object[] { false, msg };
    }

    private boolean lockedForCC() {
        return radio.cc_isLockedForCC();
    }

    // ---------- Stream control ----------

    @LuaFunction(mainThread = true)
    public Object[] setURL(String url) {
        if (lockedForCC()) return fail("locked");
        radio.cc_setStreamUrl(url == null ? "" : url);
        return ok();
    }

    @LuaFunction(mainThread = true)
    public Object[] start() {
        if (lockedForCC()) return fail("locked");
        boolean started = radio.cc_start();
        return started ? ok() : fail("missing url or volume is 0");
    }

    @LuaFunction(mainThread = true)
    public Object[] stop() {
        if (lockedForCC()) return fail("locked");
        radio.cc_stop();
        return ok();
    }

    @LuaFunction(mainThread = true)
    public Object[] play(String url) {
        if (lockedForCC()) return fail("locked");
        url = (url == null) ? "" : url;
        if (url.isBlank()) return fail("missing url");
        radio.cc_setStreamUrl(url);
        boolean started = radio.cc_start();
        return started ? ok() : fail("volume is 0");
    }

    @LuaFunction(mainThread = true)
    public Object[] volUp() {
        if (lockedForCC()) return fail("locked");
        radio.cc_setVolume(radio.cc_getVolume() + 1);
        return ok();
    }

    @LuaFunction(mainThread = true)
    public Object[] volDown() {
        if (lockedForCC()) return fail("locked");
        radio.cc_setVolume(radio.cc_getVolume() - 1);
        return ok();
    }

    @LuaFunction(mainThread = true)
    public int getVol() {
        return radio.cc_getVolume();
    }

    @LuaFunction(mainThread = true)
    public String getURL() {
        return radio.cc_getStreamUrl();
    }

    @LuaFunction(mainThread = true)
    public boolean isPlaying() {
        return radio.isPlaying();
    }

    @LuaFunction(mainThread = true)
    public boolean getLockState() {
        return radio.isLocked();
    }

    @LuaFunction(mainThread = true)
    public String getNowPlaying() {
        return radio.getNowPlaying();
    }

    // ---------- Screen ----------

    @LuaFunction(mainThread = true)
    public Object[] setScreenColor(int rgb) {
        if (lockedForCC()) return fail("locked");
        radio.cc_setScreenColor(rgb);
        return ok();
    }

    @LuaFunction(mainThread = true)
    public int getScreenColor() {
        return radio.cc_getScreenColor();
    }

    @LuaFunction(mainThread = true)
    public Object[] setScreenText(String text) {
        if (lockedForCC()) return fail("locked");
        radio.cc_setScreenText(text == null ? "" : text);
        return ok();
    }

    @LuaFunction(mainThread = true)
    public String getScreenText() {
        return radio.cc_getScreenText();
    }
}
