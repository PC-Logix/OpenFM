/**
 * 
 */
package pcl.OpenFM.Handler;

import pcl.OpenFM.Items.ItemMemoryCard;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * @author Caitlyn
 *
 */
public class CardSlot extends Slot {

	public CardSlot(IInventory par1iInventory, int par2, int par3, int par4) {
		super(par1iInventory, par2, par3, par4);
	}

	@Override
	public boolean isItemValid(ItemStack itemstack) {
		if (itemstack.getItem() instanceof ItemMemoryCard) {
			if (itemstack.getTagCompound() == null) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

}
