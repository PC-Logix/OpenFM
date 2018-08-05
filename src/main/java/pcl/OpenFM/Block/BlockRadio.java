package pcl.OpenFM.Block;

import java.util.Random;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import pcl.OpenFM.OpenFM;
import pcl.OpenFM.TileEntity.TileEntityRadio;

@Optional.InterfaceList({
	@Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheralProvider", modid = "computercraft"),
})
public class BlockRadio extends Block implements IPeripheralProvider {

	//public GuiRadioBase guiRadio;
	private Random random;

	public BlockRadio()
	{
		super(Material.WOOD);
		setHardness(2.0F);
		setResistance(10.0F);
		setUnlocalizedName("radio");
		//setStepSound(Block.soundTypeWood);
		random = new Random();
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity == null || player.isSneaking()) {
			return false;
		}
		player.openGui(OpenFM.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		
		if(world.isRemote) {
			return;
		}
		
		TileEntityRadio t = (TileEntityRadio)world.getTileEntity(pos);
		if(t==null) {
			return;
		}
		
		IItemHandler itemHandler = t.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		ItemStack stack1 = itemHandler.getStackInSlot(0);
		if (!stack1.isEmpty()) {
			EntityItem item = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), stack1);
			world.spawnEntity(item);
		}
	}

	public static final PropertyDirection PROPERTYFACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		EnumFacing facing = EnumFacing.getHorizontal(meta);
		return this.getDefaultState().withProperty(PROPERTYFACING, facing);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		EnumFacing facing = (EnumFacing)state.getValue(PROPERTYFACING);
		int facingbits = facing.getHorizontalIndex();
		return facingbits;
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand).withProperty(PROPERTYFACING, placer.getHorizontalFacing());
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
	{
		return state;
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, new IProperty[] {PROPERTYFACING});
	}
	
    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
    	super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    	TileEntity te = worldIn.getTileEntity(pos);
		((TileEntityRadio) te).setOwner(placer.getUniqueID().toString());
    }

	public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side) {
		return true;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		boolean flag = world.isBlockPowered(pos);
		try {
			Side side = FMLCommonHandler.instance().getEffectiveSide();
			if (block.canProvidePower((IBlockState) block.getBlockState().getBaseState())) {
				TileEntity tileEntity = world.getTileEntity(pos);
				((TileEntityRadio)tileEntity).setRedstoneInput(flag);
			}
		}
		catch (Exception localException) { localException.printStackTrace(); }
	}
	
	public boolean shouldCheckWeakPower(IBlockAccess world, int x, int y, int z, int side) {
		return true;
	}

	@Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }
	
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityRadio(world);
	}
	
	// IPeripheralProvider
	@Optional.Method(modid = "computercraft")
	@Override
	public IPeripheral getPeripheral(World world, BlockPos pos, EnumFacing side) {
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityRadio)
			return (IPeripheral)te;

		return null;
	}
}