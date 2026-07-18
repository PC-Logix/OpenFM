package me.michiyo.openfm.world.menu;

import me.michiyo.openfm.net.S2CSyncFields;
import me.michiyo.openfm.reg.ModItems;
import me.michiyo.openfm.reg.ModMenus;
import me.michiyo.openfm.world.be.RadioBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class RadioMenu extends AbstractContainerMenu {
    public final BlockPos pos;
    public final RadioBlockEntity radio;

    private final ContainerData data;

    private static final int SLOT_MEMORY_CARD = 0;
    private static final int SLOT_HOTBAR_START = 1; // 1..9
    private static final int SLOT_HOTBAR_END = SLOT_HOTBAR_START + 9; // exclusive

    public RadioMenu(int containerId, Inventory inv, FriendlyByteBuf buf) {
        this(containerId, inv, buf.readBlockPos());
    }

    public RadioMenu(int containerId, Inventory inv, BlockPos pos) {
        super(ModMenus.RADIO_MENU.get(), containerId);
        this.pos = pos;

        var level = inv.player.level();
        var be = level.getBlockEntity(pos);
        if (!(be instanceof RadioBlockEntity r)) {
            throw new IllegalStateException("RadioMenu opened without RadioBlockEntity at " + pos);
        }
        this.radio = r;

        if (!inv.player.level().isClientSide() && inv.player instanceof net.minecraft.server.level.ServerPlayer sp) {
            S2CSyncFields.syncToPlayer(sp, pos, radio);
        }

        this.data = new ContainerData() {
            private int clientVolume = 5;
            private int clientLocked = 0;

            @Override
            public int get(int index) {
                // Server side: read real values from BE
                if (!inv.player.level().isClientSide()) {
                    return switch (index) {
                        case 0 -> radio.getVolume();
                        case 1 -> radio.isLocked() ? 1 : 0;
                        default -> 0;
                    };
                }

                // Client side: use the values that were synced via set()
                return switch (index) {
                    case 0 -> clientVolume;
                    case 1 -> clientLocked;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                // Client receives server values here
                switch (index) {
                    case 0 -> clientVolume = value;
                    case 1 -> clientLocked = value;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
        this.addDataSlots(this.data);

        // Slot coords relative to GUI top-left (256x166 panel)
        final int hotbarX = 49;  // centered 9-slot row
        final int hotbarY = 6;   // top row in the art

        final int cardX = 45;    // single square under left icons
        final int cardY = 60;

        // Memory card slot
        this.addSlot(new Slot(radio.getInventory(), 0, cardX, cardY) {
            @Override public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.MEMORY_CARD.get());
            }
        });

        // Hotbar row (9 slots)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inv, col, hotbarX + col * 18, hotbarY));
        }
    }

    public int getVolume() { return data.get(0); }
    public boolean isLocked() { return data.get(1) == 1; }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack empty = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) return empty;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index == SLOT_MEMORY_CARD) {
            if (!this.moveItemStackTo(stack, SLOT_HOTBAR_START, SLOT_HOTBAR_END, false)) return empty;
        } else {
            if (!this.moveItemStackTo(stack, SLOT_MEMORY_CARD, SLOT_MEMORY_CARD + 1, false)) return empty;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return copy;
    }
}
