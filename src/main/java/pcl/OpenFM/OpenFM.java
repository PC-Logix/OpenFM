package pcl.OpenFM;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import pcl.OpenFM.Handler.ClientEvent;
import pcl.OpenFM.Handler.ServerEvent;
import pcl.OpenFM.network.PacketHandler;
import pcl.OpenFM.player.MP3Player;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid=BuildInfo.modID, name=BuildInfo.modName, version=BuildInfo.versionNumber + "." + BuildInfo.buildNumber, dependencies = "")
public class OpenFM {

	// Mod vars
	@Mod.Instance(BuildInfo.modID)
	public static OpenFM instance;

	@SidedProxy(clientSide="pcl.OpenFM.ClientProxy", serverSide="pcl.OpenFM.CommonProxy")
	public static CommonProxy proxy;

	// Utils
	public static final Logger logger  = LogManager.getFormatterLogger(BuildInfo.modID);

	// Mod content
	public static Block blockRadio;
	public static Block blockSpeaker;
	public static Block blockDummySpeaker;
	public static Item itemRadioTuner;
	public static List<MP3Player> playerList = new ArrayList<MP3Player>();
	public static CreativeTabs creativeTab;

	// Config values
	public static String defaultURL;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		PacketHandler.init();

		// Load config
		Configuration config = new Configuration(new File(event.getModConfigurationDirectory() + "/openfm/openfm.cfg"));
		config.load();
			defaultURL = config.get("general", "defaultURL", "StreamURL", "The default stream of the player.").getString();
			boolean enableMUD = config.get("general", "enableMUD", true, "Automatically check for mod updates.").getBoolean();
		config.save();

		// Check for Mod Update Detector
		if (event.getSourceFile().getName().endsWith(".jar") && event.getSide().isClient() && enableMUD) {
			logger.info("Registering mod with OpenUpdater.");
			try {
				Class.forName("pcl.mud.OpenUpdater").getDeclaredMethod("registerMod", ModContainer.class, URL.class, URL.class).invoke(null, FMLCommonHandler.instance().findContainerFor(this),
						new URL("http://PC-Logix.com/OpenFM/get_latest_build.php?mcver=1.7.10"),
						new URL("http://PC-Logix.com/OpenFM/changelog.php?mcver=1.7.10"));
			} catch (Throwable e) {
				logger.info("OpenUpdater is not installed, not registering.");
			}
		}
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent evt)
	{
		MinecraftForge.EVENT_BUS.register(new ClientEvent());
		FMLCommonHandler.instance().bus().register(new ClientEvent());
		MinecraftForge.EVENT_BUS.register(new ServerEvent());
		FMLCommonHandler.instance().bus().register(new ServerEvent());
		ContentRegistry.registerTabs();
		ContentRegistry.registerBlocks();
		ContentRegistry.registerItems(); 
		ContentRegistry.registerEvents();
		proxy.initTileEntities();
		proxy.registerRenderers();
	}

	public static void killAllStreams()
	{
		for (MP3Player p : playerList)
		{
			p.stop();
		}
	}
}


