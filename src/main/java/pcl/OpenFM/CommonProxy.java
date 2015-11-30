package pcl.OpenFM;

import pcl.OpenFM.Handler.RadioContainer;
import pcl.OpenFM.TileEntity.TileEntityRadio;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class CommonProxy implements IGuiHandler {
  public void registerRenderers() {}
  
  public void initTileEntities() {}
  
  	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		TileEntity te = world.getTileEntity(x, y, z);
		if (te != null && te instanceof TileEntityRadio) {
			TileEntityRadio icte = (TileEntityRadio) te;
			return new RadioContainer(player.inventory, icte);
		} else {
			return null;
		}
	}
  
}


