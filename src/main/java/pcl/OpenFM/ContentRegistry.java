package pcl.OpenFM;

import net.minecraft.block.Block;
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

    // Blocks
    public static Block blockRadio;
    public static Block blockSpeaker;

    // Items
    public static Item itemRadioTuner;

    // Tabs
    public static CreativeTabs creativeTab;

    // Called on mod init()
	public static void init() {
        registerTabs();
        registerBlocks();
        registerItems();
	}

	public static void registerBlocks() {

		blockRadio = new BlockRadio();
		GameRegistry.registerBlock(blockRadio, "Radio");
		GameRegistry.registerTileEntity(TileEntityRadio.class, "OpenFMRadio");
		GameRegistry.addRecipe(new ItemStack(blockRadio), "  y", "xyx", "xzx",
				'x', new ItemStack(Blocks.planks),
				'y', new ItemStack(Items.iron_ingot),
				'z', new ItemStack(Items.diamond));
		blockRadio.setCreativeTab(creativeTab);

		blockSpeaker = new BlockSpeaker();
		GameRegistry.registerBlock(blockSpeaker, "Speaker");
		GameRegistry.registerTileEntity(TileEntitySpeaker.class, "OpenFMSpeaker");
		GameRegistry.addRecipe(new ItemStack(blockSpeaker), "xxx", "xyx", "xzx",
				'x', new ItemStack(Blocks.planks),
				'y', new ItemStack(Items.iron_ingot),
				'z', new ItemStack(Items.redstone));
		blockSpeaker.setCreativeTab(creativeTab);
	}

	public static void registerItems() {

        itemRadioTuner = new pcl.OpenFM.Items.ItemOpenFMTuner();
        GameRegistry.registerItem(itemRadioTuner, "OpenFMRadioTuner");
        GameRegistry.addRecipe(new ItemStack(itemRadioTuner), "  x", "  y", "  z",
                'x', new ItemStack(Items.redstone),
                'y', new ItemStack(Items.redstone),
                'z', new ItemStack(Items.stick));
        itemRadioTuner.setCreativeTab(creativeTab);
	}

	public static void registerTabs() {

		creativeTab = new CreativeTabs("tabOpenFM") {
			@SideOnly(Side.CLIENT)
			public Item getTabIconItem() {
				return Item.getItemFromBlock(blockRadio);
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