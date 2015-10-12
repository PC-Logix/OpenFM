package pcl.OpenFM;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import pcl.OpenFM.Block.BlockRadio;
import pcl.OpenFM.Block.BlockSpeaker;
import pcl.OpenFM.TileEntity.TileEntityRadio;
import pcl.OpenFM.TileEntity.TileEntitySpeaker;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ContentRegistry {

	public static void init() {


	}

	public static void registerBlocks() {

		OpenFM.blockRadio = new BlockRadio();
		GameRegistry.registerBlock(OpenFM.blockRadio, "Radio");
		GameRegistry.registerTileEntity(TileEntityRadio.class, "OpenFMRadio");
		GameRegistry.addRecipe(new ItemStack(OpenFM.blockRadio), "  y", "xyx", "xzx",
				'x', new ItemStack(Blocks.planks),
				'y', new ItemStack(Items.iron_ingot),
				'z', new ItemStack(Items.diamond));

		OpenFM.blockRadio.setCreativeTab(OpenFM.creativeTab);

		OpenFM.blockSpeaker = new BlockSpeaker();
		GameRegistry.registerBlock(OpenFM.blockSpeaker, "Speaker");
		GameRegistry.registerTileEntity(TileEntitySpeaker.class, "OpenFMSpeaker");
		GameRegistry.addRecipe(new ItemStack(OpenFM.blockSpeaker), "xxx", "xyx", "xzx",
				'x', new ItemStack(Blocks.planks),
				'y', new ItemStack(Items.iron_ingot),
				'z', new ItemStack(Items.redstone));

		OpenFM.blockSpeaker.setCreativeTab(OpenFM.creativeTab);    

		OpenFM.itemRadioTuner = new pcl.OpenFM.Items.ItemOpenFMTuner();
		GameRegistry.registerItem(OpenFM.itemRadioTuner, "OpenFMRadioTuner");
		GameRegistry.addRecipe(new ItemStack(OpenFM.itemRadioTuner), "  x", "  y", "  z",
				'x', new ItemStack(Items.redstone),
				'y', new ItemStack(Items.redstone),
				'z', new ItemStack(Items.stick));

		OpenFM.itemRadioTuner.setCreativeTab(OpenFM.creativeTab);
	}

	public static void registerItems() {


	}


	public static void registerTabs() {

		OpenFM.creativeTab = new CreativeTabs("tabOpenFM") {
			@SideOnly(Side.CLIENT)
			public Item getTabIconItem() {
				return Item.getItemFromBlock(OpenFM.blockRadio);
			}

			@SideOnly(Side.CLIENT)
			public String getTranslatedTabLabel() {
				return StatCollector.translateToLocal("itemGroup.tabOpenFM");
			}
		};
	}

	public static boolean checkBlock(World w, int x, int y, int z) {
		return (w.getBlock(x, y, z) instanceof BlockRadio);
	}
}