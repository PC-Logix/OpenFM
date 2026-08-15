package me.michiyo.openfm.world.menu;

import me.michiyo.openfm.reg.ModItems;
import me.michiyo.openfm.reg.ModMenus;
import me.michiyo.openfm.world.item.PortablePlayerItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PortablePlayerMenu extends AbstractContainerMenu {
    private static final int SLOT_CARD = 0;
    private static final int SLOT_HOTBAR_START = 1;
    private static final int SLOT_HOTBAR_END = SLOT_HOTBAR_START + 9;

    public final ItemStack portable;
    private final SimpleContainer cardInventory;
    private final ContainerData data = new SimpleContainerData(2);

    public PortablePlayerMenu(int containerId, Inventory inv, FriendlyByteBuf ignored) {
        this(containerId, inv);
    }

    public PortablePlayerMenu(int containerId, Inventory inv) {
        super(ModMenus.PORTABLE_MENU.get(), containerId);
        this.portable = PortablePlayerItem.findHeldStack(inv.player);
        if (portable.isEmpty()) throw new IllegalStateException("PortablePlayerMenu opened without a held portable streamer");

        this.cardInventory = new SimpleContainer(1) {
            @Override public void setChanged() {
                super.setChanged();
                saveCard();
            }
        };
        this.cardInventory.setItem(0, PortablePlayerItem.getEmbeddedCard(portable));
        this.addDataSlots(data);

        this.addSlot(new Slot(cardInventory, 0, 45, 60) {
            @Override public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.MEMORY_CARD.get());
            }
        });

        // Keep the player's hotbar available so a memory card can be placed into the portable player.
        final int hotbarX = 49;
        final int hotbarY = 6;
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inv, col, hotbarX + col * 18, hotbarY));
        }

    }

    public ItemStack getCard() { return cardInventory.getItem(0); }
    public String getUrl() { return PortablePlayerItem.getUrl(portable); }
    public int getVolume() { return data.get(0) == 0 ? PortablePlayerItem.getVolume(portable) : data.get(0); }
    public boolean isPlaying() { return data.get(1) == 1 || PortablePlayerItem.isPlaying(portable); }

    public void saveCard() {
        if (!portable.isEmpty()) PortablePlayerItem.saveEmbeddedCard(portable, cardInventory.getItem(0));
    }

    @Override
    public void broadcastChanges() {
        data.set(0, PortablePlayerItem.getVolume(portable));
        data.set(1, PortablePlayerItem.isPlaying(portable) ? 1 : 0);
        super.broadcastChanges();
    }

    @Override
    public boolean stillValid(Player player) {
        return PortablePlayerItem.findHeldStack(player) == portable;
    }

    @Override
    public void removed(Player player) {
        saveCard();
        super.removed(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack empty = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) return empty;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index == SLOT_CARD) {
            if (!this.moveItemStackTo(stack, SLOT_HOTBAR_START, SLOT_HOTBAR_END, false)) return empty;
        } else {
            if (!this.moveItemStackTo(stack, SLOT_CARD, SLOT_CARD + 1, false)) return empty;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return copy;
    }
}
