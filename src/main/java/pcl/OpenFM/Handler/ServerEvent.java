 package pcl.OpenFM.Handler;
 
 import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

 public class ServerEvent
 {
   @SideOnly(Side.SERVER)
   @SubscribeEvent
   public void userLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
   {
     if ((event.player instanceof EntityPlayerMP)) {}
   }
 }


