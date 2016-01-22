 package pcl.OpenFM.network;
 
 import pcl.OpenFM.network.Message.MessageTERadioBlock;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

 public class PacketHandler {
   public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("openfm");
   
   public static void init() {
     INSTANCE.registerMessage(RadioBlockMessageHandlerServer.class, MessageTERadioBlock.class, 0, Side.SERVER);
     INSTANCE.registerMessage(RadioBlockMessageHandlerClient.class, MessageTERadioBlock.class, 1, Side.CLIENT);
   }
 }


