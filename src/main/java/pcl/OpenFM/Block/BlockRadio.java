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
public class BlockRadio extends Block implements IPeripheralProvider, ITileEntityProvider {

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
		TileEntityRadio t = (TileEntityRadio)world.getTileEntity(pos);
		if(t==null) {
			return;
		}
		
		IItemHandler itemHandler = t.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.NORTH);
		ItemStack stack1 = itemHandler.getStackInSlot(0);
		if (!stack1.isEmpty()) {
			EntityItem item = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), stack1);
			world.spawnEntity(item);
		}

		if (t instanceof TileEntityRadio) {
			if (t.stations.size() > 0) {
				ItemStack stack = new ItemStack(Item.getItemFromBlock(this), 1);

				if (!stack.hasTagCompound()) {
					stack.setTagCompound(new NBTTagCompound());
				}
				if(t.streamURL != null) {
					stack.getTagCompound().setString("streamurl", t.streamURL);
				}
				if(t.getScreenText() != null) {
					stack.getTagCompound().setString("screenText", t.getScreenText());
				}
				stack.getTagCompound().setInteger("screenColor", t.getScreenColor());
				for(int i = 0; i < t.getStationCount(); i++)
				{
					if (t.stations.get(i) != null) {
						stack.getTagCompound().setString("station" + i, t.stations.get(i));
						stack.getTagCompound().setInteger("stationCount", i + 1);
					}
				}
				world.spawnEntity(new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), stack));
				super.breakBlock(world, pos, state);;
			} else {
				ItemStack stack = new ItemStack(Item.getItemFromBlock(this), 1);
				world.spawnEntity(new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), stack));
			}
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

	public void dropContent(IInventory chest, World world, int xCoord, int yCoord, int zCoord) {
		if (chest == null)
			return;

		for (int i1 = 0; i1 < chest.getSizeInventory(); ++i1) {
			ItemStack itemstack = chest.getStackInSlot(i1);

			if (itemstack != null) {
				float offsetX = random.nextFloat() * 0.8F + 0.1F;
				float offsetY = random.nextFloat() * 0.8F + 0.1F;
				float offsetZ = random.nextFloat() * 0.8F + 0.1F;
				EntityItem entityitem;

				for (; itemstack.getCount() > 0; world.spawnEntity(entityitem)) {
					int stackSize = random.nextInt(21) + 10;
					if (stackSize > itemstack.getCount())
						stackSize = itemstack.getCount();

					itemstack.setCount(itemstack.getCount() - stackSize);
					ItemStack stack = new ItemStack(itemstack.getItem(), stackSize, itemstack.getItemDamage());
					if (itemstack.hasTagCompound())
						stack.setTagCompound(itemstack.getTagCompound().copy());
					
					entityitem = new EntityItem(world, (double)((float)xCoord + offsetX), (double)((float)yCoord + offsetY), (double)((float)zCoord + offsetZ), stack);

					float velocity = 0.05F;
					entityitem.motionX = (double)((float)random.nextGaussian() * velocity);
					entityitem.motionY = (double)((float)random.nextGaussian() * velocity + 0.2F);
					entityitem.motionZ = (double)((float)random.nextGaussian() * velocity);
					
				}
			}
		}
	}
	
	public boolean shouldCheckWeakPower(IBlockAccess world, int x, int y, int z, int side) {
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityRadio(world);
	}
	
	// IPeripheralProvider
	@Optional.Method(modid = "computercraft")
	@Override
	public IPeripheral getPeripheral(World world, BlockPos pos, EnumFacing side) {
		TileEntity te = world.getTileEntity(pos);
		OpenFM.logger.info("Looking for TE");
		if(te instanceof TileEntityRadio)
			return (IPeripheral)te;

		return null;
	}
}