package pcl.OpenFM;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pcl.OpenFM.TileEntity.RadioRenderer;
import pcl.OpenFM.TileEntity.TileEntityRadio;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;

public class ClientProxy extends CommonProxy {

	
	public void registerRenderers() {
		TileEntitySpecialRenderer<TileEntityRadio> radioRenderer = new RadioRenderer();
		ClientRegistry.bindTileEntitySpecialRenderer(pcl.OpenFM.TileEntity.TileEntityRadio.class, radioRenderer);
		OpenFM.logger.info("Registering TESR");		
	}
	
	@Override
	public void registerItemRenderers() {
		registerBlockItem(ContentRegistry.blockRadio, 0, "radio");
		registerBlockItem(ContentRegistry.blockSpeaker, 0, "speaker");
		registerItem(ContentRegistry.itemMemoryCard, "memorycard");
		registerItem(ContentRegistry.itemRadioTuner, "radiotuner");
	}
	
	public static void registerBlockItem(final Block block, int meta, final String blockName)
    {
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), meta, new ModelResourceLocation(OpenFM.MODID + ":" + blockName, "inventory"));
		OpenFM.logger.info("Registering " + blockName + " Item Renderer");
    }
	
	public static void registerItem(final Item item, final String itemName)
    {
		ModelLoader.setCustomModelResourceLocation(item,  0, new ModelResourceLocation(OpenFM.MODID + ":" + itemName, "inventory"));
		OpenFM.logger.info("Registering " + itemName + " Item Renderer");
    }
	
	public void initTileEntities() {}
}