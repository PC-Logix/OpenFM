package me.michiyo.openfm.fabric;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

final class FabricClientConfig {
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("openfm-client.properties");
    private static double volume = loadVolume();

    static synchronized double getVolume() {
        return volume;
    }

    static synchronized void setVolume(double value) {
        volume = Math.max(0.0, Math.min(1.0, value));
        Properties properties = new Properties();
        properties.setProperty("radioVolume", Double.toString(volume));
        try {
            Files.createDirectories(PATH.getParent());
            try (OutputStream output = Files.newOutputStream(PATH)) {
                properties.store(output, "OpenFM client settings");
            }
        } catch (IOException exception) {
            System.err.println("[OpenFM] Could not save Fabric client config: " + exception.getMessage());
        }
    }

    private static double loadVolume() {
        if (!Files.isRegularFile(PATH)) return 1.0;
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(PATH)) {
            properties.load(input);
            return Math.max(0.0, Math.min(1.0,
                    Double.parseDouble(properties.getProperty("radioVolume", "1.0"))));
        } catch (IOException | NumberFormatException exception) {
            System.err.println("[OpenFM] Could not read Fabric client config: " + exception.getMessage());
            return 1.0;
        }
    }

    private FabricClientConfig() {}
}

