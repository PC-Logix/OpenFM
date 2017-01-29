package pcl.OpenFM.Items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import pcl.OpenFM.OFMConfiguration;
import pcl.OpenFM.Block.BlockRadio;
import pcl.OpenFM.Block.BlockSpeaker;
import pcl.OpenFM.TileEntity.TileEntityRadio;
import pcl.OpenFM.misc.Speaker;
import pcl.OpenFM.network.PacketHandler;
import pcl.OpenFM.network.message.MessageRadioAddSpeaker;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashMap;

public class ItemTuner extends Item {

    public static HashMap<ItemStack, Speaker> boundSpeakers = new HashMap<ItemStack, Speaker>();

    public ItemTuner() {
        setMaxStackSize(1);
        setUnlocalizedName("OpenFM.Tuner");
    }

    @Override
    public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        // Item can only be used by client.
        if (FMLCommonHandler.instance().getEffectiveSide() != Side.SERVER) {
            // If clicked block is a speaker, keep a reference to it.
            if ((world.getBlockState(pos).getBlock() instanceof BlockSpeaker)) {
                // TODO: one speaker should only be able to be linked to exactly one radio.
                boundSpeakers.put(stack, new Speaker(pos.getX(), pos.getY(), pos.getZ(), world));
                player.addChatMessage(new TextComponentString(I18n.translateToLocal("msg.OpenFM.selected_speaker")));
            } else if ((world.getBlockState(pos).getBlock() instanceof BlockRadio)) {
                // Else, it it's a radio, try to link it to the speaker.
                if (boundSpeakers.get(stack) != null) {
                    Speaker speaker = boundSpeakers.get(stack);
                    TileEntityRadio radio = (TileEntityRadio) player.getEntityWorld().getTileEntity(pos);
                    // Check if the speaker can be added to the radio.
                    int canAdd = radio.canAddSpeaker(player.getEntityWorld(), speaker.x, speaker.y, speaker.z);
                    if (canAdd == 0) {
                        // It can, so send a packet.
                        player.addChatMessage(new TextComponentString(I18n.translateToLocal("msg.OpenFM.added_speaker")));
                        PacketHandler.INSTANCE.sendToServer(new MessageRadioAddSpeaker(radio, speaker).wrap());
                    } else if (canAdd == 1) {
                        // Too many speakers linked.
                        player.addChatMessage(new TextComponentString(I18n.translateToLocal("msg.OpenFM.failed_adding_speaker_limit").replaceFirst("10", Integer.toString(OFMConfiguration.maxSpeakers))));
                    } else if (canAdd == 2) {
                        // Speaker is already linked.
                        player.addChatMessage(new TextComponentString(I18n.translateToLocal("msg.OpenFM.failed_adding_speaker_exists")));
                    }
                } else {
                    // Apparently no speaker is bound.
                    player.addChatMessage(new TextComponentString(I18n.translateToLocal("msg.OpenFM.failed_adding_speaker_not_selected")));
                }
            }
        }
        return EnumActionResult.SUCCESS;
    }
}