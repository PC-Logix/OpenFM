package pcl.OpenFM.Handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import pcl.OpenFM.OpenFM;
import pcl.OpenFM.TileEntity.TileEntityRadio;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class OFMBreakEvent {

	public OFMBreakEvent() {
		OpenFM.logger.info("Registering BreakEvent");
	}

	public static boolean IsOp(EntityPlayer player) {
		return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().canSendCommands(player.getGameProfile());
	}

	@SubscribeEvent(priority=EventPriority.NORMAL)
	public void onBlockBreak(BreakEvent event) {
		if (event.getPlayer() instanceof EntityPlayerMP) {
			if (!IsOp(event.getPlayer())) {
				TileEntity TE = event.getWorld().getTileEntity(new BlockPos(event.getPos().getX(), event.getPos().getY(), event.getPos().getZ()));
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
				TileEntity TE = event.getWorld().getTileEntity(new BlockPos(event.getPos().getX(), event.getPos().getY(), event.getPos().getZ()));
				if(TE instanceof TileEntityRadio){
					OpenFM.logger.info("Op is breaking a radio at X:" + event.getPos().getX() + " Y: " + event.getPos().getY() + " Z: " + event.getPos().getZ());
				}
			}
		}
	}
}