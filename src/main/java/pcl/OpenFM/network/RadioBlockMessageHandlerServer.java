 package pcl.OpenFM.network;
 
 import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import pcl.OpenFM.OpenFM;
import pcl.OpenFM.Handler.ChatMessage;
import pcl.OpenFM.TileEntity.TileEntityRadio;
import pcl.OpenFM.network.Message.MessageTERadioBlock;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
 
 public class RadioBlockMessageHandlerServer implements IMessageHandler<MessageTERadioBlock, IMessage>
 {
   public IMessage onMessage(MessageTERadioBlock message, MessageContext ctx)
   {
     Side side = Side.SERVER;
     PacketHandler.INSTANCE.sendToAll(message);
     WorldServer targetWorld = null;
     net.minecraft.tileentity.TileEntity tileEntity = null;
     WorldServer[] ws = MinecraftServer.getServer().worldServers;
     for (WorldServer s : ws) {
       if (s.provider.dimensionId == message.dim) {
         targetWorld = s;
         tileEntity = s.getTileEntity((int)message.x, (int)message.y, (int)message.z);
       }
     }
     
 
 
     if ((tileEntity instanceof TileEntityRadio)) {
       if (message.mode == 15) {
         ((TileEntityRadio)tileEntity).addSpeaker(targetWorld, message.tx, message.ty, message.tz);
         return null;
       }
       
       if ((message.mode == 11) || (message.mode == 14)) {
         ((TileEntityRadio)tileEntity).setListenToRedstoneInput(true);
       }
       if ((message.mode == 12) || (message.mode == 13)) {
         ((TileEntityRadio)tileEntity).setListenToRedstoneInput(false);
       }
       
       if ((message.volume > 0.0F) && (message.volume <= 1.0F)) {
         ((TileEntityRadio)tileEntity).setVolume(message.volume);
       }
       
 
       ((TileEntityRadio)tileEntity).setStreamURL(message.streamURL);
       
       if ((message.mode == 1) || (message.mode == 13) || (message.mode == 14)) {
         //((TileEntityRadio)tileEntity).setIsPlaying(message.isPlaying); // This should be handled in startStream() and stopStream().
         if (message.isPlaying)
         {
           ((TileEntityRadio)tileEntity).startStream();
         }
         else
         {
           ((TileEntityRadio)tileEntity).stopStream();
         }
       }
     }
     
     return null;
   }
 }


