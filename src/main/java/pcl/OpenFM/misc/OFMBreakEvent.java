package pcl.OpenFM.misc;

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
	
	@SubscribeEvent(priority=EventPriority.NORMAL)
	public void onBlockBreak(BreakEvent event) {
		TileEntity TE = event.world.getTileEntity(event.x, event.y, event.z);
		if(TE instanceof TileEntityRadio){
			TileEntityRadio xEntity = (TileEntityRadio) TE;
			if(xEntity.owner!=null){
				if(!MinecraftServer.getServer().getConfigurationManager().func_152596_g(event.getPlayer().getGameProfile()) || !xEntity.owner.equals(event.getPlayer().getUniqueID().toString()) && xEntity.isLocked) {
					if(!xEntity.owner.isEmpty()) {
						event.setCanceled(true);						
					}
				}
			}
		}		
	}
}