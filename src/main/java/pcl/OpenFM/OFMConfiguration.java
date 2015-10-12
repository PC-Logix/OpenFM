package pcl.OpenFM;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class OFMConfiguration {

	// All config values
	public static String defaultURL;
	public static boolean enableMUD;

	// Called by OpenFM preInit()
	public static void init(File configFile) {

		Configuration config = new Configuration(configFile);

		try {
			config.load();

			defaultURL = config.get("general", "defaultURL", "StreamURL", "The default stream of the player.").getString();
			enableMUD = config.get("general", "enableMUD", true, "Automatically check for mod updates.").getBoolean();

		} catch(Exception e) {
			OpenFM.logger.error("OpenFM encountered a problem with loading the config file.");
		} finally {
			if (config.hasChanged()) {
				config.save();
			}
		}
	}
}