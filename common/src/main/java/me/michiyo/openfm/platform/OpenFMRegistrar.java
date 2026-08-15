package me.michiyo.openfm.platform;

import me.michiyo.openfm.world.menu.RadioMenu;
import me.michiyo.openfm.world.menu.PortablePlayerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public interface OpenFMRegistrar {
    RegistrySupplier<Block> registerBlock(String name, Supplier<? extends Block> factory);

    RegistrySupplier<Item> registerItem(String name, Supplier<? extends Item> factory);

    <T extends net.minecraft.world.level.block.entity.BlockEntity> RegistrySupplier<BlockEntityType<T>> registerBlockEntity(
            String name, BlockEntityFactory<T> factory, RegistrySupplier<? extends Block>... blocks);

    RegistrySupplier<MenuType<RadioMenu>> registerRadioMenu(String name);

    RegistrySupplier<MenuType<PortablePlayerMenu>> registerPortableMenu(String name);

    RegistrySupplier<CreativeModeTab> registerCreativeTab(
            String name,
            Component title,
            Supplier<ItemStack> icon,
            CreativeModeTab.DisplayItemsGenerator displayItems);

    @FunctionalInterface
    interface BlockEntityFactory<T extends net.minecraft.world.level.block.entity.BlockEntity> {
        T create(BlockPos pos, BlockState state);
    }
}
