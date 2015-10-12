package pcl.OpenFM.Items;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import pcl.OpenFM.Block.BlockRadio;
import pcl.OpenFM.Block.BlockSpeaker;
import pcl.OpenFM.TileEntity.TileEntityRadio;
import pcl.OpenFM.misc.Speaker;
import pcl.OpenFM.network.PacketHandler;
import pcl.OpenFM.network.Message.MessageTERadioBlock;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
 
public class ItemOpenFMTuner extends Item {

    public static Speaker currentlySelected = null;

    public ItemOpenFMTuner() {
        setMaxStackSize(1);
        setUnlocalizedName("Tuner");
        setTextureName("openfm:ItemOpenFMTuner");
    }

    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int hitSide, float hitX, float hitY, float hitZ) {
        Side side = FMLCommonHandler.instance().getEffectiveSide();
        if ((!(player.worldObj.getBlock(x, y, z) instanceof BlockSpeaker)) && (!(player.worldObj.getBlock(x, y, z) instanceof BlockRadio))) {
            return false;
        }
        if (side != Side.SERVER) {

            if ((player.worldObj.getBlock(x, y, z) instanceof BlockSpeaker)) {
                currentlySelected = new Speaker(x, y, z, player.worldObj);
                player.addChatMessage(new ChatComponentTranslation("msg.selected_speaker"));
            }
            if ((player.worldObj.getBlock(x, y, z) instanceof BlockRadio)) {
                if (currentlySelected != null) {
                    TileEntityRadio ter = (TileEntityRadio) player.getEntityWorld().getTileEntity(x, y, z);
                    int res = ter.canAddSpeaker(player.getEntityWorld(), currentlySelected.x, currentlySelected.y, currentlySelected.z);
                    if (res == 0) {
                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentTranslation("msg.added_speaker"));
                        PacketHandler.INSTANCE.sendToServer(new MessageTERadioBlock(x, y, z, player.worldObj, ter.streamURL, ter.isPlaying(), ter.volume, 15, currentlySelected.x, currentlySelected.y, currentlySelected.z));

                    } else if (res == 1) {
                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentTranslation("msg.failed_adding_speaker_limit"));
                    } else if (res == 2) {
                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentTranslation("msg.failed_adding_speaker_exists"));
                    } else {
                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentTranslation("msg.failed_adding_speaker_unknown"));
                    }
                } else {
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentTranslation("msg.failed_adding_speaker_not_selected"));
                }
            }

        }
        return true;
    }
}


