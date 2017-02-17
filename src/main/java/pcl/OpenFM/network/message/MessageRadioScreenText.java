package pcl.OpenFM.network.message;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import pcl.OpenFM.TileEntity.TileEntityRadio;

public class MessageRadioScreenText extends BaseRadioMessage {
	private String screenText;

	public MessageRadioScreenText() {}

	public MessageRadioScreenText(TileEntityRadio radio, String screenText) {
		super(radio);
		this.screenText = screenText;
	}

	@Override
	public void onMessage(TileEntityRadio radio, MessageContext ctx) {
		radio.setScreenText(screenText);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		screenText = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		ByteBufUtils.writeUTF8String(buf, screenText);
	}
}
