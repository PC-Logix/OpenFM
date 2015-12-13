package pcl.OpenFM;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import pcl.OpenFM.Block.BlockRadio;
import pcl.OpenFM.Block.BlockSpeaker;
import pcl.OpenFM.Handler.OFMBreakEvent;
import pcl.OpenFM.Items.ItemBlockRadio;
import pcl.OpenFM.Items.ItemMemoryCard;
import pcl.OpenFM.Items.ItemTuner;
import pcl.OpenFM.TileEntity.TileEntityRadio;
import pcl.OpenFM.TileEntity.TileEntitySpeaker;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.common.Loader;

public class ContentRegistry {
	
	
    // Blocks
    public static Block blockRadio;
    public static Block blockSpeaker;

    // Items
    public static Item itemRadioTuner;
    public static Item itemMemoryCard;

    // Tabs
    public static CreativeTabs creativeTab;

    // Called on mod init()
	public static void init() {
        registerTabs();
        registerBlocks();
        registerItems();
        registerEvents();
	}
	
	public static void registerBlocks() {

		blockRadio = new BlockRadio();
		//Rename these to lowercase and keep in world
		// GameRegistry.addSubstitutionAlias("minecraft:end_stone", GameRegistry.Type.BLOCK, testBlock);
		GameRegistry.registerBlock(blockRadio, ItemBlockRadio.class, "Radio");
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

        itemRadioTuner = new ItemTuner();
        GameRegistry.registerItem(itemRadioTuner, "RadioTuner");
        itemRadioTuner.setCreativeTab(creativeTab);
        GameRegistry.addRecipe(new ItemStack(itemRadioTuner), "  x", "  y", "  z",
                'x', new ItemStack(Items.redstone),
                'y', new ItemStack(Items.redstone),
                'z', new ItemStack(Items.stick));
        
        itemMemoryCard = new ItemMemoryCard();
        GameRegistry.registerItem(itemMemoryCard, "MemoryCard");
        itemMemoryCard.setCreativeTab(creativeTab);
        GameRegistry.addRecipe(new ItemStack(itemMemoryCard), "  x", "  y", "  z",
                'x', new ItemStack(Items.redstone),
                'y', new ItemStack(Items.redstone),
                'z', new ItemStack(Items.paper));
        
	}

	public static void registerEvents() {
		MinecraftForge.EVENT_BUS.register(new OFMBreakEvent());
	}

	public static void registerTabs() {

		creativeTab = new CreativeTabs("tabOpenFM") {
			@SideOnly(Side.CLIENT)
			public Item getTabIconItem() {
				return Item.getItemFromBlock(blockRadio);
			}

			@SideOnly(Side.CLIENT)
			public String getTranslatedTabLabel() {
				return StatCollector.translateToLocal("itemGroup.OpenFM.tabOpenFM");
			}
		};
	}

	public static boolean checkBlock(World w, BlockPos pos) {
		return (w.getBlockState(pos).getBlock() instanceof BlockRadio);
	}
}