 package pcl.OpenFM.Handler;
 
 import net.minecraft.entity.player.EntityPlayerMP;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

 public class ServerEvent
 {
   @SideOnly(Side.SERVER)
   @SubscribeEvent
   public void userLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
   {
     if ((event.player instanceof EntityPlayerMP)) {}
   }
 }


