package pcl.OpenFM;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pcl.OpenFM.GUI.OFMGuiHandler;
import pcl.OpenFM.Handler.ClientEvent;
import pcl.OpenFM.Handler.ServerEvent;
import pcl.OpenFM.network.PacketHandler;
import pcl.OpenFM.player.PlayerDispatcher;

@Mod(modid=BuildInfo.modID, name=BuildInfo.modName, version=BuildInfo.versionNumber + "." + BuildInfo.buildNumber, dependencies = "", guiFactory = "pcl.OpenFM.GUI.OFMGuiFactory", acceptedMinecraftVersions = "1.9.4, 1.10, 1.10.2")
public class OpenFM {
	public static final String MODID = "openfm";
	@Mod.Instance(BuildInfo.modID)
	public static OpenFM instance;
	@SidedProxy(clientSide="pcl.OpenFM.ClientProxy", serverSide="pcl.OpenFM.CommonProxy")
	public static CommonProxy proxy;
	public static List<PlayerDispatcher> playerList = new ArrayList<PlayerDispatcher>();
	public Configuration config;
	public static final Logger logger = LogManager.getFormatterLogger(BuildInfo.modID);
	public static File configFile;
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		PacketHandler.init();

		// Load config
		configFile = new File(event.getModConfigurationDirectory() + "/openfm/openfm.cfg");
		OFMConfiguration.init(configFile);
		
		// Check for Mod Update Detector
		if (event.getSourceFile().getName().endsWith(".jar") && event.getSide().isClient() && OFMConfiguration.enableMUD) {
			logger.info("Registering mod with OpenUpdater.");
			try {
				Class.forName("pcl.mud.OpenUpdater").getDeclaredMethod("registerMod", ModContainer.class, URL.class, URL.class).invoke(null, FMLCommonHandler.instance().findContainerFor(this),
						new URL("http://PC-Logix.com/OpenFM/get_latest_build.php?mcver=1.7.10"),
						new URL("http://PC-Logix.com/OpenFM/changelog.php?mcver=1.7.10"));
			} catch (Throwable e) {
				logger.info("OpenUpdater is not installed, not registering.");
			}
		}
		ContentRegistry.init();
		proxy.initTileEntities();
		proxy.registerItemRenderers();
		
	}

/*
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
	public void onEvent(GuiOpenEvent event) {
		if (event.gui instanceof GuiIngameModOptions) {
			event.gui = new GuiModList(new GuiIngameMenu());
		}
	}
*/	
	@Mod.EventHandler
	public void init(FMLInitializationEvent evt) {
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new OFMGuiHandler());
		MinecraftForge.EVENT_BUS.register(new ClientEvent());
		FMLCommonHandler.instance().bus().register(new ClientEvent());
		MinecraftForge.EVENT_BUS.register(new ServerEvent());
		FMLCommonHandler.instance().bus().register(new ServerEvent());
		FMLCommonHandler.instance().bus().register(instance);
		MinecraftForge.EVENT_BUS.register(instance);
		proxy.registerRenderers();
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if(eventArgs.getModID().equals(BuildInfo.modID)){
			OFMConfiguration.sync();
		}
	}

	public static void killAllStreams() {
		if (playerList != null) {
			for (PlayerDispatcher p : playerList) {
				p.stop();
			}
		}
	}
}


