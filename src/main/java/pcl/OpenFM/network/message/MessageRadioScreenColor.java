package pcl.OpenFM.network.message;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import pcl.OpenFM.TileEntity.TileEntityRadio;

public class MessageRadioScreenColor extends BaseRadioMessage {
	private int screenColor;

	public MessageRadioScreenColor() {}

	public MessageRadioScreenColor(TileEntityRadio radio, int screenColor) {
		super(radio);
		this.screenColor = screenColor;
	}

	@Override
	public void onMessage(TileEntityRadio radio, MessageContext ctx) {
		radio.setScreenColor(screenColor);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		screenColor = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		buf.writeInt(screenColor);
	}
}
