package pcl.OpenFM.network.message;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import pcl.OpenFM.TileEntity.TileEntityRadio;
import pcl.OpenFM.network.MessageRadioBase;

public abstract class BaseRadioMessage {
	public int x, y, z;

	public BaseRadioMessage() {}

	public BaseRadioMessage(TileEntityRadio radio) {
		x = radio.xCoord;
		y = radio.yCoord;
		z = radio.zCoord;
	}

	public abstract void onMessage(TileEntityRadio radio, MessageContext ctx);

	public boolean shouldBroadcast() {
		return true;
	}

	public MessageRadioBase wrap() {
		return new MessageRadioBase(this);
	}

	public void fromBytes(ByteBuf buf) {
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
	}

	public void toBytes(ByteBuf buf) {
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
	}
}
