package me.michiyo.openfm.neoforge;

import me.michiyo.openfm.OpenFM;
import me.michiyo.openfm.platform.OpenFMRegistrar;
import me.michiyo.openfm.platform.RegistrySupplier;
import me.michiyo.openfm.world.menu.RadioMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

final class NeoForgeRegistrar implements OpenFMRegistrar {
    private final DeferredRegister.Blocks blocks = DeferredRegister.createBlocks(OpenFM.MODID);
    private final DeferredRegister.Items items = DeferredRegister.createItems(OpenFM.MODID);
    private final DeferredRegister<BlockEntityType<?>> blockEntities =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, OpenFM.MODID);
    private final DeferredRegister<MenuType<?>> menus = DeferredRegister.create(Registries.MENU, OpenFM.MODID);
    private final DeferredRegister<CreativeModeTab> tabs =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, OpenFM.MODID);

    @Override
    public RegistrySupplier<Block> registerBlock(String name, Supplier<? extends Block> factory) {
        var holder = blocks.register(name, factory);
        return holder::get;
    }

    @Override
    public RegistrySupplier<Item> registerItem(String name, Supplier<? extends Item> factory) {
        var holder = items.register(name, factory);
        return holder::get;
    }

    @Override
    public <T extends BlockEntity> RegistrySupplier<BlockEntityType<T>> registerBlockEntity(
            String name, BlockEntityFactory<T> factory, RegistrySupplier<? extends Block>... blocks) {
        var holder = blockEntities.register(name, () -> {
            Block[] validBlocks = java.util.Arrays.stream(blocks)
                    .map(RegistrySupplier::get)
                    .toArray(Block[]::new);
            return new BlockEntityType<>(factory::create, validBlocks);
        });
        return holder::get;
    }

    @Override
    public RegistrySupplier<MenuType<RadioMenu>> registerRadioMenu(String name) {
        var holder = menus.register(name, () -> IMenuTypeExtension.create(RadioMenu::new));
        return holder::get;
    }

    @Override
    public RegistrySupplier<CreativeModeTab> registerCreativeTab(
            String name,
            Component title,
            Supplier<ItemStack> icon,
            CreativeTabEntries displayItems) {
        var holder = tabs.register(name, () -> CreativeModeTab.builder()
                .title(title)
                .icon(icon)
                .displayItems((parameters, output) -> displayItems.accept(output::accept))
                .build());
        return holder::get;
    }

    void bind(IEventBus bus) {
        blocks.register(bus);
        items.register(bus);
        blockEntities.register(bus);
        menus.register(bus);
        tabs.register(bus);
    }
}
