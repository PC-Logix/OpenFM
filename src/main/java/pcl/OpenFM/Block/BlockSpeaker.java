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
	public static IIcon sideIcon;
	@SideOnly(Side.CLIENT)
	public static IIcon rearIcon;

	public BlockSpeaker() {
		super(Material.wood);
		setHardness(2.0F);
		setResistance(10.0F);
		setBlockName("OpenFM.Speaker");
		setStepSound(Block.soundTypeWood);
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister ir) {
		sideIcon = ir.registerIcon("openfm:speaker_side");
		rearIcon = ir.registerIcon("openfm:speaker_rear");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {

        switch (side) {
            case 2:
                if (meta == 1) {
                    return rearIcon;
                }
                return sideIcon;
            case 3:
                if (meta == 0 || meta == 3) {
                    return rearIcon;
                }
                return sideIcon;
            case 4:
                if (meta == 4) {
                    return rearIcon;
                }
                return sideIcon;
            case 5:
                if (meta == 2) {
                    return rearIcon;
                }
                return sideIcon;
        }
        return sideIcon;
    }

	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack) {
		int dir = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 0x3;
        world.setBlockMetadataWithNotify(x, y, z, dir + 1, 2);
	}

	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileEntitySpeaker();
	}
}