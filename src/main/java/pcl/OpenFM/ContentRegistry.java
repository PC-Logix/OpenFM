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
	public static void registerBlocks()
	{
		OpenFM.blockRadio = new BlockRadio();
		GameRegistry.registerBlock(OpenFM.blockRadio, "Radio");
		GameRegistry.registerTileEntity(TileEntityRadio.class, "OpenFMRadio");
		GameRegistry.addRecipe(new ItemStack(OpenFM.blockRadio), new Object[] { "  y", "xyx", "xzx", 
			Character.valueOf('x'), new ItemStack(Blocks.planks), 
			Character.valueOf('y'), new ItemStack(Items.iron_ingot), 
			Character.valueOf('z'), new ItemStack(Items.diamond) });

		OpenFM.blockRadio.setCreativeTab(OpenFM.creativeTab);

		OpenFM.blockSpeaker = new BlockSpeaker();
		GameRegistry.registerBlock(OpenFM.blockSpeaker, "Speaker");
		GameRegistry.registerTileEntity(TileEntitySpeaker.class, "OpenFMSpeaker");
		GameRegistry.addRecipe(new ItemStack(OpenFM.blockSpeaker), new Object[] { "xxx", "xyx", "xzx", 
			Character.valueOf('x'), new ItemStack(Blocks.planks), 
			Character.valueOf('y'), new ItemStack(Items.iron_ingot), 
			Character.valueOf('z'), new ItemStack(Items.redstone) });

		OpenFM.blockSpeaker.setCreativeTab(OpenFM.creativeTab);    

		OpenFM.itemRadioTuner = new pcl.OpenFM.Items.ItemOpenFMTuner();
		GameRegistry.registerItem(OpenFM.itemRadioTuner, "OpenFMRadioTuner");
		GameRegistry.addRecipe(new ItemStack(OpenFM.itemRadioTuner), new Object[] { "  x", "  y", "  z", 
			Character.valueOf('x'), new ItemStack(Items.redstone), 
			Character.valueOf('y'), new ItemStack(Items.redstone), 
			Character.valueOf('z'), new ItemStack(Items.stick) });

		OpenFM.itemRadioTuner.setCreativeTab(OpenFM.creativeTab);
	}

	public static void registerItems() {}


	public static void registerTabs()
	{
		OpenFM.creativeTab = new CreativeTabs("tabOpenFM")
		{
			@SideOnly(Side.CLIENT)
			public Item getTabIconItem() {
				return Item.getItemFromBlock(OpenFM.blockRadio);
			}

			@SideOnly(Side.CLIENT)
			public String getTranslatedTabLabel() {
				return StatCollector.translateToLocal("itemGroup.tabRadioMod");
			}
		};
	}

	public static boolean checkBlock(World w, int x, int y, int z) {
		if ((w.getBlock(x, y, z) instanceof BlockRadio)) {
			return true;
		}
		return false;
	}
}