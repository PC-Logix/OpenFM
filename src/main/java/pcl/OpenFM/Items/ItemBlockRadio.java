package pcl.OpenFM.Items;

import pcl.OpenFM.TileEntity.TileEntityRadio;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class ItemBlockRadio extends ItemBlock {

	public ItemBlockRadio(Block p_i45328_1_) {
		super(p_i45328_1_);
		//setUnlocalizedName("Radio");
		// TODO Auto-generated constructor stub
	}
	
    /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     
    public String getUnlocalizedName(ItemStack stack)
    {
        return "Radio";
    }
	*/
	
	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState state) {
        if(super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, state))
        {
        	TileEntityRadio tileEntity = (TileEntityRadio) world.getTileEntity(pos);
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
