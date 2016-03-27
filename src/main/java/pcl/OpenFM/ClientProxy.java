package pcl.OpenFM;

import cpw.mods.fml.client.registry.ClientRegistry;
import pcl.OpenFM.TileEntity.RadioRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class ClientProxy extends CommonProxy {

	public void registerRenderers() {
		TileEntitySpecialRenderer radioRenderer = new RadioRenderer();
		ClientRegistry.bindTileEntitySpecialRenderer(pcl.OpenFM.TileEntity.TileEntityRadio.class, radioRenderer);
		OpenFM.logger.info("Registering TESR");
	}

	public void initTileEntities() {}
}