/**
 * 
 */
package pcl.OpenFM.TileEntity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import pcl.OpenFM.ContentRegistry;
import pcl.OpenFM.Items.ItemMemoryCard;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
/**
 * @author Caitlyn
 *
 */
public class RadioContainer extends Container {
	public TileEntityRadio tileEntity;	

	public RadioContainer(InventoryPlayer playerInv, final TileEntityRadio pedestal) {
		IItemHandler inventory = pedestal.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.NORTH);
		final int TILE_INVENTORY_XPOS = 5;
		final int TILE_INVENTORY_YPOS = 70;
		final int SLOT_X_SPACING = 18;
		addSlotToContainer(new SlotItemHandler(inventory, 0, TILE_INVENTORY_XPOS + SLOT_X_SPACING * 0, TILE_INVENTORY_YPOS) {
			@Override
			public void onSlotChanged() {
				pedestal.markDirty();
			}
			
			@Override
			public boolean isItemValid(@Nonnull ItemStack stack) {
				Item memoryCard = ContentRegistry.itemMemoryCard;
				ItemStack memoryCards = new ItemStack(memoryCard);
				return stack.areItemsEqualIgnoreDurability(stack, memoryCards);
			}
		});
		
		final int HOTBAR_XPOS = 9;
		final int HOTBAR_YPOS = 15;
		for (int k = 0; k < 9; k++) {
			addSlotToContainer(new Slot(playerInv, k, HOTBAR_XPOS + SLOT_X_SPACING * k, HOTBAR_YPOS));
		}
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = inventorySlots.get(index);	
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
	
			int containerSlots = inventorySlots.size() - player.inventory.mainInventory.size();
	
			if (index < containerSlots) {
				if (!this.mergeItemStack(itemstack1, containerSlots, inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(itemstack1, 0, containerSlots, false)) {
				return ItemStack.EMPTY;
			}
	
			if (itemstack1.getCount() == 0) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}
	
			if (itemstack1.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}
	
			slot.onTake(player, itemstack1);
		}
	
		return itemstack;
	}
	
}
