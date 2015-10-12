package pcl.OpenFM.TileEntity;

import java.util.ArrayList;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import pcl.OpenFM.OpenFM;
import pcl.OpenFM.Block.BlockSpeaker;
import pcl.OpenFM.misc.Speaker;
import pcl.OpenFM.network.PacketHandler;
import pcl.OpenFM.network.Message.MessageTERadioBlock;
import pcl.OpenFM.player.MP3Player;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.network.NetworkRegistry;

@Optional.InterfaceList(value={
		@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")
})
public class TileEntityRadio extends TileEntity implements SimpleComponent {

    private MP3Player player = null;
    private boolean isPlaying = false;
    private String streamURL = "";
    private World world;
    private float volume = 0.1F;    // TODO: default volume
    private boolean redstoneInput = false;
    private boolean listenToRedstone = false;
    private boolean scheduledRedstoneInput = false;
    private boolean scheduleRedstoneInput = false;
    private ArrayList<Speaker> speakers = new ArrayList<Speaker>();
    private int screenColor = 0x0000FF;

    public TileEntityRadio(World w) {
        world = w;
        if (isPlaying)
            startStream();
    }

    public TileEntityRadio() {
        if (isPlaying) {
            startStream();
        }
    }

    public void setWorld(World w) {
        world = w;
    }


    public void startStream() {
        Side side = FMLCommonHandler.instance().getEffectiveSide();
        if (!OpenFM.playerList.contains(player)) {
            isPlaying = true;
            if (side == Side.CLIENT) {
                player = new MP3Player(streamURL, world, xCoord, yCoord, zCoord);
                OpenFM.playerList.add(player);
            }
        }
    }


    public void stopStream() {
        Side side = FMLCommonHandler.instance().getEffectiveSide();
        if (OpenFM.playerList.contains(player)) {
            if (side == Side.CLIENT) {
                player.stop();
            }
            OpenFM.playerList.remove(player);
            isPlaying = false;
        }
    }


    public boolean isPlaying() {
        return isPlaying;
    }

    @SideOnly(Side.CLIENT)
    public void invalidate() {
        stopStream();
        super.invalidate();
    }

    public void updateEntity() {
        Side side = FMLCommonHandler.instance().getEffectiveSide();
        float vol;
        int th = 0;
        if (side == Side.CLIENT) {
            th += 1;
            if (th >= 10) {
                th = 0;
                for (Speaker s : speakers) {
                    Block sb = getWorldObj().getBlock((int) s.x, (int) s.y, (int) s.z);

                    if (!(sb instanceof BlockSpeaker)) {
                        if (!getWorldObj().getChunkFromBlockCoords((int) s.x, (int) s.z).isChunkLoaded) break;
                        speakers.remove(s);
                        break;
                    }
                }
            }
            if ((Minecraft.getMinecraft().thePlayer != null) && (player != null) &&
                    (!isInvalid())) {
                vol = getClosest();
                if (vol > 10000.0F * volume) {
                    player.setVolume(0.0F);
                } else {
                    float v2 = 10000.0F / vol / 100.0F;
                    if (v2 > 1.0F) {
                        player.setVolume(1.0F * volume * volume);
                    } else {
                        player.setVolume(v2 * volume * volume);
                    }
                }
                if (vol == 0.0F) {
                    invalidate();
                }
            }
        } else {
            if (isPlaying()) {
                th += 1;
                if (th >= 60) {
                    th = 0;
                    for (Speaker s : speakers) {
                        if (!(worldObj.getBlock((int) s.x, (int) s.y, (int) s.z) instanceof BlockSpeaker)) {
                            if (!worldObj.getChunkFromBlockCoords((int) s.x, (int) s.z).isChunkLoaded) break;
                            speakers.remove(s);
                            break;
                        }
                    }
                }
            }


            if ((scheduleRedstoneInput) && (listenToRedstone)) {
                if ((!scheduledRedstoneInput) && (redstoneInput)) {
                    isPlaying = (!isPlaying);
                    PacketHandler.INSTANCE.sendToAll(new MessageTERadioBlock(xCoord, yCoord, zCoord,
                            getWorldObj(), streamURL, isPlaying, volume, 1));
                }

                redstoneInput = scheduledRedstoneInput;
                scheduleRedstoneInput = false;
                scheduledRedstoneInput = false;
            }
        }
    }

    public void setVolume(float vol) {
        volume = vol;
    }

    public void setScreenColor(Integer color) {
        screenColor = color;
    }

    public boolean isListeningToRedstoneInput() {
        return listenToRedstone;
    }

    public void setListenToRedstoneInput(boolean input) {
        listenToRedstone = input;
    }

    public void setRedstoneInput(boolean input) {
        if (input) {
            scheduledRedstoneInput = true;
        }
        scheduleRedstoneInput = true;
    }

    public int addSpeaker(World w, double x, double y, double z) {
        if (speakers.size() >= 10)
            return 1;
        for (Speaker s : speakers)
            if ((s.x == x) && (s.y == y) && (s.z == z))
                return 2;
        speakers.add(new Speaker(x, y, z, w));
        return 0;
    }

    public int canAddSpeaker(World w, double x, double y, double z) {
        if (speakers.size() >= 10)
            return 1;
        for (Speaker s : speakers)
            if ((s.x == x) && (s.y == y) && (s.z == z))
                return 2;
        return 0;
    }

    public float getVolume() {
        return volume;
    }

    private float getClosest() {
        float closest = (float) getDistanceFrom(Minecraft.getMinecraft().thePlayer.posX,
                Minecraft.getMinecraft().thePlayer.posY, Minecraft.getMinecraft().thePlayer.posZ);
        if (!speakers.isEmpty()) {
            for (Speaker s : speakers) {
                float distance = (float) Math.pow(Minecraft.getMinecraft().thePlayer.getDistance(s.x, s.y, s.z), 2.0D);
                if (closest > distance) {
                    closest = distance;
                }
            }
        }
        return closest;
    }

    public int getScreenColor() {
        return screenColor;
    }

    @Override
    public String getComponentName() {
        return "openfm_radio";
    }

    @Override
    public Packet getDescriptionPacket() {
        for (Speaker s : speakers) {
            PacketHandler.INSTANCE.sendToDimension(new MessageTERadioBlock(xCoord, yCoord, zCoord, worldObj, "", false, 1.0F, 15, s.x, s.y, s.z),
                    getWorldObj().provider.dimensionId);
        }
        int mode = 13;
        if (listenToRedstone)
            mode = 14;

        PacketHandler.INSTANCE.sendToAllAround(new MessageTERadioBlock(this), new NetworkRegistry.TargetPoint(getWorldObj().provider.dimensionId, xCoord, yCoord, zCoord, 20.0D));
        //PacketHandler.INSTANCE.sendToDimension(new MessageTERadioBlock(this), getWorldObj().provider.dimensionId);


        NBTTagCompound tagCom = new NBTTagCompound();
        writeToNBT(tagCom);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, blockMetadata, tagCom);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
        readFromNBT(packet.func_148857_g());    // == "getNBTData"
    }

    // OpenComputers lua methods.

    @Optional.Method(modid = "OpenComputers")
    @Callback
    public Object[] setURL(Context context, Arguments args) {
        streamURL = args.checkString(0);
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        getDescriptionPacket();
        return new Object[]{ true };
    }

    @Optional.Method(modid = "OpenComputers")
    @Callback
    public Object[] getVol(Context context, Arguments args) {
        return new Object[]{ volume };
    }

    @Optional.Method(modid = "OpenComputers")
    @Callback
    public Object[] setVol(Context context, Arguments args) {
        float v = (float) (args.checkInteger(0));
        if ((v > 0.0F) && (v <= 1.0F)) {
            volume = args.checkInteger(0);
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            getDescriptionPacket();
            return new Object[]{ volume};
        } else {
            return new Object[]{ false };
        }
    }

    @Optional.Method(modid = "OpenComputers")
    @Callback
    public Object[] volUp(Context context, Arguments args) {
        float v = (float) (volume + 0.1D);
        if ((v > 0.0F) && (v <= 1.0F)) {
            volume = v;
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            getDescriptionPacket();
            return new Object[]{ volume};
        } else {
            return new Object[]{ false };
        }
    }

    @Optional.Method(modid = "OpenComputers")
    @Callback
    public Object[] volDown(Context context, Arguments args) {
        float v = (float) (volume - 0.1D);
        if ((v > 0.0F) && (v <= 1.0F)) {
            volume = v;
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            getDescriptionPacket();
            return new Object[]{ volume };
        } else {
            return new Object[]{ false };
        }
    }

    @Optional.Method(modid = "OpenComputers")
    @Callback
    public Object[] stop(Context context, Arguments args) {
        stopStream();
        isPlaying = false;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        getDescriptionPacket();
        return new Object[]{ true };
    }

    @Optional.Method(modid = "OpenComputers")
    @Callback
    public Object[] isPlaying(Context context, Arguments args) {
        return new Object[]{ isPlaying() };
    }

    @Optional.Method(modid = "OpenComputers")
    @Callback
    public Object[] start(Context context, Arguments args) {
        startStream();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        getDescriptionPacket();
        return new Object[]{ true };
    }

    @Optional.Method(modid = "OpenComputers")
    @Callback
    public Object[] getListenRedstone(Context context, Arguments args) {
        return new Object[]{ isListeningToRedstoneInput() };
    }

    @Optional.Method(modid = "OpenComputers")
    @Callback
    public Object[] setListenRedstone(Context context, Arguments args) {
        setRedstoneInput(args.checkBoolean(0));
        return new Object[]{ isListeningToRedstoneInput() };
    }

    @Optional.Method(modid = "OpenComputers")
    @Callback
    public Object[] getScreenColor(Context context, Arguments args) {
        return new Object[]{ getScreenColor() };
    }

    @Optional.Method(modid = "OpenComputers")
    @Callback
    public Object[] setScreenColor(Context context, Arguments args) {
        setScreenColor(args.checkInteger(0));
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        getDescriptionPacket();
        markDirty(); // Marks the chunk as dirty, so that it is saved properly on changes. Not required for the sync specifically, but usually goes alongside the former.
        return new Object[]{ true };
    }

    @Optional.Method(modid = "OpenComputers")
    @Callback
    public Object[] getAttachedSpeakerCount(Context context, Arguments args) {
        return new Object[]{ speakers.size() };
    }

    // NBT serialization / deserialization

    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        streamURL = nbt.getString("streamurl");
        volume = nbt.getFloat("volume");
        listenToRedstone = nbt.getBoolean("input");
        redstoneInput = nbt.getBoolean("lastInput");
        isPlaying = nbt.getBoolean("lastState");
        int speakersCount = nbt.getInteger("speakersCount");
        screenColor = nbt.getInteger("screenColor");
        for (int i = 0; i < speakersCount; i++) {
            double x = nbt.getDouble("speakerX" + i);
            double y = nbt.getDouble("speakerY" + i);
            double z = nbt.getDouble("speakerZ" + i);
            addSpeaker(getWorldObj(), x, y, z);
        }
    }

    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setString("streamurl", streamURL);
        nbt.setFloat("volume", volume);
        nbt.setBoolean("input", listenToRedstone);
        nbt.setBoolean("lastInput", redstoneInput);
        nbt.setBoolean("lastState", isPlaying);
        nbt.setInteger("speakersCount", speakers.size());
        nbt.setInteger("screenColor", screenColor);
        for (int i = 0; i < speakers.size(); i++) {
            nbt.setDouble("speakerX" + i, speakers.get(i).x);
            nbt.setDouble("speakerY" + i, speakers.get(i).y);
            nbt.setDouble("speakerZ" + i, speakers.get(i).z);
        }
    }

}


