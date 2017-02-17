package pcl.OpenFM.Items;

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
import pcl.OpenFM.network.message.MessageRadioAddSpeaker;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

import java.util.HashMap;

public class ItemTuner extends Item {

    public static HashMap<ItemStack, Speaker> boundSpeakers = new HashMap<ItemStack, Speaker>();

    public ItemTuner() {
        setMaxStackSize(1);
        setUnlocalizedName("OpenFM.Tuner");
        setTextureName("openfm:ItemTuner");
    }

    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int hitSide, float hitX, float hitY, float hitZ) {
        Side side = FMLCommonHandler.instance().getEffectiveSide();

        // Item can only be used by client.
        if (side != Side.SERVER) {

            // If clicked block is a speaker, keep a reference to it.
            if ((world.getBlock(x, y, z) instanceof BlockSpeaker)) {

                // TODO: one speaker should only be able to be linked to exactly one radio.
                boundSpeakers.put(stack, new Speaker(x, y, z, world));
                player.addChatMessage(new ChatComponentTranslation("msg.OpenFM.selected_speaker"));

            } else if ((world.getBlock(x, y, z) instanceof BlockRadio)) {

                // Else, it it's a radio, try to link it to the speaker.
                if (boundSpeakers.get(stack) != null) {
                    Speaker speaker = boundSpeakers.get(stack);
                    TileEntityRadio radio = (TileEntityRadio) player.getEntityWorld().getTileEntity(x, y, z);

                    // Check if the speaker can be added to the radio.
                    int canAdd = radio.canAddSpeaker(player.getEntityWorld(), speaker.x, speaker.y, speaker.z);
                    if (canAdd == 0) {
                        // It can, so send a packet.
                        player.addChatMessage(new ChatComponentTranslation("msg.OpenFM.added_speaker"));
                        PacketHandler.INSTANCE.sendToServer(new MessageRadioAddSpeaker(radio, speaker).wrap());
                    } else if (canAdd == 1) {
                        // Too many speakers linked.
                        player.addChatMessage(new ChatComponentTranslation("msg.OpenFM.failed_adding_speaker_limit"));
                    } else if (canAdd == 2) {
                        // Speaker is already linked.
                        player.addChatMessage(new ChatComponentTranslation("msg.OpenFM.failed_adding_speaker_exists"));
                    }

                } else {
                    // Apparently no speaker is bound.
                    player.addChatMessage(new ChatComponentTranslation("msg.OpenFM.failed_adding_speaker_not_selected"));
                }
            }

        }
        return true;
    }
}
