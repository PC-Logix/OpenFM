package pcl.OpenFM.Block;
 
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pcl.OpenFM.Block.Gui.GuiRadio;
import pcl.OpenFM.Block.Gui.NGuiRadio;
import pcl.OpenFM.TileEntity.TileEntityRadio;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockRadio extends Block implements ITileEntityProvider {

    @SideOnly(Side.CLIENT)
    public static IIcon sideIcon;
    @SideOnly(Side.CLIENT)
    public static IIcon rearIcon;

    public BlockRadio() {
        super(Material.wood);
        setHardness(2.0F);
        setResistance(10.0F);
        setBlockName("Radio");
        setStepSound(Block.soundTypeWood);
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister ir) {
        sideIcon = ir.registerIcon("openfm:radio_side");
        rearIcon = ir.registerIcon("openfm:radio_rear");
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

    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
        Side side = FMLCommonHandler.instance().getEffectiveSide();
        if (side == Side.CLIENT) {
            openGUI(world, x, y, z);
        }
        return true;
    }

    @SideOnly(Side.CLIENT)
    private void openGUI(World world, int x, int y, int z) {
        TileEntityRadio ter = (TileEntityRadio) world.getTileEntity(x, y, z);
        GuiRadio guiRadio = new NGuiRadio(ter);
        Minecraft.getMinecraft().displayGuiScreen(guiRadio);
    }

    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack) {
        int l = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 0x3;
        world.setBlockMetadataWithNotify(x, y, z, l + 1, 2);
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
                ((TileEntityRadio) tileEntity).setRedstoneInput(flag);
            }
        } catch (Exception localException) {
        }
    }

    public boolean shouldCheckWeakPower(IBlockAccess world, int x, int y, int z, int side) {
        return true;
    }

    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityRadio(world);
    }
}