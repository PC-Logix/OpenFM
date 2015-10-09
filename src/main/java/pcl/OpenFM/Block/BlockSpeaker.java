package pcl.OpenFM.Block;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import pcl.OpenFM.TileEntity.TileEntitySpeaker;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockSpeaker extends Block implements ITileEntityProvider {
	@SideOnly(Side.CLIENT)
	public static IIcon topIcon;
	@SideOnly(Side.CLIENT)
	public static IIcon bottomIcon;
	@SideOnly(Side.CLIENT)
	public static IIcon sideIcon;
	@SideOnly(Side.CLIENT)
	public static IIcon rearIcon;
	@SideOnly(Side.CLIENT)
	public static IIcon backIcon;

	public BlockSpeaker()
	{
		super(Material.wood);
		setHardness(2.0F);
		setResistance(10.0F);
		setBlockName("Speaker");
		setStepSound(Block.soundTypeWood);
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister ir)
	{
		sideIcon = ir.registerIcon("openfm:speaker_side");
		rearIcon = ir.registerIcon("openfm:speaker_rear");
	}


	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int i, int j)
	{
		switch (i)
		{
		case 2: 
			if (j == 1)
			{
				return rearIcon;
			}


			return sideIcon;



		case 3: 
			if (j == 0)
				return rearIcon;
			if (j == 3) {
				return rearIcon;
			}
			return sideIcon;

		case 4: 
			if (j == 4)
			{
				return rearIcon;
			}


			return sideIcon;


		case 5: 
			if (j == 2)
			{
				return rearIcon;
			}


			return sideIcon;
		}

		return sideIcon;
	}



	public void onBlockClicked(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLiving, ItemStack par6ItemStack) {}



	public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLiving, ItemStack par6ItemStack)
	{
		int l = MathHelper.floor_double(par5EntityLiving.rotationYaw * 4.0F / 360.0F + 0.5D) & 0x3;
		par1World.setBlockMetadataWithNotify(par2, par3, par4, l + 1, 2);
	}

	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntitySpeaker();
	}
}


