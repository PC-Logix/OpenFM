/**
 * 
 */
package pcl.OpenFM.Handler;

import pcl.OpenFM.TileEntity.TileEntityRadio;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;

/**
 * @author Caitlyn
 *
 */
public class RadioContainer extends Container {
	public TileEntityRadio tileEntity;
	
	public RadioContainer(InventoryPlayer inventory, TileEntityRadio te) {
		tileEntity = te;
	}
	
	/* (non-Javadoc)
	 * @see net.minecraft.inventory.Container#canInteractWith(net.minecraft.entity.player.EntityPlayer)
	 */
	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return tileEntity.isUseableByPlayer(player);
	}

}
