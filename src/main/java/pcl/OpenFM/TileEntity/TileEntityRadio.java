package pcl.OpenFM.TileEntity;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

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
import pcl.OpenFM.OFMConfiguration;
import pcl.OpenFM.OpenFM;
import pcl.OpenFM.Block.BlockSpeaker;
import pcl.OpenFM.misc.Speaker;
import pcl.OpenFM.network.PacketHandler;
import pcl.OpenFM.network.Message.MessageTERadioBlock;
import pcl.OpenFM.player.MP3Player;
import pcl.OpenFM.player.OGGPlayer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.network.NetworkRegistry;

@Optional.InterfaceList(value={
		@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")
})
public class TileEntityRadio extends TileEntity implements SimpleComponent {
	public MP3Player mp3Player = null;
	public OGGPlayer oggPlayer = null;
	public boolean useMP3 = true;
	public boolean isPlaying = false;
	public boolean isValid = true;
	public String streamURL = "";
	private World world;
	public float volume = 0.3F;
	private boolean redstoneInput = false;
	public boolean listenToRedstone = false;
	private boolean scheduledRedstoneInput = false;
	private boolean scheduleRedstoneInput = false;
	public ArrayList<Speaker> speakers = new ArrayList<Speaker>();
	private int screenColor = 0x0000FF;
	private String screenText = "OpenFM";
	public List<String> stations = new ArrayList<String>();
	private int stationCount = 0;
	public boolean isLocked;
	public String owner = "";

	public TileEntityRadio(World w) {
		world = w;
		if (isPlaying)
			startStream();
	}

	public TileEntityRadio() {
		if (this.isPlaying) {
			startStream();
		}
	}

	public void setWorld(World w)
	{
		world = w;
	}

	public void startStream() {
		OFMConfiguration.init(OpenFM.configFile);
		if (OFMConfiguration.enableStreams) {
			Side side = FMLCommonHandler.instance().getEffectiveSide();
			String decoder = null;
			if (!OpenFM.playerList.contains(mp3Player)) {
				if (side == Side.CLIENT) {
					URL file = null;
					try {
						file = new URL(streamURL);
						AudioFileFormat baseFileFormat = null;
						AudioFormat baseFormat = null;
						try {
							baseFileFormat = AudioSystem.getAudioFileFormat(file);
						} catch (UnsupportedAudioFileException e) {
							// TODO Auto-generated catch block
							isValid = false;
						} catch (IOException e) {
							isValid = false;
						}
						baseFormat = baseFileFormat.getFormat();
						// Audio type such as MPEG1 Layer3, or Layer 2, or ...
						AudioFileFormat.Type type = baseFileFormat.getType();
						OpenFM.logger.info(type.toString());
						if (type.toString().equals("MP3")) {
							decoder = "mp3";
						} else if (type.toString().equals("OGG")) {
							decoder = "ogg";
						}
						if (decoder != null && isValid) {
							isPlaying = true;
							OpenFM.logger.info("Starting Stream: " + streamURL + " at X:" + xCoord + " Y:" + yCoord + " Z:" + zCoord);
							mp3Player = new MP3Player(decoder, streamURL, world, xCoord, yCoord, zCoord);
							OpenFM.playerList.add(mp3Player);	
						}
					} catch (MalformedURLException e) {

					}
				}
			}
		} else {
			stopStream();
		}
	}

	public void stopStream() {
		Side side = FMLCommonHandler.instance().getEffectiveSide();
		if (OpenFM.playerList.contains(mp3Player)) {
			if (side == Side.CLIENT) {
				mp3Player.stop();
			}
			OpenFM.playerList.remove(mp3Player);
			isPlaying = false;
		}
		isPlaying = false;
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
				for (Speaker s : speakers) {
					Block sb = getWorldObj().getBlock((int) s.x, (int) s.y, (int) s.z);
					if (!(sb instanceof BlockSpeaker)) {
						if (!getWorldObj().getChunkFromBlockCoords((int) s.x, (int) s.z).isChunkLoaded) break;
						speakers.remove(s);
						break;
					}
				}
			}
			if ((Minecraft.getMinecraft().thePlayer != null) && (mp3Player != null || oggPlayer != null) && (!isInvalid())) {
				vol = getClosest();
				if (vol > 10000.0F * volume) {
					if (mp3Player != null)
						mp3Player.setVolume(0.0F);
					else if (oggPlayer != null)
						oggPlayer.setVolume(0.0f);
				} else {
					float v2 = 10000.0F / vol / 100.0F;
					if (v2 > 1.0F) {
						if (mp3Player != null)
							mp3Player.setVolume(1.0F * volume * volume);
						else if (oggPlayer != null)
							oggPlayer.setVolume(1.0F * volume * volume);
					} else {
						if (mp3Player != null)
							mp3Player.setVolume(v2 * volume * volume);
						else if (oggPlayer != null)
							oggPlayer.setVolume(v2 * volume * volume);
					}
				}
				if (vol == 0.0F) {
					invalidate();
				}
			}
		} else {
			if (isPlaying()) {
				PacketHandler.INSTANCE.sendToAllAround(new MessageTERadioBlock(this), new NetworkRegistry.TargetPoint(getWorldObj().provider.dimensionId, this.xCoord, this.yCoord, this.zCoord, 50.0D));
				th += 1;
				if (th >= 60) {
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

	public void setStreamURL(String url) {
		streamURL = url;
	}

	public String getStreamURL() {
		return streamURL;
	}

	@Optional.Method(modid = "OpenComputers")
	@Callback
	public Object[] setURL(Context context, Arguments args) {
		streamURL = args.checkString(0);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		getDescriptionPacket();
		return new Object[] { true };
	}

	@Optional.Method(modid = "OpenComputers")
	@Callback
	public Object[] getVol(Context context, Arguments args) {
		return new Object[] { getVolume() };
	}

	@Optional.Method(modid = "OpenComputers")
	@Callback
	public Object[] setVol(Context context, Arguments args) {
		float v = (float)(args.checkInteger(0));
		if ((v > 0.0F) && (v <= 1.0F)) {
			setVolume(args.checkInteger(0));
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			getDescriptionPacket();
			return new Object[] { getVolume() };
		} else {
			return new Object[] { false };
		}
	}

	@Optional.Method(modid = "OpenComputers")
	@Callback
	public Object[] volUp(Context context, Arguments args) {
		float v = (float)(this.volume + 0.1D);
		if ((v > 0.0F) && (v <= 1.0F)) {
			setVolume(v);
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			getDescriptionPacket();
			return new Object[] { getVolume() };
		} else {
			return new Object[] { false };
		}
	}

	@Optional.Method(modid = "OpenComputers")
	@Callback
	public Object[] volDown(Context context, Arguments args) {
		float v = (float)(this.volume - 0.1D);
		if ((v > 0.0F) && (v <= 1.0F)) {
			setVolume(v);
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			getDescriptionPacket();
			return new Object[] { getVolume() };
		} else {
			return new Object[] { false };
		}
	}

	public void addStation(String station) {
		if (!stations.contains(station)) {
			stations.add(station);
			PacketHandler.INSTANCE.sendToDimension(new MessageTERadioBlock(this.xCoord, this.yCoord, this.zCoord, this.worldObj, station, 42), getWorldObj().provider.dimensionId);
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			getDescriptionPacket();
			markDirty();
		}
	}

	public void delStation(String station) {
		if (stations.contains(station)) {
			stations.remove(station);
			PacketHandler.INSTANCE.sendToDimension(new MessageTERadioBlock(this.xCoord, this.yCoord, this.zCoord, this.worldObj, station, 43), getWorldObj().provider.dimensionId);
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			getDescriptionPacket();
			markDirty();
		}
	}

	public String getNext(String uid) {
		int idx = stations.indexOf(uid);
		if (idx < 0 || idx+1 == stations.size()) return uid;
		return stations.get(idx + 1);
	}

	public String getPrevious(String uid) {
		int idx = stations.indexOf(uid);
		if (idx <= 0 || idx-1 == stations.size()) return uid;
		return stations.get(idx - 1);
	}

	public void setVolume(float vol) {
		volume = vol;
	}

	public boolean isListeningToRedstoneInput() {
		return listenToRedstone;
	}

	public void setScreenColor(Integer color) {
		this.screenColor = color;
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		getDescriptionPacket();
		markDirty();
	}

	public void setRedstoneInput(boolean input) {
		if (input) {
			this.scheduledRedstoneInput = input;
		}
		this.scheduleRedstoneInput = true;
	}

	public void setScreenText(String text) {
		this.screenText = text;
	}

	@Optional.Method(modid = "OpenComputers")
	@Callback
	public Object[] setScreenText(Context context, Arguments args) {
		setScreenText(args.checkString(0));
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		getDescriptionPacket();
		markDirty(); // Marks the chunk as dirty, so that it is saved properly on changes. Not required for the sync specifically, but usually goes alongside the former.
		return new Object[] { true } ;
	}

	public String getScreenText() {
		return this.screenText;
	}

	@Optional.Method(modid = "OpenComputers")
	@Callback
	public Object[] getAttachedSpeakers(Context context, Arguments args) {
		return new Object[] { this.speakers.size() };
	}

	public int addSpeaker(World w, double x, double y, double z) {
		int ret = canAddSpeaker(w, x, y, z);
		if (ret == 0) {
			speakers.add(new Speaker(x, y, z, w));
		}
		return ret;
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
		float closest = (float) getDistanceFrom(Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posY, Minecraft.getMinecraft().thePlayer.posZ);
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
		// TODO Auto-generated method stub
		return "openfm_radio";
	}

	public int getStationCount() {
		return this.stationCount;
	}

	public void setStationCount(int stationCount) {
		this.stationCount = stationCount;
	}

	@Override
	public Packet getDescriptionPacket()
	{
		for (Speaker s :speakers) {
			PacketHandler.INSTANCE.sendToDimension(new MessageTERadioBlock(this.xCoord, this.yCoord, this.zCoord, this.worldObj, "", false, 1.0F, 15, s.x, s.y, s.z), getWorldObj().provider.dimensionId);
		}

		PacketHandler.INSTANCE.sendToAllAround(new MessageTERadioBlock(this), new NetworkRegistry.TargetPoint(getWorldObj().provider.dimensionId, this.xCoord, this.yCoord, this.zCoord, 30.0D));
		//PacketHandler.INSTANCE.sendToDimension(new MessageTERadioBlock(this), getWorldObj().provider.dimensionId);


		NBTTagCompound tagCom = new NBTTagCompound();
		this.writeToNBT(tagCom);
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, this.blockMetadata, tagCom);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
		readFromNBT(packet.func_148857_g());    // == "getNBTData"
	}

	@Optional.Method(modid = "OpenComputers")
	@Callback
	public Object[] greet(Context context, Arguments args) {
		return new Object[] { "Lasciate ogne speranza, voi ch'intrate" };
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


	public void readFromNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.readFromNBT(par1NBTTagCompound);
		this.streamURL = par1NBTTagCompound.getString("streamurl");
		this.volume = par1NBTTagCompound.getFloat("volume");
		this.listenToRedstone = par1NBTTagCompound.getBoolean("input");
		this.redstoneInput = par1NBTTagCompound.getBoolean("lastInput");
		this.isPlaying = par1NBTTagCompound.getBoolean("lastState");
		int speakersCount = par1NBTTagCompound.getInteger("speakersCount");
		this.setStationCount(par1NBTTagCompound.getInteger("stationCount"));
		this.screenColor = par1NBTTagCompound.getInteger("screenColor");
		this.isLocked = par1NBTTagCompound.getBoolean("isLocked");
		this.owner = par1NBTTagCompound.getString("owner");
		if (par1NBTTagCompound.getString("screenText").length() < 1) {
			this.screenText = "OpenFM";
		} else {
			this.screenText = par1NBTTagCompound.getString("screenText");
		}
		for (int i = 0; i < speakersCount; i++) {
			double x = par1NBTTagCompound.getDouble("speakerX" + i);
			double y = par1NBTTagCompound.getDouble("speakerY" + i);
			double z = par1NBTTagCompound.getDouble("speakerZ" + i);
			addSpeaker(getWorldObj(), x, y, z);
		}
		for(int i = 0; i < this.getStationCount(); i++)
		{
			stations.add(par1NBTTagCompound.getString("station" + i));
		}
	}


	public void writeToNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.writeToNBT(par1NBTTagCompound);
		par1NBTTagCompound.setString("streamurl", this.streamURL);
		par1NBTTagCompound.setFloat("volume", this.volume);
		par1NBTTagCompound.setBoolean("input", this.listenToRedstone);
		par1NBTTagCompound.setBoolean("lastInput", this.redstoneInput);
		par1NBTTagCompound.setBoolean("lastState", this.isPlaying);
		par1NBTTagCompound.setInteger("speakersCount", this.speakers.size());
		par1NBTTagCompound.setInteger("screenColor", this.screenColor);
		par1NBTTagCompound.setString("screenText", this.screenText);
		par1NBTTagCompound.setBoolean("isLocked", this.isLocked);
		par1NBTTagCompound.setString("owner", this.owner);
		for (int i = 0; i < this.speakers.size(); i++) {
			par1NBTTagCompound.setDouble("speakerX" + i, ((Speaker)this.speakers.get(i)).x);
			par1NBTTagCompound.setDouble("speakerY" + i, ((Speaker)this.speakers.get(i)).y);
			par1NBTTagCompound.setDouble("speakerZ" + i, ((Speaker)this.speakers.get(i)).z);
		}
		for(int i = 0; i < stations.size(); i++)
		{
			String s = stations.get(i);
			if(s != null)
			{
				par1NBTTagCompound.setString("station" + i, s);
				par1NBTTagCompound.setInteger("stationCount", i + 1);
			}
		}
	}
}