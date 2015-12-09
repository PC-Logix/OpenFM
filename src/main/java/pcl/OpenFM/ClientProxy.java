package pcl.OpenFM;

import cpw.mods.fml.client.registry.ClientRegistry;
import pcl.OpenFM.TileEntity.RadioContainer;
import pcl.OpenFM.TileEntity.RadioRenderer;
import pcl.OpenFM.TileEntity.TileEntityRadio;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ClientProxy extends CommonProxy {

	public void registerRenderers() {
		TileEntitySpecialRenderer radioRenderer = new RadioRenderer();
		ClientRegistry.bindTileEntitySpecialRenderer(pcl.OpenFM.TileEntity.TileEntityRadio.class, radioRenderer);
		OpenFM.logger.info("Registering TESR");
	}

	public void initTileEntities() {}
}