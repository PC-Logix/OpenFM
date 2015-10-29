 package pcl.OpenFM.network;
 
 import pcl.OpenFM.TileEntity.TileEntityRadio;
import pcl.OpenFM.network.Message.MessageTERadioBlock;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
 
 
 public class RadioBlockMessageHandlerClient implements IMessageHandler<MessageTERadioBlock, IMessage>
 {
   public IMessage onMessage(MessageTERadioBlock message, MessageContext ctx)
   {
     Side side = FMLCommonHandler.instance().getEffectiveSide();
     if (side == Side.SERVER)
       return null;
     TileEntityRadio tileEntity = null;
     
     tileEntity = (TileEntityRadio)FMLClientHandler.instance().getClient().theWorld.getTileEntity((int)message.x, (int)message.y, (int)message.z);
     if (tileEntity == null) {
       return null;
     }
     if ((tileEntity instanceof TileEntityRadio)) {
       if (message.mode == 15) {
         tileEntity.addSpeaker(tileEntity.getWorldObj(), message.tx, message.ty, message.tz);
         return null;
       }
       
       if (message.mode == 42) {
    	   if (!tileEntity.stations.contains(message.streamURL))
    		   tileEntity.addStation(message.streamURL);
       }
       
       if (message.mode == 43) {
    	   tileEntity.delStation(message.streamURL);
       }
       
       if (message.mode == 44 || message.mode == 47) {
    	   tileEntity.isLocked = true;
       }
       
       if (message.mode == 45 || message.mode == 46) {
    	   tileEntity.isLocked = false;
       }
       
       if ((message.mode == 11) || (message.mode == 14)) {
         tileEntity.listenToRedstone = true;
       }
       if ((message.mode == 12) || (message.mode == 13)) {
         tileEntity.listenToRedstone = false;
       }
       
       if ((message.volume > 0.0F) && (message.volume <= 1.0F)) {
         tileEntity.volume = message.volume;
       }
       
       tileEntity.streamURL = message.streamURL;
       
       if ((message.mode == 1) || (message.mode == 13) || (message.mode == 14)) {
         if (message.isPlaying)
         {
        	 if (tileEntity.isValid) {
                 tileEntity.startStream();
        	 }
         }
         else
         {
           tileEntity.stopStream();
         }
       }
     }
     return null;
   }
 }


