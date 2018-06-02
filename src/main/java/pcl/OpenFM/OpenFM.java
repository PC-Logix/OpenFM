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

@Mod(modid=BuildInfo.modID, name=BuildInfo.modName, version=BuildInfo.versionNumber + "." + BuildInfo.buildNumber, dependencies = "", guiFactory = "pcl.OpenFM.GUI.OFMGuiFactory", acceptedMinecraftVersions = "1.12.2")
public class OpenFM {
	public static final String MODID = "openfm";
	@Mod.Instance(BuildInfo.modID)
	public static OpenFM instance;
	@SidedProxy(clientSide="pcl.OpenFM.ClientProxy", serverSide="pcl.OpenFM.CommonProxy")
	public static CommonProxy proxy;
	public static List<PlayerDispatcher> playerList = new ArrayList<PlayerDispatcher>();
	public static final Logger logger = LogManager.getFormatterLogger(BuildInfo.modID);
	public static File configFile;
	private static ContentRegistry contentRegistry = new ContentRegistry();
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		PacketHandler.init();
		MinecraftForge.EVENT_BUS.register(contentRegistry);
		// Load config
		configFile = new File(event.getModConfigurationDirectory() + "/openfm/openfm.cfg");
		OFMConfiguration.init(configFile);
		ContentRegistry.preInit();
		proxy.initTileEntities();
		proxy.registerItemRenderers();
		proxy.registerRenderers();
		FMLCommonHandler.instance().bus().register(instance);
		MinecraftForge.EVENT_BUS.register(instance);
	}
	
	@Mod.EventHandler
	public void init(FMLInitializationEvent evt) {
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new OFMGuiHandler());
		MinecraftForge.EVENT_BUS.register(new ClientEvent());
		FMLCommonHandler.instance().bus().register(new ClientEvent());
		MinecraftForge.EVENT_BUS.register(new ServerEvent());
		FMLCommonHandler.instance().bus().register(new ServerEvent());
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
				if (p != null && p.isPlaying())
					p.stop();
			}
		}
	}
}


