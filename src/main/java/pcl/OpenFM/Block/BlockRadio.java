package pcl.OpenFM.Block;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pcl.OpenFM.OpenFM;
import pcl.OpenFM.GUI.GuiRadio;
import pcl.OpenFM.GUI.GuiRadioBase;
import pcl.OpenFM.TileEntity.TileEntityRadio;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.common.Optional;

@Optional.InterfaceList({
	@Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheralProvider", modid = "ComputerCraft"),
})
public class BlockRadio extends Block implements ITileEntityProvider {

	public GuiRadioBase guiRadio;
	private Random random;

	public BlockRadio()
	{
		super(Material.wood);
		setHardness(2.0F);
		setResistance(10.0F);
		//setBlockName("OpenFM.Radio");
		setUnlocalizedName("OpenFM.Radio");
		setStepSound(Block.soundTypeWood);
		random = new Random();
	}


	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
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
		dropContent(t, world, t.getPos().getX(), t.getPos().getY(), t.getPos().getZ());
		ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		if (t instanceof TileEntityRadio) {
			if (t.stations.size() > 0) {
				ItemStack stack = new ItemStack(Item.getItemFromBlock(this), 1);

				if (!stack.hasTagCompound()) {
					stack.setTagCompound(new NBTTagCompound());
				}
				stack.getTagCompound().setString("streamurl", t.streamURL);
				stack.getTagCompound().setString("screenText", t.getScreenText());
				stack.getTagCompound().setInteger("screenColor", t.getScreenColor());
				for(int i = 0; i < t.getStationCount(); i++)
				{
					stack.getTagCompound().setString("station" + i, t.stations.get(i));
					stack.getTagCompound().setInteger("stationCount", i + 1);
				}
				items.add(stack);
				world.spawnEntityInWorld(new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), items.get(0)));
				world.setBlockState(pos, Blocks.air.getDefaultState());
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
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
	{
		return state;
	}

	@Override
	protected BlockState createBlockState()
	{
		return new BlockState(this, new IProperty[] {PROPERTYFACING});
	}

	@Override
	public Item getItemDropped(IBlockState state, Random random, int fortune) {
		return null;
	}

	@Override
	  public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing blockFaceClickedOn, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		EnumFacing enumfacing = (placer == null) ? EnumFacing.NORTH : EnumFacing.fromAngle(placer.rotationYaw);
		TileEntity te = world.getTileEntity(pos);
		//((TileEntityRadio) te).owner = placer.getUniqueID().toString();
		return this.getDefaultState().withProperty(PROPERTYFACING, enumfacing);
	}

	public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side) {
		return true;
	}

	public void onNeighborBlockChange(World world, BlockPos pos, Block block) {
		boolean flag = world.isBlockPowered(pos);
		try {
			Side side = FMLCommonHandler.instance().getEffectiveSide();
			if (block.canProvidePower()) {
				TileEntity tileEntity;
				if (side == Side.SERVER) {
					tileEntity = MinecraftServer.getServer().getEntityWorld().getTileEntity(pos);
				} else {
					tileEntity = FMLClientHandler.instance().getClient().theWorld.getTileEntity(pos);
				}
				((TileEntityRadio)tileEntity).setRedstoneInput(flag);
			}
		}
		catch (Exception localException) { }
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

				for (; itemstack.stackSize > 0; world.spawnEntityInWorld(entityitem)) {
					int stackSize = random.nextInt(21) + 10;
					if (stackSize > itemstack.stackSize)
						stackSize = itemstack.stackSize;

					itemstack.stackSize -= stackSize;
					entityitem = new EntityItem(world, (double)((float)xCoord + offsetX), (double)((float)yCoord + offsetY), (double)((float)zCoord + offsetZ), new ItemStack(itemstack.getItem(), stackSize, itemstack.getItemDamage()));

					float velocity = 0.05F;
					entityitem.motionX = (double)((float)random.nextGaussian() * velocity);
					entityitem.motionY = (double)((float)random.nextGaussian() * velocity + 0.2F);
					entityitem.motionZ = (double)((float)random.nextGaussian() * velocity);

					if (itemstack.hasTagCompound())
						entityitem.getEntityItem().setTagCompound((NBTTagCompound)itemstack.getTagCompound().copy());
				}
			}
		}
	}

	public boolean shouldCheckWeakPower(IBlockAccess world, int x, int y, int z, int side) {
		return true;
	}

	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityRadio(world);
	}
}