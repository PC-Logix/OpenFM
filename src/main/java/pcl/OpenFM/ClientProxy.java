package pcl.OpenFM;

import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import pcl.OpenFM.TileEntity.RadioRenderer;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;

public class ClientProxy extends CommonProxy {

	
	public void registerRenderers() {
		TileEntitySpecialRenderer radioRenderer = new RadioRenderer();
		ClientRegistry.bindTileEntitySpecialRenderer(pcl.OpenFM.TileEntity.TileEntityRadio.class, radioRenderer);
		OpenFM.logger.info("Registering TESR");		
	}

	@Override
	public void registerItemRenderers() {
		registerItem(ContentRegistry.blockRadio, 0, "Radio");
		
	}
	
	public static void registerItem(final Block block, int meta, final String blockName)
    {
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), meta, new ModelResourceLocation(OpenFM.MODID + ":" + blockName, "inventory"));
		OpenFM.logger.info("Registering " + blockName + " Item Renderer");
    }
	
	public void initTileEntities() {}
}