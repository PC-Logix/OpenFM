/**
 * 
 */
package pcl.OpenFM.GUI;

import pcl.OpenFM.Handler.RadioContainer;
import pcl.OpenFM.TileEntity.TileEntityRadio;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
/**
 * @author Caitlyn
 *
 */
public class OFMGUIHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		if (id == 0) {
			TileEntity tileEntity = world.getTileEntity(x, y, z);
			if (tileEntity instanceof TileEntityRadio) {
				return new RadioContainer(player.inventory, (TileEntityRadio) tileEntity);
			}
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		if (id == 0) {
			TileEntity tileEntity = world.getTileEntity(x, y, z);
			if (tileEntity instanceof TileEntityRadio) {
				return new GuiRadio(player.inventory, (TileEntityRadio) tileEntity);
			}
		}
		return null;
	}

}