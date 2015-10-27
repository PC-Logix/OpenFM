package pcl.OpenFM.misc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import pcl.OpenFM.OpenFM;
import pcl.OpenFM.TileEntity.TileEntityRadio;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class OFMBreakEvent {

	public OFMBreakEvent() {
		OpenFM.logger.info("Registering BreakEvent");
	}

	public static boolean IsOp(EntityPlayer player) {
		return MinecraftServer.getServer().getConfigurationManager().func_152596_g(player.getGameProfile());
	}

	@SubscribeEvent(priority=EventPriority.NORMAL)
	public void onBlockBreak(BreakEvent event) {
		if (event.getPlayer() instanceof EntityPlayerMP) {
			if (!IsOp(event.getPlayer())) {
				TileEntity TE = event.world.getTileEntity(event.x, event.y, event.z);
				if(TE instanceof TileEntityRadio){
					TileEntityRadio xEntity = (TileEntityRadio) TE;
					if(xEntity.owner!=null){
						if(!xEntity.owner.equals(event.getPlayer().getUniqueID().toString()) && xEntity.isLocked) {
							if(!xEntity.owner.isEmpty()) {
								event.setCanceled(true);						
							}
						}
					}
				}
			} else {
				TileEntity TE = event.world.getTileEntity(event.x, event.y, event.z);
				if(TE instanceof TileEntityRadio){
					OpenFM.logger.info("Op is breaking a radio at X:" + event.x + " Y: " + event.y + " Z: " + event.z);
				}
			}
		}
	}
}