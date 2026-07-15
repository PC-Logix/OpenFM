package me.michiyo.openfm;

import me.michiyo.openfm.config.OpenFMClientConfig;
import me.michiyo.openfm.net.Net;
import me.michiyo.openfm.reg.ModBlockEntities;
import me.michiyo.openfm.reg.ModBlocks;
import me.michiyo.openfm.reg.ModItems;
import me.michiyo.openfm.reg.ModMenus;
import me.michiyo.openfm.reg.ModCreativeTabs;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(OpenFM.MODID)
public class OpenFM {
    public static final String MODID = "openfm";

    public OpenFM() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.REG.register(bus);
        ModItems.REG.register(bus);
        ModBlockEntities.REG.register(bus);
        ModMenus.REG.register(bus);
        ModCreativeTabs.REG.register(bus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, OpenFMClientConfig.SPEC);

        Net.register();

        if (ModList.get().isLoaded("computercraft")) {
            MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class,
                    me.michiyo.openfm.compat.cc.ComputerCraftCompat::attachPeripherals
            );
        }
    }
}
