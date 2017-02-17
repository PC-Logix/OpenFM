package pcl.OpenFM.network.message;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import pcl.OpenFM.TileEntity.TileEntityRadio;

public class MessageRadioSync extends BaseRadioMessage {
	private String streamURL = "";
	private int screenColor = 0x0000FF;
	private String screenText = "OpenFM";
	private boolean playing;
	private float volume;

	public MessageRadioSync() {}

	public MessageRadioSync(TileEntityRadio radio) {
		super(radio);
		this.streamURL = radio.streamURL;
		this.screenColor = radio.getScreenColor();
		this.screenText = radio.getScreenText();
		this.playing = radio.isPlaying;
		this.volume = radio.volume;
	}

	@Override
	public void onMessage(TileEntityRadio radio, MessageContext ctx) {
		radio.streamURL = streamURL;
		radio.screenColor = screenColor;
		radio.screenText = screenText;
		radio.volume = volume;
		if (playing) {
			if (radio.isValid) {
				try {
					radio.startStream();
				} catch (Exception e) {
					radio.stopStream();
				}
			}
		} else {
			radio.stopStream();
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		streamURL = ByteBufUtils.readUTF8String(buf);
		screenColor = buf.readInt();
		screenText = ByteBufUtils.readUTF8String(buf);
		playing = buf.readBoolean();
		volume = buf.readFloat();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		ByteBufUtils.writeUTF8String(buf, streamURL);
		buf.writeInt(screenColor);
		ByteBufUtils.writeUTF8String(buf, screenText);
		buf.writeBoolean(playing);
		buf.writeFloat(volume);
	}
}
