package pcl.OpenFM;

import net.minecraft.block.Block;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import pcl.OpenFM.Block.BlockRadio;
import pcl.OpenFM.Block.BlockSpeaker;
import pcl.OpenFM.Handler.OFMBreakEvent;
import pcl.OpenFM.Items.ItemMemoryCard;
import pcl.OpenFM.Items.ItemTuner;
import pcl.OpenFM.TileEntity.TileEntityRadio;
import pcl.OpenFM.TileEntity.TileEntitySpeaker;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@SuppressWarnings("deprecation")
@Mod.EventBusSubscriber
public class ContentRegistry {
	
	
    // Blocks
    public static Block blockRadio;
    public static Block blockSpeaker;

    // Items
    public static Item itemRadioTuner;
    public static Item itemMemoryCard;

    // Tabs
    public static CreativeTabs creativeTab;

	public static Item init(Item item, String name)
	{
		return item.setUnlocalizedName(name).setRegistryName("openfm:" + name);
	}
    
	public static Block init(Block block, String name)
	{
		return block.setUnlocalizedName(name).setRegistryName("openfm:" + name);
	}
	
    public static void preInit() {
    	blockRadio =init(new BlockRadio(), "radio");
    	GameRegistry.registerTileEntity(TileEntityRadio.class, "OpenFMRadio");
    	blockSpeaker = init(new BlockSpeaker(), "speaker");
		GameRegistry.registerTileEntity(TileEntitySpeaker.class, "OpenFMSpeaker");
		
		itemRadioTuner = init(new ItemTuner(), "radiotuner");
		itemMemoryCard = init(new ItemMemoryCard(), "memorycard");
		registerEvents();
		registerTabs();
		blockRadio.setCreativeTab(creativeTab);
		blockSpeaker.setCreativeTab(creativeTab);
		itemRadioTuner.setCreativeTab(creativeTab);
		itemMemoryCard.setCreativeTab(creativeTab);
    }

	
	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> register) {
		register.getRegistry().register(blockRadio);
		register.getRegistry().register(blockSpeaker);
	}
	
	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> register) {
		register.getRegistry().register(itemRadioTuner);
		register.getRegistry().register(itemMemoryCard);
		register.getRegistry().register(new ItemBlock(blockRadio).setCreativeTab(creativeTab).setRegistryName(blockRadio.getRegistryName()));
		register.getRegistry().register(new ItemBlock(blockSpeaker).setCreativeTab(creativeTab).setRegistryName(blockSpeaker.getRegistryName()));

	}

	public static void registerEvents() {
		MinecraftForge.EVENT_BUS.register(new OFMBreakEvent());
	}

	public static void registerTabs() {

		creativeTab = new CreativeTabs("tabOpenFM") {
			@SideOnly(Side.CLIENT)
			public ItemStack getTabIconItem() {
				return new ItemStack(Item.getItemFromBlock(blockRadio));
			}

			@SideOnly(Side.CLIENT)
			public String getTranslatedTabLabel() {
				return I18n.translateToLocal("itemGroup.OpenFM.tabOpenFM");
			}
		};
	}

	public static boolean checkBlock(World w, BlockPos pos) {
		return (w.getBlockState(pos).getBlock() instanceof BlockRadio);
	}
}