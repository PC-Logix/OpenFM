# OpenFM

OpenFM adds streaming internet radio blocks and linked speakers to Minecraft 1.21.1, with optional CC:Tweaked peripheral support.

## Loaders

OpenFM is maintained as a multi-loader project:

- `common` contains shared gameplay, networking payloads, UI, rendering, and streaming audio code.
- `neoforge` contains the NeoForge entrypoint and loader integrations.
- `fabric` contains the Fabric entrypoints and loader integrations.

The loader modules compile the same common sources. Loader-specific behavior is kept behind the registration and platform service interfaces under `me.michiyo.openfm.platform`.

## Building

Java 21 is required.

```powershell
.\gradlew.bat :neoforge:build :fabric:build
```

The distributable JARs are written to:

- `neoforge/build/libs/openfm-neoforge-1.21.1-<version>.jar`
- `fabric/build/libs/openfm-fabric-1.21.1-<version>.jar`

Both JARs embed the MP3, Ogg Vorbis, and AAC decoder libraries. The Fabric build requires Fabric API. CC:Tweaked is optional on both loaders.

## Development runs

```powershell
.\gradlew.bat :neoforge:runClient
.\gradlew.bat :neoforge:runServer
.\gradlew.bat :fabric:runClient
.\gradlew.bat :fabric:runServer
```

## CC:Tweaked

When CC:Tweaked is installed, the radio is exposed as a peripheral with the type
`openfm_radio`. Existing Lua-facing methods and events are shared between both
loader builds.

OpenFM also adds an interactive controller to CraftOS. Attach a radio directly
or through a wired modem, then run:

```text
openfm
```

Use `openfm <peripheral_name>` to select a specific radio when several are
attached, or `help openfm` for the bundled controls reference.

Find an adjacent radio, or one connected through a wired modem, with the
CC:Tweaked peripheral API:

```lua
local fm = peripheral.find("openfm_radio")
assert(fm, "No OpenFM radio found")

local ok, err = fm.play("https://example.com/radio-stream")
if not ok then
    error(err)
end
```

Methods which change the radio return `true` on success, or `false, error` if
the operation fails. A locked, player-owned radio cannot be changed by a
computer.

| Method | Description |
| --- | --- |
| `setURL(url)` | Sets the stream URL. Clearing the URL stops playback. |
| `start()` | Starts the configured stream. Fails if the URL is empty or the volume is `0`. |
| `stop()` | Stops playback. |
| `play(url)` | Sets the stream URL and starts playback. |
| `volUp()` | Raises the volume by one step, up to `10`. |
| `volDown()` | Lowers the volume by one step, down to `0`. Setting the volume to `0` stops playback. |
| `getVol()` | Returns the current volume from `0` to `10`. |
| `getURL()` | Returns the configured stream URL. |
| `isPlaying()` | Returns whether the radio is playing. |
| `getLockState()` | Returns whether the radio is locked. |
| `getNowPlaying()` | Returns the latest ICY song title reported by a listening client, or an empty string if none is known. |
| `setScreenColor(rgb)` | Sets the display text color as a 24-bit RGB value, such as `0xFF0000` for red. |
| `getScreenColor()` | Returns the display text color as a 24-bit RGB value. |
| `setScreenText(text)` | Sets the display text. Long text scrolls; blank text resets it to `OpenFM`. |
| `getScreenText()` | Returns the current display text. |

Attached computers receive state changes as events. Song metadata is pushed as:

```lua
local _, x, y, z, title = os.pullEvent("openfm_now_playing")
print("Now playing: " .. title)
```

An empty title means playback stopped or the stream URL changed. Metadata is
reported by clients already listening to the stream, so the dedicated server
does not open an additional connection for every active radio.
