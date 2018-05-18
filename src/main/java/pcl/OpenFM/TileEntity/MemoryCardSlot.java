/**
 * 
 */
package pcl.OpenFM.TileEntity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import pcl.OpenFM.Items.ItemMemoryCard;

/**
 * @author Caitlyn
 *
 */
public class MemoryCardSlot extends Slot {
	public MemoryCardSlot(IInventory par1iInventory, int par2, int par3, int par4) {
		super(par1iInventory, par2, par3, par4);
	}
	@Override
	public boolean isItemValid(ItemStack itemstack) {
		if (itemstack.getItem() instanceof ItemMemoryCard) {
			return true;
		}
		return false;
	}

	/**
	 * Called when the player picks up an item from an inventory slot
	 */
	@Override
	public ItemStack onTake(EntityPlayer par1EntityPlayer, ItemStack par2ItemStack)
	{
		this.onCrafting(par2ItemStack);
		return super.onTake(par1EntityPlayer, par2ItemStack);
	}
}
