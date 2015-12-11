 package pcl.OpenFM.Handler;
 
 import net.minecraftforge.event.world.WorldEvent;
import pcl.OpenFM.OpenFM;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

 public class ClientEvent
 {
   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void worldUnload(WorldEvent.Unload world)
   {
     ChatMessage.writeline("Stopping all currently running radio streams.");
     OpenFM.killAllStreams();
   }
 }