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

When CC:Tweaked is installed, the radio is exposed as an OpenFM peripheral. Existing Lua-facing methods and events are shared between both loader builds.

OpenFM is a heavily modified version of Dragon's Radio Mod. Both projects are licensed under the MIT License.
