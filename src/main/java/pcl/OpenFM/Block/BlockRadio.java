package pcl.OpenFM.Block;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
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
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pcl.OpenFM.OpenFM;
import pcl.OpenFM.GUI.GuiRadio;
import pcl.OpenFM.GUI.GuiRadioBase;
import pcl.OpenFM.TileEntity.TileEntityRadio;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cpw.mods.fml.common.Optional;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;

@Optional.InterfaceList({
	@Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheralProvider", modid = "ComputerCraft"),
})
public class BlockRadio extends Block implements ITileEntityProvider, IPeripheralProvider {

	@SideOnly(Side.CLIENT)
	public static IIcon sideIcon;
	@SideOnly(Side.CLIENT)
	public static IIcon frontIcon;
	public GuiRadioBase guiRadio;
	private Random random;
	
	public BlockRadio()
	{
		super(Material.wood);
		setHardness(2.0F);
		setResistance(10.0F);
		setBlockName("OpenFM.Radio");
		setStepSound(Block.soundTypeWood);
		random = new Random();
	}


	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister ir)
	{
		sideIcon = ir.registerIcon("openfm:radio_side");
		frontIcon = ir.registerIcon("openfm:radio_front");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta)
	{
		switch (side)
		{
		case 2: 
			if (meta == 1) {
				return frontIcon;
			}
			return sideIcon;
		case 3: 
			if (meta == 0 || meta == 3)
				return frontIcon;

			return sideIcon;
		case 4: 
			if (meta == 4){
				return frontIcon;
			}
			return sideIcon;
		case 5: 
			if (meta == 2){
				return frontIcon;
			}
			return sideIcon;
		}
		return sideIcon;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int metadata, float clickX, float clickY, float clickZ) {
			TileEntity tileEntity = world.getTileEntity(x, y, z);
			if (tileEntity == null || player.isSneaking()) {
				return false;
			}
			player.openGui(OpenFM.instance, 0, world, x, y, z);
			return true;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int p_149749_6_) {
		TileEntityRadio t = (TileEntityRadio)world.getTileEntity(x, y, z);
		if(t==null)
			return;
		
		dropContent(t, world, t.xCoord, t.yCoord, t.zCoord);
		ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		if (t instanceof TileEntityRadio) {
			if (t.stations.size() > 0) {
				ItemStack stack = new ItemStack(block, 1);

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
				world.spawnEntityInWorld(new EntityItem(world, x, y, z, items.get(0)));
				world.setBlock(x, y, z, Blocks.air);
			}
		}
	}

	@Override
	public Item getItemDropped(int meta, Random random, int fortune) {
		return null;
	}

	@Override
	public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLiving, ItemStack par6ItemStack) {
		int l = MathHelper.floor_double(par5EntityLiving.rotationYaw * 4.0F / 360.0F + 0.5D) & 0x3;
		par1World.setBlockMetadataWithNotify(par2, par3, par4, l + 1, 2);
		TileEntity te = par1World.getTileEntity(par2, par3, par4);
		((TileEntityRadio) te).owner = par5EntityLiving.getUniqueID().toString();
	}

	public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side) {
		return true;
	}

	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		boolean flag = world.isBlockIndirectlyGettingPowered(x, y, z);
		try {
			Side side = FMLCommonHandler.instance().getEffectiveSide();
			if (block.canProvidePower()) {
				TileEntity tileEntity;
				if (side == Side.SERVER) {
					tileEntity = MinecraftServer.getServer().getEntityWorld().getTileEntity(x, y, z);
				} else {
					tileEntity = FMLClientHandler.instance().getClient().theWorld.getTileEntity(x, y, z);
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


	// IPeripheralProvider
	@Optional.Method(modid = "ComputerCraft")
	@Override
	public IPeripheral getPeripheral(World world, int x, int y, int z, int side) {
		TileEntity te = world.getTileEntity(x, y, z);

		if(te instanceof TileEntityRadio)
			return (IPeripheral)te;

		return null;
	}
}
