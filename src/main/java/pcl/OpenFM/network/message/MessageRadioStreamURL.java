package pcl.OpenFM.network.message;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import pcl.OpenFM.TileEntity.TileEntityRadio;

public class MessageRadioStreamURL extends BaseRadioMessage {
	private String streamURL;

	public MessageRadioStreamURL() {}

	public MessageRadioStreamURL(TileEntityRadio radio, String streamURL) {
		super(radio);
		this.streamURL = streamURL;
	}

	@Override
	public void onMessage(TileEntityRadio radio, MessageContext ctx) {
		if (radio.streamURL != streamURL) {
			if (radio.isPlaying()) {
				radio.stopStream();
				radio.streamURL = streamURL;
				try {
					radio.startStream();
				} catch (Exception e) {
					radio.stopStream();
				}
			} else {
				radio.streamURL = streamURL;
			}
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		streamURL = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		ByteBufUtils.writeUTF8String(buf, streamURL);
	}
}
