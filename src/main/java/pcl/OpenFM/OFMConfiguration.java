package pcl.OpenFM;

import java.io.File;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;

public class OFMConfiguration {

	// All config values
	public static String defaultURL = "StreamURL";
	public static boolean enableMUD = true;
	public static boolean enableStreams = true;
	public static Integer maxSpeakers = 10;
	public static Configuration config;

	// Called by OpenFM preInit()
	public static void init(File configFile) {

		config = new Configuration(configFile);

		try {
			config.load();
			defaultURL = config.get("general", "defaultURL", defaultURL, "The default stream of the player.").getString();
			enableMUD = config.get("general", "enableMUD", enableMUD, "Automatically check for mod updates.").getBoolean();
			enableStreams = config.get("general", "enableStreams", enableStreams, "Should we try to play streams at all? If false streams will not work in game. (Client side only)").getBoolean();
			maxSpeakers = config.get("general", "maxSpeakers", maxSpeakers, "Maximum speakers that can be attached to a radio, higher numbers may cause performance issues").getInt(10);
		} catch(Exception e) {
			OpenFM.logger.error("OpenFM encountered a problem with loading the config file.");
		} finally {
			if (config.hasChanged()) {
				config.save();
			}
		}
	}

	public static void sync() {
		if (config.hasChanged()) {
			config.save();
		}
	}
	
	public static ConfigCategory getCategory(String name) {
		// TODO Auto-generated method stub
		return config.getCategory(name.toLowerCase()).setLanguageKey(name.toLowerCase().replace(" ", ""));
	}
}
