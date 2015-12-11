package pcl.OpenFM;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import pcl.OpenFM.TileEntity.RadioRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;

public class ClientProxy extends CommonProxy {

	
	public void registerRenderers() {
		TileEntitySpecialRenderer radioRenderer = new RadioRenderer();
		ClientRegistry.bindTileEntitySpecialRenderer(pcl.OpenFM.TileEntity.TileEntityRadio.class, radioRenderer);
		OpenFM.logger.info("Registering TESR");
		
		registerItem(Item.getItemFromBlock(ContentRegistry.blockRadio), 0, "openfm:Radio");
		OpenFM.logger.info("Registering Item Renderer");
	}

	public static void registerItem(Item item, int metadata, String itemName)
    {
        ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
        mesher.register(item, metadata, new ModelResourceLocation(itemName, "inventory"));
    }
	
	public void initTileEntities() {}
}