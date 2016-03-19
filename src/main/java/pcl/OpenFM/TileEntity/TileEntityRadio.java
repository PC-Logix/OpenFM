package pcl.OpenFM.TileEntity;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedPeripheral;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import pcl.OpenFM.ContentRegistry;
import pcl.OpenFM.OFMConfiguration;
import pcl.OpenFM.OpenFM;
import pcl.OpenFM.Block.BlockSpeaker;
import pcl.OpenFM.Items.ItemMemoryCard;
import pcl.OpenFM.misc.Speaker;
import pcl.OpenFM.network.PacketHandler;
import pcl.OpenFM.network.Message.MessageTERadioBlock;
import pcl.OpenFM.player.PlayerDispatcher;
import pcl.OpenFM.player.OGGPlayer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.network.NetworkRegistry;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

@Optional.InterfaceList({
	@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers"),
	@Optional.Interface(iface = "li.cil.oc.api.network.ManagedPeripheral", modid = "OpenComputers"),
	@Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft")
})

public class TileEntityRadio extends TileEntity implements IPeripheral, SimpleComponent, ManagedPeripheral, IInventory {
	public PlayerDispatcher mp3Player = null;
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
	public ItemStack[] RadioItemStack = new ItemStack[1];
	public TileEntityRadio(World w) {
		world = w;
		if (isPlaying)
			try {
				startStream();
			} catch (Exception e) {
				stopStream();
			}
	}

	public TileEntityRadio() {
		if (this.isPlaying) {
			try {
				startStream();
			} catch (Exception e) {
				stopStream();
			}
		}
	}

	public void setWorld(World w)
	{
		world = w;
	}

	public void startStream() throws Exception {
		OFMConfiguration.init(OpenFM.configFile);
		if (OFMConfiguration.enableStreams) {
			Side side = FMLCommonHandler.instance().getEffectiveSide();
			String decoder = null;
			if (!OpenFM.playerList.contains(mp3Player)) {
				if (side == Side.CLIENT) {
					OkHttpClient client = new OkHttpClient();
					Request request = new Request.Builder().url(streamURL).build();
					Response response = null;
					AudioFileFormat baseFileFormat = null;
					try {
						response = client.newCall(request).execute();
					} catch (IOException e1) {
						isValid = false;
						streamURL = null;
						stopStream();
					}
					try {
						BufferedInputStream bis = new BufferedInputStream(response.body().byteStream());
						baseFileFormat = AudioSystem.getAudioFileFormat(bis);
					} catch (IOException | UnsupportedAudioFileException e1) {
						isValid = false;
						streamURL = null;
						stopStream();
						OpenFM.logger.info(e1);
					}
					if (isValid) {
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
							mp3Player = new PlayerDispatcher(decoder, streamURL, world, xCoord, yCoord, zCoord);
							OpenFM.playerList.add(mp3Player);	
						}
					}
				} else {
					if (isValid) {
						this.isPlaying = true;
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
		int loops = 0;
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
				if (loops >= 40) {
					PacketHandler.INSTANCE.sendToAllAround(new MessageTERadioBlock(this), new NetworkRegistry.TargetPoint(getWorldObj().provider.dimensionId, this.xCoord, this.yCoord, this.zCoord, 50.0D));
					loops++;
				} else {
					loops = 0;
				}
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
					PacketHandler.INSTANCE.sendToAll(new MessageTERadioBlock(xCoord, yCoord, zCoord, this.world, streamURL, isPlaying, volume, 1));
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

	public void addStation(String station) {
		if (station != null && !stations.contains(station)) {
			stations.add(station);
			PacketHandler.INSTANCE.sendToDimension(new MessageTERadioBlock(this.xCoord, this.yCoord, this.zCoord, this.worldObj, station, 42), getWorldObj().provider.dimensionId);
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			getDescriptionPacket();
			markDirty();
		}
	}

	public void delStation(String station) {
		if (station != null && stations.contains(station)) {
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

	public String getScreenText() {
		return this.screenText;
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
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 6, tagCom);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
		readFromNBT(packet.func_148857_g());    // == "getNBTData"
	}


	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.streamURL = nbt.getString("streamurl");
		this.volume = nbt.getFloat("volume");
		this.listenToRedstone = nbt.getBoolean("input");
		this.redstoneInput = nbt.getBoolean("lastInput");
		this.isPlaying = nbt.getBoolean("lastState");
		int speakersCount = nbt.getInteger("speakersCount");
		this.setStationCount(nbt.getInteger("stationCount"));
		this.screenColor = nbt.getInteger("screenColor");
		this.isLocked = nbt.getBoolean("isLocked");
		this.owner = nbt.getString("owner");
		if (nbt.getString("screenText").length() < 1) {
			this.screenText = "OpenFM";
		} else {
			this.screenText = nbt.getString("screenText");
		}
		for (int i = 0; i < speakersCount; i++) {
			double x = nbt.getDouble("speakerX" + i);
			double y = nbt.getDouble("speakerY" + i);
			double z = nbt.getDouble("speakerZ" + i);
			addSpeaker(getWorldObj(), x, y, z);
		}
		for(int i = 0; i < this.getStationCount(); i++)
		{
			stations.add(nbt.getString("station" + i));
		}
		NBTTagList var2 = nbt.getTagList("Items",nbt.getId());
		this.RadioItemStack = new ItemStack[this.getSizeInventory()];
		for (int var3 = 0; var3 < var2.tagCount(); ++var3)
		{
			NBTTagCompound var4 = (NBTTagCompound)var2.getCompoundTagAt(var3);
			byte var5 = var4.getByte("Slot");
			if (var5 >= 0 && var5 < this.RadioItemStack.length)
			{
				this.RadioItemStack[var5] = ItemStack.loadItemStackFromNBT(var4);
			}
		}
	}


	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		if (this.streamURL != null)
			nbt.setString("streamurl", this.streamURL);
		nbt.setFloat("volume", this.volume);
		nbt.setBoolean("input", this.listenToRedstone);
		nbt.setBoolean("lastInput", this.redstoneInput);
		nbt.setBoolean("lastState", this.isPlaying);
		nbt.setInteger("speakersCount", this.speakers.size());
		nbt.setInteger("screenColor", this.screenColor);
		if (this.screenText != null)
			nbt.setString("screenText", this.screenText);
		nbt.setBoolean("isLocked", this.isLocked);
		if (this.owner != null)
			nbt.setString("owner", this.owner);
		for (int i = 0; i < this.speakers.size(); i++) {
			nbt.setDouble("speakerX" + i, ((Speaker)this.speakers.get(i)).x);
			nbt.setDouble("speakerY" + i, ((Speaker)this.speakers.get(i)).y);
			nbt.setDouble("speakerZ" + i, ((Speaker)this.speakers.get(i)).z);
		}
		for(int i = 0; i < stations.size(); i++)
		{
			String s = stations.get(i);
			if(s != null)
			{
				if (s != null) {
					nbt.setString("station" + i, s);
					nbt.setInteger("stationCount", i + 1);
				}
			}
		}
		NBTTagList var2 = new NBTTagList();
		for (int var3 = 0; var3 < this.RadioItemStack.length; ++var3)
		{
			if (this.RadioItemStack[var3] != null)
			{
				NBTTagCompound var4 = new NBTTagCompound();
				var4.setByte("Slot", (byte)var3);
				this.RadioItemStack[var3].writeToNBT(var4);
				var2.appendTag(var4);
			}
		}
		nbt.setTag("Items", var2);
	}


	public enum ComputerMethod {
		getAttachedSpeakerCount, //No args
		setScreenColor, //Integer (0x######)
		getScreenColor, //No args
		setListenRedstone, //Boolean
		getListenRedstone, //No args
		isPlaying, //No args
		stop, //No args
		play,
		start, //No args
		greet, //No args
		setURL, //String
		getVol, //No args
		setVol, //Double
		volUp, //No args
		volDown, //No args
		setScreenText, //String
		getAttachedSpeakers //No args
	}

	public static final int numMethods = ComputerMethod.values().length;

	public static final String[] methodNames = new String[numMethods];
	static {
		ComputerMethod[] methods = ComputerMethod.values();
		for(ComputerMethod method : methods) {
			methodNames[method.ordinal()] = method.toString();
		}
	}

	public static final Map<String, Integer> methodIds = new HashMap<String, Integer>();
	static {
		for (int i = 0; i < numMethods; ++i) {
			methodIds.put(methodNames[i], i);
		}
	}

	public Object[] callMethod(int method, Object[] args) {
		if(method < 0 || method >= numMethods) {
			throw new IllegalArgumentException("Invalid method number");
		}
		ComputerMethod computerMethod = ComputerMethod.values()[method];

		switch(computerMethod) {
		case getAttachedSpeakerCount:
			return new Object[] { speakers.size() };

		case setScreenColor:
			if(args.length != 1) {
				return new Object[]{false, "Insufficient number of arguments, expected 1"};
			}
			setScreenColor((int)Math.round((Double)args[0]));
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			getDescriptionPacket();
			markDirty();
			return new Object[]{ true };

		case getScreenColor:
			return new Object[]{ getScreenColor() };

		case setListenRedstone:
			if(args.length != 1) {
				return new Object[]{false, "Insufficient number of arguments, expected 1"};
			}
			setRedstoneInput((boolean) args[0]);
			return new Object[]{ isListeningToRedstoneInput() };

		case getListenRedstone:
			return new Object[]{ isListeningToRedstoneInput() };

		case isPlaying:
			return new Object[]{ isPlaying() };

		case stop:
			stopStream();
			isPlaying = false;
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			getDescriptionPacket();
			return new Object[]{ true };
		case play:
		case start:
			try {
				startStream();
			} catch (Exception e) {
				e.printStackTrace();
			}
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			getDescriptionPacket();
			return new Object[]{ true };

		case greet:
			return new Object[] { "Lasciate ogne speranza, voi ch'intrate" };

		case getAttachedSpeakers:
			return new Object[] { this.speakers.size() };

		case setScreenText:
			if(args.length != 1) {
				return new Object[]{false, "Insufficient number of arguments, expected 1"};
			}
			setScreenText((String) args[0]);
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			getDescriptionPacket();
			markDirty(); // Marks the chunk as dirty, so that it is saved properly on changes. Not required for the sync specifically, but usually goes alongside the former.
			return new Object[] { true } ;

		case volDown:
			float v = (float)(this.volume - 0.1D);
			if ((v > 0.0F) && (v <= 1.0F)) {
				setVolume(v);
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				getDescriptionPacket();
				return new Object[] { getVolume() };
			} else {
				return new Object[] { false };
			}

		case volUp:
			float v1 = (float)(this.volume + 0.1D);
			if ((v1 > 0.0F) && (v1 <= 1.0F)) {
				setVolume(v1);
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				getDescriptionPacket();
				return new Object[] { getVolume() };
			} else {
				return new Object[] { false };
			}

		case setVol:
			if(args.length != 1) {
				return new Object[]{false, "Insufficient number of arguments, expected 1"};
			}
			float    v2;
			Double  x = new Double((double) args[0]);
			v2    = x.floatValue() / 10 + 0.0001F;

			if ((v2 > 0.0F) && (v2 <= 1.0F)) {
				setVolume(v2);
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				getDescriptionPacket();
				return new Object[] { getVolume() };
			} else {
				return new Object[] { false };
			}

		case getVol:
			return new Object[] { getVolume() };

		case setURL:
			if(args.length != 1) {
				return new Object[]{false, "Insufficient number of arguments, expected 1"};
			}
			if (args[0] != null) {
				String tempURL = new String((byte[]) args[0], StandardCharsets.UTF_8);
				if (tempURL != null && tempURL.length() > 1) {
					streamURL = tempURL;
				} else {
					return new Object[] { false, "Error parsing URL in packet" };
				}
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				getDescriptionPacket();
				return new Object[] { true };
			}
			return new Object[] { false, "Error parsing URL in packet" };


		default: return new Object[]{false, "Not implemented."};
		}
	}


	@Override
	@Optional.Method(modid = "OpenComputers")
	public Object[] invoke(final String method, final Context context, final Arguments args) throws Exception {
		final Object[] arguments = new Object[args.count()];
		for (int i = 0; i < args.count(); ++i) {
			arguments[i] = args.checkAny(i);
		}
		final Integer methodId = methodIds.get(method);
		if (methodId == null) {
			throw new NoSuchMethodError();
		}
		return callMethod(methodId, arguments);
	}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public String[] methods() {
		return methodNames;
	}

	@Override
	@Optional.Method(modid = "ComputerCraft")
	public String getType() {
		return "openfm_radio";
	}

	@Override
	@Optional.Method(modid = "ComputerCraft")
	public String[] getMethodNames() {
		return methodNames;
	}

	@Override
	@Optional.Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException {
		try {
			return callMethod(method, arguments);
		} catch(Exception e) {
			// Rethrow errors as LuaExceptions for CC
			throw new LuaException(e.getMessage());
		}
	}

	@Override
	@Optional.Method(modid = "ComputerCraft")
	public void attach(IComputerAccess computer) {
	}

	@Override
	@Optional.Method(modid = "ComputerCraft")
	public void detach(IComputerAccess computer) {
	}

	@Override
	@Optional.Method(modid = "ComputerCraft")
	public boolean equals(IPeripheral other) {
		return hashCode() == other.hashCode();
	}

	@Override
	public int getSizeInventory() {
		return RadioItemStack.length;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return this.RadioItemStack[i];
	}

	@Override
	public ItemStack decrStackSize(int slot, int amt) {
		ItemStack stack = getStackInSlot(slot);
		if (stack != null) {
			if (stack.stackSize <= amt) {
				setInventorySlotContents(slot, null);
			} else {
				stack = stack.splitStack(amt);
				if (stack.stackSize == 0) {
					setInventorySlotContents(slot, null);
				}
			}
		}
		return stack;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		if (getStackInSlot(i) != null) {
			ItemStack var2 = getStackInSlot(i);
			setInventorySlotContents(i, null);
			return var2;
		} else {
			return null;
		}
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		this.RadioItemStack[i] = itemstack;
		if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit()) {
			itemstack.stackSize = this.getInventoryStackLimit();
		}
	}

	@Override
	public String getInventoryName() {
		return "ofm_radio";
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
	}

	@Override
	public void openInventory() {		
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		if (i == 0) {
			if (itemstack.getItem() instanceof ItemMemoryCard) {
				return true;
			}
		}
		return false;
	}

	public void writeDataToCard() {
		if (getStackInSlot(0) != null) {
			RadioItemStack[0] = new ItemStack(ContentRegistry.itemMemoryCard);
			RadioItemStack[0].setTagCompound(new NBTTagCompound());
			RadioItemStack[0].stackTagCompound.setString("screenText", this.screenText);	
			RadioItemStack[0].stackTagCompound.setInteger("screenColor", this.screenColor);
			RadioItemStack[0].stackTagCompound.setString("streamURL", this.streamURL);
			RadioItemStack[0].stackTagCompound.setInteger("stationCount", this.stationCount);
			for(int i = 0; i < this.getStationCount(); i++)
			{
				RadioItemStack[0].stackTagCompound.setString("station" + i, stations.get(i));
			}
			RadioItemStack[0].setStackDisplayName(this.screenText);
		}

	}

	public void readDataFromCard() {
		if (getStackInSlot(0) != null) {
			if (RadioItemStack[0].hasTagCompound()) {
				this.screenText = RadioItemStack[0].getTagCompound().getString("screenText");
				this.screenColor = RadioItemStack[0].getTagCompound().getInteger("screenColor");
				this.streamURL = RadioItemStack[0].getTagCompound().getString("streamURL");
				this.stationCount = RadioItemStack[0].getTagCompound().getInteger("stationCount");
				for(int i = 0; i < this.getStationCount(); i++)
				{
					stations.add(RadioItemStack[0].stackTagCompound.getString("station" + i));
				}
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				getDescriptionPacket();
				markDirty();
			}
		}
	}
}