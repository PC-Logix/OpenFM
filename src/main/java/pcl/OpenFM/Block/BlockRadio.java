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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pcl.OpenFM.GUI.GuiRadio;
import pcl.OpenFM.GUI.GuiRadioBase;
import pcl.OpenFM.TileEntity.TileEntityRadio;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockRadio extends Block implements ITileEntityProvider {

	@SideOnly(Side.CLIENT)
	public static IIcon sideIcon;
	@SideOnly(Side.CLIENT)
	public static IIcon frontIcon;
	public GuiRadioBase guiRadio;

	public BlockRadio()
	{
		super(Material.wood);
		setHardness(2.0F);
		setResistance(10.0F);
		setBlockName("Radio");
		setStepSound(Block.soundTypeWood);
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


	public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9)
	{
		Side side = FMLCommonHandler.instance().getEffectiveSide();
		if (side == Side.CLIENT)
		{
			TileEntityRadio ter = (TileEntityRadio)par1World.getTileEntity(par2, par3, par4);
			if (!ter.isLocked || ter.owner.equals(par5EntityPlayer.getUniqueID().toString())) {
				openGUI(par1World, par2, par3, par4);
			}
		}
		return true;
	}

	@SideOnly(Side.CLIENT)
	private void openGUI(World par1World, int par2, int par3, int par4)
	{
		TileEntityRadio ter = (TileEntityRadio)par1World.getTileEntity(par2, par3, par4);
		this.guiRadio = new GuiRadio(ter);
		Minecraft.getMinecraft().displayGuiScreen(this.guiRadio);
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int p_149749_6_) {
		TileEntityRadio t = (TileEntityRadio)world.getTileEntity(x, y, z);
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

	public boolean shouldCheckWeakPower(IBlockAccess world, int x, int y, int z, int side) {
		return true;
	}

	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityRadio(world);
	}
}