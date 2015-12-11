package pcl.OpenFM.Block;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import pcl.OpenFM.TileEntity.TileEntitySpeaker;

public class BlockSpeaker extends Block implements ITileEntityProvider {

	public BlockSpeaker() {
		super(Material.wood);
		setHardness(2.0F);
		setResistance(10.0F);
		setUnlocalizedName("OpenFM.Speaker");
		setStepSound(Block.soundTypeWood);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
		int dir = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 0x3;
        world.setBlockState(pos, state);
	}

	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileEntitySpeaker();
	}
}