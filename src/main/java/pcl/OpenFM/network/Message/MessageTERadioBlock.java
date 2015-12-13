package pcl.OpenFM.network.Message;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.World;
import pcl.OpenFM.TileEntity.TileEntityRadio;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MessageTERadioBlock implements IMessage {
	public World world;
	public float volume;
	public boolean isPlaying;
	public String streamURL;
	public double tz = 0.0D; public double ty = 0.0D; public double tx = 0.0D;

	public int dim;

	public int mode;

	public double z;

	public double y;

	public double x;

	public List<String> stations = new ArrayList<String>();
	
	public int stationCount = 0;
	public int screenColor = 0x0000FF;
	public String screenText = "OpenFM";

	public MessageTERadioBlock() {}

	public MessageTERadioBlock(TileEntityRadio radio) {
		this.x = radio.getPos().getX();
		this.y = radio.getPos().getY();
		this.z = radio.getPos().getZ();
		this.world = radio.getWorld();
		this.dim = this.world.provider.getDimensionId();
		this.streamURL = radio.streamURL;
		this.screenColor = radio.getScreenColor();
		this.screenText = radio.getScreenText();
		this.isPlaying = radio.isPlaying;
		this.volume = radio.volume;
		int mode = 13;
		if (radio.listenToRedstone)
			mode = 14;
		if (radio.isLocked)
			mode = 46;
		this.mode = mode;
	}

	public MessageTERadioBlock(double x, double y, double z, World world, String streamURL, boolean isPlaying, float volume, int mode) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.world = world;
		this.dim = world.provider.getDimensionId();
		this.streamURL = streamURL;
		this.isPlaying = isPlaying;
		this.volume = volume;
		this.mode = mode;
	}

	public MessageTERadioBlock(double x, double y, double z, World world, String streamURL, boolean isPlaying, float volume, int mode, double tx, double ty, double tz) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.world = world;
		this.dim = world.provider.getDimensionId();
		this.streamURL = streamURL;
		this.isPlaying = isPlaying;
		this.volume = volume;
		this.mode = mode;
		this.tx = tx;
		this.ty = ty;
		this.tz = tz;
	}

	public MessageTERadioBlock(int xCoord, int yCoord, int zCoord, World worldObj, String station, int i) {
		this.x = xCoord;
		this.y = yCoord;
		this.z = zCoord;
		this.world = worldObj;
		this.dim = world.provider.getDimensionId();
		this.streamURL = station;
		this.mode = i;
		if (i == 42) {
			if (!this.stations.contains(station)) {
				this.stations.add(station);
				this.stationCount = this.stations.size();
			}
		} else if (i == 43) {
			if (this.stations.contains(station)) {
				this.stations.remove(station);
				this.stationCount = this.stations.size();
			}
		} else if (i == 44) {
			
		} else if (i == 45) {
			
		}
	}
	
	public MessageTERadioBlock(int xCoord, int yCoord, int zCoord, World worldObj, String stream, String screenInfo, int i, int f) {
		this.x = xCoord;
		this.y = yCoord;
		this.z = zCoord;
		this.world = worldObj;
		this.streamURL = stream;
		this.dim = world.provider.getDimensionId();
		this.mode = i;
		if (i == 49) {
			this.screenText = screenInfo;
		} else if (i == 48) {
			this.screenColor = Integer.parseInt(screenInfo, 16);
		}
	}

	public void fromBytes(ByteBuf buf) {
		this.mode = buf.readInt();
		this.x = buf.readDouble();
		this.y = buf.readDouble();
		this.z = buf.readDouble();
		this.dim = buf.readInt();
		int streamURLlength = buf.readInt();
		this.screenColor = buf.readInt();
		int screenTextLength = buf.readInt();
		this.screenText = new String(buf.readBytes(screenTextLength).array());
		this.streamURL = new String(buf.readBytes(streamURLlength).array());
		this.isPlaying = buf.readBoolean();
		this.volume = buf.readFloat();
		this.tx = buf.readDouble();
		this.ty = buf.readDouble();
		this.tz = buf.readDouble();
	}

	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.mode);
		buf.writeDouble(this.x);
		buf.writeDouble(this.y);
		buf.writeDouble(this.z);
		buf.writeInt(this.dim);
		buf.writeInt(this.streamURL.length());
		buf.writeInt(this.screenColor);
		buf.writeInt(this.screenText.length());
		buf.writeBytes(this.screenText.getBytes());
		buf.writeBytes(this.streamURL.getBytes());
		buf.writeBoolean(this.isPlaying);
		buf.writeFloat(this.volume);
		buf.writeDouble(this.tx);
		buf.writeDouble(this.ty);
		buf.writeDouble(this.tz);
	}
}