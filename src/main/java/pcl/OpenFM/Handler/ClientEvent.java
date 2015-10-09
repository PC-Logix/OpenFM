 package pcl.OpenFM.Handler;
 
 import net.minecraftforge.event.world.WorldEvent;
import pcl.OpenFM.OpenFM;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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