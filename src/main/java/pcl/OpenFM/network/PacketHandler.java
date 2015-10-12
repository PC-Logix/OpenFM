 package pcl.OpenFM.network;
 
 import pcl.OpenFM.network.Message.MessageTERadioBlock;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

 public class PacketHandler {
   public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("openfm");
   
   public static void init() {
     INSTANCE.registerMessage(RadioBlockMessageHandlerServer.class, MessageTERadioBlock.class, 0, Side.SERVER);
     INSTANCE.registerMessage(RadioBlockMessageHandlerClient.class, MessageTERadioBlock.class, 0, Side.CLIENT);
   }
 }


