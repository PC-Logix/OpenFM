package pcl.OpenFM.Items;

import pcl.OpenFM.OpenFM;
import pcl.OpenFM.TileEntity.TileEntityRadio;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class ItemBlockRadio extends ItemBlock {

	public ItemBlockRadio(Block p_i45328_1_) {
		super(p_i45328_1_);
		// TODO Auto-generated constructor stub
	}
	
	@Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
    {
        if(super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata))
        {
        	TileEntityRadio tileEntity = (TileEntityRadio) world.getTileEntity(x, y, z);
        	if (stack.hasTagCompound()) {
            	tileEntity.streamURL = stack.getTagCompound().getString("streamurl");
            	tileEntity.owner = player.getUniqueID().toString();
            	tileEntity.setStationCount(stack.getTagCompound().getInteger("stationCount"));
        		for(int i = 0; i < stack.getTagCompound().getInteger("stationCount"); i++)
        		{
        			tileEntity.stations.add(stack.getTagCompound().getString("station" + i));
        		}
        	} 
        }
        return true;
    }
	
}
