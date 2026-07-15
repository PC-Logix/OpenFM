package me.michiyo.openfm.events;

import me.michiyo.openfm.OpenFM;
import me.michiyo.openfm.reg.ModCreativeTabs;
import me.michiyo.openfm.reg.ModItems;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OpenFM.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CreativeTabEvents {

    @SubscribeEvent
    public static void onBuildTabContents(BuildCreativeModeTabContentsEvent e) {
        if (e.getTab() == ModCreativeTabs.OPENFM_TAB.get()) {
            e.accept(ModItems.RADIO_BLOCK_ITEM.get());
            e.accept(ModItems.MEMORY_CARD.get());
        }
    }

    private CreativeTabEvents() {}
}
