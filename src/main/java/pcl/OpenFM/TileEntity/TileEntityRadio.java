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

@Optional.InterfaceList(value={
		@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")
})
public class TileEntityRadio extends TileEntity implements SimpleComponent {
	public MP3Player player = null;
	public boolean isPlaying = false;
	public String streamURL = "";
	public boolean blockExists = true;
	private World world;
	public float volume = 0.1F;
	private boolean redstoneInput = false;
	public boolean listenToRedstone = false;
	private boolean scheduledRedstoneInput = false;
	private boolean scheduleRedstoneInput = false;
	public ArrayList<Speaker> speakers = new ArrayList<Speaker>();
	private int th = 0;
	private int screenColor = 0x0000FF;
	double cx = 0.0D; double cy = 0.0D; double cz = 0.0D;

	private int speakersCount = 0;

	public TileEntityRadio(World w) {
		this.world = w;
		if (this.isPlaying)
			startStream();
	}

	public TileEntityRadio() {
		if (this.isPlaying) {
			startStream();
		}
	}

	public void deleted()
	{
		this.blockExists = false;
	}

	public void setWorld(World w)
	{
		this.world = w;
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
		return new Object[] { true };
	}

	public void startStream()
	{
		Side side = FMLCommonHandler.instance().getEffectiveSide();
		if (!OpenFM.playerList.contains(this.player))
		{
			this.isPlaying = true;
			if (side == Side.CLIENT)
			{
				this.player = new MP3Player(this.streamURL, this.world, this.xCoord, this.yCoord, this.zCoord);
				OpenFM.playerList.add(this.player);
			}
		}
	}

	@Optional.Method(modid = "OpenComputers")
	@Callback
	public Object[] stop(Context context, Arguments args) {
		stopStream();
		this.isPlaying = false;
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		getDescriptionPacket();
		return new Object[] { true };
	}

	public void stopStream()
	{
		Side side = FMLCommonHandler.instance().getEffectiveSide();
		if (OpenFM.playerList.contains(this.player))
		{
			if (side == Side.CLIENT)
			{
				this.player.stop();
			}
			OpenFM.playerList.remove(this.player);
			this.isPlaying = false;
		}
	}

	@Optional.Method(modid = "OpenComputers")
	@Callback
	public Object[] isPlaying(Context context, Arguments args) {
		return new Object[] { isPlaying() };
	}

	public boolean isPlaying()
	{
		return this.isPlaying;
	}

	@SideOnly(Side.CLIENT)
	public void invalidate()
	{
		stopStream();
		super.invalidate();
	}

	public void updateEntity()
	{
		Side side = FMLCommonHandler.instance().getEffectiveSide();
		float vol; if (side == Side.CLIENT) {
			this.th += 1;
			if (this.th >= 10) {
				this.th = 0;
				for (Speaker s : this.speakers) {
					Block sb = getWorldObj().getBlock((int)s.x, (int)s.y, (int)s.z);

					if (!(sb instanceof BlockSpeaker)) {
						if (!getWorldObj().getChunkFromBlockCoords((int)s.x, (int)s.z).isChunkLoaded) break;
						this.speakers.remove(s); break;
					}
				}
			}

			if ((Minecraft.getMinecraft().thePlayer != null) && (this.player != null) && 
					(!isInvalid())) {
				vol = getClosest();
				if (vol > 10000.0F * this.volume) {
					this.player.setVolume(0.0F);
				} else {
					float v2 = 10000.0F / vol / 100.0F;
					if (v2 > 1.0F) {
						this.player.setVolume(1.0F * this.volume * this.volume);
					} else {
						this.player.setVolume(v2 * this.volume * this.volume);
					}
				}
				if (vol == 0.0F) {
					invalidate();
				}
			}
		} else {
			if (isPlaying()) {
				this.th += 1;
				if (this.th >= 60) {
					this.th = 0;
					for (Speaker s : this.speakers) {
						if (!(this.worldObj.getBlock((int)s.x, (int)s.y, (int)s.z) instanceof BlockSpeaker))
						{
							if (!this.worldObj.getChunkFromBlockCoords((int)s.x, (int)s.z).isChunkLoaded) break;
							this.speakers.remove(s); break;
						}
					}
				}
			}


			if ((this.scheduleRedstoneInput) && (this.listenToRedstone)) {
				if ((!this.scheduledRedstoneInput) && (this.redstoneInput)) {
					this.isPlaying = (!this.isPlaying);
					PacketHandler.INSTANCE.sendToAll(new MessageTERadioBlock(this.xCoord, this.yCoord, this.zCoord, 

							getWorldObj(), this.streamURL, this.isPlaying, this.volume, 1));
				}

				this.redstoneInput = this.scheduledRedstoneInput;
				this.scheduleRedstoneInput = false;
				this.scheduledRedstoneInput = false;
			}
		}
		
	}

	@Optional.Method(modid = "OpenComputers")
	@Callback
	public Object[] setURL(Context context, Arguments args) {
		this.streamURL = args.checkString(0);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		getDescriptionPacket();
		return new Object[] { true };
	}

	@Optional.Method(modid = "OpenComputers")
	@Callback
	public Object[] getVol(Context context, Arguments args) {
		return new Object[] { this.volume };
	}

	@Optional.Method(modid = "OpenComputers")
	@Callback
	public Object[] setVol(Context context, Arguments args) {
		float v = (float)(args.checkInteger(0));
		if ((v > 0.0F) && (v <= 1.0F)) {
			this.volume = args.checkInteger(0);
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			getDescriptionPacket();
			return new Object[] { this.volume };
		} else {
			return new Object[] { false };
		}
	}

	@Optional.Method(modid = "OpenComputers")
	@Callback
	public Object[] volUp(Context context, Arguments args) {
		float v = (float)(this.volume + 0.1D);
		if ((v > 0.0F) && (v <= 1.0F)) {
			this.volume = v;
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			getDescriptionPacket();
			return new Object[] { this.volume };
		} else {
			return new Object[] { false };
		}
	}

	@Optional.Method(modid = "OpenComputers")
	@Callback
	public Object[] volDown(Context context, Arguments args) {
		float v = (float)(this.volume - 0.1D);
		if ((v > 0.0F) && (v <= 1.0F)) {
			this.volume = v;
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			getDescriptionPacket();
			return new Object[] { this.volume };
		} else {
			return new Object[] { false };
		}
	}

	public void readFromNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.readFromNBT(par1NBTTagCompound);
		this.streamURL = par1NBTTagCompound.getString("streamurl");
		this.volume = par1NBTTagCompound.getFloat("volume");
		this.listenToRedstone = par1NBTTagCompound.getBoolean("input");
		this.redstoneInput = par1NBTTagCompound.getBoolean("lastInput");
		this.isPlaying = par1NBTTagCompound.getBoolean("lastState");
		this.speakersCount = par1NBTTagCompound.getInteger("speakersCount");
		this.screenColor = par1NBTTagCompound.getInteger("screenColor");
		for (int i = 0; i < this.speakersCount; i++) {
			double x = par1NBTTagCompound.getDouble("speakerX" + i);
			double y = par1NBTTagCompound.getDouble("speakerY" + i);
			double z = par1NBTTagCompound.getDouble("speakerZ" + i);
			addSpeaker(getWorldObj(), x, y, z);
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
		for (int i = 0; i < this.speakers.size(); i++) {
			par1NBTTagCompound.setDouble("speakerX" + i, ((Speaker)this.speakers.get(i)).x);
			par1NBTTagCompound.setDouble("speakerY" + i, ((Speaker)this.speakers.get(i)).y);
			par1NBTTagCompound.setDouble("speakerZ" + i, ((Speaker)this.speakers.get(i)).z);
		}
	}

	@Override
	public Packet getDescriptionPacket()
	{
		for (Speaker s : this.speakers) {
			PacketHandler.INSTANCE.sendToDimension(new MessageTERadioBlock(this.xCoord, this.yCoord, this.zCoord, this.worldObj, "", false, 1.0F, 15, s.x, s.y, s.z), 
					getWorldObj().provider.dimensionId);
		}
		int mode = 13;
		if (this.listenToRedstone)
			mode = 14;
		PacketHandler.INSTANCE.getPacketFrom(new MessageTERadioBlock(this));
		
	    NBTTagCompound tagCom = new NBTTagCompound();
	    this.writeToNBT(tagCom);
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, this.blockMetadata, tagCom);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
		readFromNBT(packet.func_148857_g());
	}
	
	@Optional.Method(modid = "OpenComputers")
	@Callback
	public Object[] getListenRedstone(Context context, Arguments args) {
		return new Object[] { getListenRedstoneInput() };
	}
	
	public boolean getListenRedstoneInput() {
		return this.listenToRedstone;
	}

	@Optional.Method(modid = "OpenComputers")
	@Callback
	public Object[] setListenRedstone(Context context, Arguments args) {
		setRedstoneInput(args.checkBoolean(0));
		return new Object[] { getListenRedstoneInput() };
	}
	
	@Optional.Method(modid = "OpenComputers")
	@Callback
	public Object[] getScreenColor(Context context, Arguments args) {
		return new Object[] { this.getScreenColor() };
	}
	
	public int getScreenColor() {
		return screenColor;
	}
	
	@Optional.Method(modid = "OpenComputers")
	@Callback
	public Object[] setScreenColor(Context context, Arguments args) {
		setScreenColor(args.checkInteger(0));
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		getDescriptionPacket();
		return new Object[] { true };
	}
	
	public void setScreenColor(Integer color) {
		this.screenColor = color;
	}
	
	public void setRedstoneInput(boolean input) {
		if (input) {
			this.scheduledRedstoneInput = input;
		}
		this.scheduleRedstoneInput = true;
	}

	@Optional.Method(modid = "OpenComputers")
	@Callback
	public Object[] getAttachedSpeakers(Context context, Arguments args) {
		return new Object[] { this.speakers.size() };
	}
	
	public int addSpeaker(World w, double x, double y, double z)
	{
		if (this.speakers.size() >= 10)
			return 1;
		for (Speaker s : this.speakers)
			if ((s.x == x) && (s.y == y) && (s.z == z))
				return 2;
		this.speakers.add(new Speaker(x, y, z, w));
		return 0;
	}

	public int canAddSpeaker(World w, double x, double y, double z)
	{
		if (this.speakers.size() >= 10)
			return 1;
		for (Speaker s : this.speakers)
			if ((s.x == x) && (s.y == y) && (s.z == z))
				return 2;
		return 0;
	}


	private float getClosest()
	{
		float closest = (float)getDistanceFrom(Minecraft.getMinecraft().thePlayer.posX, 
				Minecraft.getMinecraft().thePlayer.posY, Minecraft.getMinecraft().thePlayer.posZ);
		if (!this.speakers.isEmpty()) {
			for (Speaker s : this.speakers) {
				float distance = (float)Math.pow(Minecraft.getMinecraft().thePlayer.getDistance(s.x, s.y, s.z), 2.0D);
				if (closest > distance) {
					closest = distance;
				}
			}
		}
		return closest;
	}

	@Override
	public String getComponentName() {
		// TODO Auto-generated method stub
		return "openfm_radio";
	}
}


