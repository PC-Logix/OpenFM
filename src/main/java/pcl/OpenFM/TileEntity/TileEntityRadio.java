package pcl.OpenFM.TileEntity;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedPeripheral;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pcl.OpenFM.ContentRegistry;
import pcl.OpenFM.OFMConfiguration;
import pcl.OpenFM.OpenFM;
import pcl.OpenFM.Block.BlockSpeaker;
import pcl.OpenFM.Items.ItemMemoryCard;
import pcl.OpenFM.misc.Speaker;
import pcl.OpenFM.network.PacketHandler;
import pcl.OpenFM.network.message.MessageRadioAddSpeaker;
import pcl.OpenFM.network.message.MessageRadioAddStation;
import pcl.OpenFM.network.message.MessageRadioDelStation;
import pcl.OpenFM.network.message.MessageRadioPlaying;
import pcl.OpenFM.network.message.MessageRadioSync;
import pcl.OpenFM.player.OGGPlayer;
import pcl.OpenFM.player.PlayerDispatcher;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

@Optional.InterfaceList({
	@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers"),
	@Optional.Interface(iface = "li.cil.oc.api.network.ManagedPeripheral", modid = "OpenComputers"),
	@Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft")
})

public class TileEntityRadio extends TileEntity implements SimpleComponent, ManagedPeripheral, IInventory, ITickable {
	public PlayerDispatcher player = null;
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
	public int screenColor = 0x0000FF;
	public String screenText = "OpenFM";
	public String screenOut = "";
	public List<String> stations = new ArrayList<String>();
	private int stationCount = 0;
	public boolean isLocked;
	public String owner = "";
	public ItemStack[] RadioItemStack = new ItemStack[1];

	int th = 0;
	int loops = 0;
	int ticks = 0;
	int renderCount = 0;

	public TileEntityRadio(World w) {
		world = w;
		if (isPlaying) {
			try {
				startStream();
			} catch (Exception e) {
				stopStream();
			}
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
			if (!OpenFM.playerList.contains(player)) {
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
					}
					if (isValid) {
						// Audio type such as MPEG1 Layer3, or Layer 2, or ...
						AudioFileFormat.Type type = baseFileFormat.getType();
						OpenFM.logger.info(baseFileFormat.getFormat());
						OpenFM.logger.info(type.toString());
						if (type.toString().equals("MP3")) {
							decoder = "mp3";
						} else if (type.toString().equals("AAC")) {
							//decoder = "aac";
							isValid = false;
							stopStream();
							OpenFM.logger.error("Stopping AAC Stream before catastrophic failure");
						} else if (type.toString().equals("OGG")) {
							decoder = "ogg";
						}
						if (decoder != null && isValid) {
							isPlaying = true;
							OpenFM.logger.info("Starting Stream: " + streamURL + " at X:" + pos.getX() + " Y:" + pos.getY() + " Z:" + pos.getZ());
							player = new PlayerDispatcher(decoder, streamURL, this.worldObj, pos.getX(), pos.getY(), pos.getZ());
							OpenFM.playerList.add(player);	
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
		if (OpenFM.playerList.contains(player)) {
			if (side == Side.CLIENT) {
				player.stop();
			}
			OpenFM.playerList.remove(player);
			isPlaying = false;
		}
		isPlaying = false;
	}

	public boolean isPlaying() {
		return isPlaying;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void invalidate() {
		stopStream();
		super.invalidate();
	}

	@Override
	public void update() {
		if (this.getTicks() > 20) {
			if (this.getScreenText().length() > 6) {
				//scrollText(this);
			}
		}
		Side side = FMLCommonHandler.instance().getEffectiveSide();
		float vol;
		if (side == Side.CLIENT) {
			th++;
			if (th >= OFMConfiguration.maxSpeakers) {
				for (Speaker s : speakers) {
					IBlockState sb = getWorld().getBlockState(new BlockPos(s.x, s.y, s.z));
					if (!(sb.getBlock() instanceof BlockSpeaker)) {
						if (!getWorld().getChunkFromBlockCoords(new BlockPos(s.x, s.y, s.z)).isLoaded()) {
							break;
						}
						speakers.remove(s);
						break;
					}
				}
				th = 0;
			}
			if ((Minecraft.getMinecraft().thePlayer != null) && player != null && (!isInvalid())) {
				vol = getClosest();
				if (vol > 10000.0F * volume) {
					if (player != null) {
						player.setVolume(0.0F);
					}
				} else {
					float v2 = 10000.0F / vol / 100.0F;
					if (v2 > 1.0F) {
						if (player != null) {
							player.setVolume(1.0F * volume * volume);
						}
					} else {
						if (player != null) {
							player.setVolume(v2 * volume * volume);
						}
					}
				}
				if (vol == 0.0F) {
					invalidate();
				}
			}
		} else {
			if (isPlaying()) {
				if (loops >= 40) {
					PacketHandler.INSTANCE.sendToAllAround(new MessageRadioSync(this).wrap(), new NetworkRegistry.TargetPoint(getWorld().provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 50.0D));
					loops = 0;
				} else {
					loops++;
					
				}
				th++;
				if (th >= 60) {
					for (Speaker s : speakers) {
						if (!(worldObj.getBlockState(new BlockPos(s.x, s.y, s.z)).getBlock() instanceof BlockSpeaker)) {
							if (!worldObj.getChunkFromBlockCoords(new BlockPos(s.x, s.y, s.z)).isLoaded()) {
								break;
							}
							speakers.remove(s);
							break;
						}
					}
					th = 0;
				}
			}

			if ((scheduleRedstoneInput) && (listenToRedstone)) {
				if ((!scheduledRedstoneInput) && (redstoneInput)) {
					isPlaying = (!isPlaying);
					if (getWorld() != null) {
						PacketHandler.INSTANCE.sendToAll(new MessageRadioPlaying(this, isPlaying).wrap());
					}
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
			if(stations.size() > 0) {
				PacketHandler.INSTANCE.sendToDimension(new MessageRadioAddStation(this, station).wrap(), getWorld().provider.getDimension());
				getUpdateTag();
				markDirty();
			}
		}
	}

	public void delStation(String station) {
		if (station != null && stations.contains(station)) {
			stations.remove(station);
			PacketHandler.INSTANCE.sendToDimension(new MessageRadioDelStation(this, station).wrap(), getWorld().provider.getDimension());
			getUpdateTag();
			markDirty();
		}
	}

	public String getNext(String uid) {
		int idx = stations.indexOf(uid);
		if (idx < 0 || idx+1 == stations.size()) {
			return uid;
		}
		return stations.get(idx + 1);
	}

	public String getPrevious(String uid) {
		int idx = stations.indexOf(uid);
		if (idx <= 0 || idx-1 == stations.size()) {
			return uid;
		}
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
		//worldObj.markBlockForUpdate(pos);
		getUpdateTag();
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
		//worldObj.markBlockForUpdate(pos);
		getUpdateTag();
		markDirty();
	}

	public String getScreenText() {
		return this.screenText;
	}

	public int addSpeaker(World w, int x, int y, int z) {
		int ret = canAddSpeaker(w, x, y, z);
		if (ret == 0) {
			speakers.add(new Speaker(x, y, z, w));
		}
		return ret;
	}

	public int canAddSpeaker(World w, int x, int y, int z) {
		if (speakers.size() >= OFMConfiguration.maxSpeakers) {
			return 1;
		}
		for (Speaker s : speakers) {
			if ((s.x == x) && (s.y == y) && (s.z == z)) {
				return 2;
			}
		}
		return 0;
	}

	public float getVolume() {
		return volume;
	}

	private float getClosest() {
		float closest = (float) getDistanceSq(Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posY, Minecraft.getMinecraft().thePlayer.posZ);
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
	public NBTTagCompound getUpdateTag() {
		for (Speaker s :speakers) {
			PacketHandler.INSTANCE.sendToDimension(new MessageRadioAddSpeaker(this, s).wrap(), getWorld().provider.getDimension());
		}
		if(this.streamURL != null) {
			PacketHandler.INSTANCE.sendToAllAround(new MessageRadioSync(this).wrap(), new NetworkRegistry.TargetPoint(getWorld().provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 30.0D));
		}
		//PacketHandler.INSTANCE.sendToDimension(new MessageTERadioBlock(this), getWorldObj().provider.dimensionId);

		NBTTagCompound tagCom = new NBTTagCompound();
		this.writeToNBT(tagCom);
		return tagCom;
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
		readFromNBT(packet.getNbtCompound());
	}


	@Override
	public void readFromNBT(NBTTagCompound nbt) {
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
			int x = nbt.getInteger("speakerX" + i);
			int y = nbt.getInteger("speakerY" + i);
			int z = nbt.getInteger("speakerZ" + i);
			addSpeaker(getWorld(), x, y, z);
		}
		for(int i = 0; i < this.getStationCount(); i++) {
			stations.add(nbt.getString("station" + i));
		}
		NBTTagList var2 = nbt.getTagList("Items",nbt.getId());
		this.RadioItemStack = new ItemStack[this.getSizeInventory()];
		for (int var3 = 0; var3 < var2.tagCount(); ++var3) {
			NBTTagCompound var4 = var2.getCompoundTagAt(var3);
			byte var5 = var4.getByte("Slot");
			if (var5 >= 0 && var5 < this.RadioItemStack.length) {
				this.RadioItemStack[var5] = ItemStack.loadItemStackFromNBT(var4);
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		if (this.streamURL != null) {
			nbt.setString("streamurl", this.streamURL);
		}
		nbt.setFloat("volume", this.volume);
		nbt.setBoolean("input", this.listenToRedstone);
		nbt.setBoolean("lastInput", this.redstoneInput);
		nbt.setBoolean("lastState", this.isPlaying);
		nbt.setInteger("speakersCount", this.speakers.size());
		nbt.setInteger("screenColor", this.screenColor);
		if (this.screenText != null) {
			nbt.setString("screenText", this.screenText);
		}
		nbt.setBoolean("isLocked", this.isLocked);
		if (this.owner != null) {
			nbt.setString("owner", this.owner);
		}
		for (int i = 0; i < this.speakers.size(); i++) {
			nbt.setInteger("speakerX" + i, this.speakers.get(i).x);
			nbt.setInteger("speakerY" + i, this.speakers.get(i).y);
			nbt.setInteger("speakerZ" + i, this.speakers.get(i).z);
		}
		for(int i = 0; i < stations.size(); i++)
		{
			String s = stations.get(i);
			if(s != null) {
				nbt.setString("station" + i, s);
				nbt.setInteger("stationCount", i + 1);
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
		return nbt;
	}


	public enum ComputerMethod {
		getAttachedSpeakerCount, //No args
		setScreenColor, //Integer (0x######)
		getScreenColor, //No args
		setListenRedstone, //Boolean
		getListenRedstone, //No args
		isPlaying, //No args
		stop, //No args
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
			getUpdateTag();
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
			getUpdateTag();
			return new Object[]{ true };

		case start:
			try {
				startStream();
			} catch (Exception e) {
				e.printStackTrace();
			}
			getUpdateTag();
			return new Object[]{ true };

		case greet:
			return new Object[] { "Lasciate ogne speranza, voi ch'intrate" };

		case getAttachedSpeakers:
			return new Object[] { this.speakers.size() };

		case setScreenText:
			if(args.length != 1) {
				return new Object[]{false, "Insufficient number of arguments, expected 1"};
			}
			String tempString = new String((byte[]) args[0], StandardCharsets.UTF_8);
			setScreenText(tempString);
			getUpdateTag();
			markDirty(); // Marks the chunk as dirty, so that it is saved properly on changes. Not required for the sync specifically, but usually goes alongside the former.	
			return new Object[] { true } ;

		case volDown:
			float v = (float)(this.volume - 0.1D);
			if ((v > 0.0F) && (v <= 1.0F)) {
				setVolume(v);
				getUpdateTag();
				markDirty();
				return new Object[] { getVolume() };
			} else {
				return new Object[] { false };
			}

		case volUp:
			float v1 = (float)(this.volume + 0.1D);
			if ((v1 > 0.0F) && (v1 <= 1.0F)) {
				setVolume(v1);
				getUpdateTag();
				markDirty();
				return new Object[] { getVolume() };
			} else {
				return new Object[] { false };
			}

		case setVol:
			if(args.length != 1) {
				return new Object[]{false, "Insufficient number of arguments, expected 1"};
			}
			float v2 = (float)(args[0]);
			if ((v2 > 0.0F) && (v2 <= 1.0F)) {
				setVolume(v2);
				getUpdateTag();
				markDirty();
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
				getUpdateTag();
				markDirty();
				return new Object[] { true };
			}
			return new Object[] { false, "Error parsing URL in packet" };

		default: return new Object[]{false, "Not implemented."};
		}
	}


	@Override
	@Optional.Method(modid = "OpenComputers")
	public Object[] invoke(final String method, final Context context,
			final Arguments args) throws Exception {
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
	public ItemStack removeStackFromSlot(int i) {
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
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return worldObj.getTileEntity(new BlockPos(pos.getX(), pos.getY(), pos.getZ())) == this && player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64;
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
			RadioItemStack[0].getTagCompound().setString("screenText", this.screenText);	
			RadioItemStack[0].getTagCompound().setInteger("screenColor", this.screenColor);
			RadioItemStack[0].getTagCompound().setString("streamURL", this.streamURL);
			RadioItemStack[0].getTagCompound().setInteger("stationCount", this.stationCount);
			for(int i = 0; i < this.getStationCount(); i++)
			{
				RadioItemStack[0].getTagCompound().setString("station" + i, stations.get(i));
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
					stations.add(RadioItemStack[0].getTagCompound().getString("station" + i));
				}
				getUpdateTag();
				markDirty();
			}
		}
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "ofm_radio";
	}

	@Override
	public boolean hasCustomName() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ITextComponent getDisplayName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void openInventory(EntityPlayer player) {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeInventory(EntityPlayer player) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getField(int id) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setField(int id, int value) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getFieldCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	public void setOwner(String inOwner) {
		this.owner = inOwner;

	}

	public void incTicks() {
		this.ticks++;
	}

	public int getTicks() {
		return this.ticks;
	}

	public int getRenderCount() {
		return renderCount;
	}

	public void incRenderCount() {
		this.renderCount++;
	}

	public void resetRenderCount() {
		this.renderCount = 0;
	}

	public void resetTicks() {
		this.ticks = 0;
	}
	
	@SideOnly(Side.CLIENT)
	public String scrollText(TileEntityRadio radio) {
		Minecraft mc = Minecraft.getMinecraft();
		FontRenderer fontRenderer = mc.getRenderManager().getFontRenderer();
		String text = "       " + this.screenText + "        ";
		if (text.length() > radio.getRenderCount() + 6 && text.trim().length() > 6) {
			this.incTicks();
			if(this.getTicks() % 20 == 0) {
				screenOut = text.substring(radio.getRenderCount(), radio.getRenderCount() + 6);
				if (fontRenderer.getStringWidth(screenOut) / 6 < 5) {
					screenOut = text.substring(radio.getRenderCount(), radio.getRenderCount() + 7);
				}
				radio.incRenderCount();
				radio.resetTicks();
				if (radio.getRenderCount() > text.length()) {
					radio.resetRenderCount();
				}
			}
		} else if (text.trim().length() <= 6) {
			screenOut = this.screenText;
		} else {
			radio.resetRenderCount();
		}
		return screenOut;
	}
}
