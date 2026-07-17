package me.michiyo.openfm.fabric;

import me.michiyo.openfm.OpenFM;
import me.michiyo.openfm.platform.OpenFMRegistrar;
import me.michiyo.openfm.platform.RegistrySupplier;
import me.michiyo.openfm.world.menu.RadioMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

final class FabricRegistrar implements OpenFMRegistrar {
    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(OpenFM.MODID, name);
    }

    private static <T> RegistrySupplier<T> fixed(T value) {
        return () -> value;
    }

    @Override
    public RegistrySupplier<Block> registerBlock(String name, Supplier<? extends Block> factory) {
        return fixed(net.minecraft.core.Registry.register(BuiltInRegistries.BLOCK, id(name), factory.get()));
    }

    @Override
    public RegistrySupplier<Item> registerItem(String name, Supplier<? extends Item> factory) {
        return fixed(net.minecraft.core.Registry.register(BuiltInRegistries.ITEM, id(name), factory.get()));
    }

    @Override
    public <T extends BlockEntity> RegistrySupplier<BlockEntityType<T>> registerBlockEntity(
            String name, BlockEntityFactory<T> factory, RegistrySupplier<? extends Block>... blocks) {
        Block[] validBlocks = java.util.Arrays.stream(blocks).map(RegistrySupplier::get).toArray(Block[]::new);
        BlockEntityType<T> type = FabricBlockEntityTypeBuilder.create(factory::create, validBlocks).build();
        return fixed(net.minecraft.core.Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id(name), type));
    }

    @Override
    public RegistrySupplier<MenuType<RadioMenu>> registerRadioMenu(String name) {
        MenuType<RadioMenu> type = new ExtendedScreenHandlerType<>(RadioMenu::new, BlockPos.STREAM_CODEC);
        return fixed(net.minecraft.core.Registry.register(BuiltInRegistries.MENU, id(name), type));
    }

    @Override
    public RegistrySupplier<CreativeModeTab> registerCreativeTab(
            String name,
            Component title,
            Supplier<ItemStack> icon,
            CreativeModeTab.DisplayItemsGenerator displayItems) {
        CreativeModeTab tab = FabricItemGroup.builder()
                .title(title)
                .icon(icon)
                .displayItems(displayItems)
                .build();
        return fixed(net.minecraft.core.Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id(name), tab));
    }
}
