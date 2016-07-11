package pcl.OpenFM.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pcl.OpenFM.TileEntity.TileEntityRadio;

public class MessageRadioVolume extends BaseRadioMessage {
	private float volume;

	public MessageRadioVolume() {}

	public MessageRadioVolume(TileEntityRadio radio, float v) {
		super(radio);
		this.volume = v;
	}

	@Override
	public void onMessage(TileEntityRadio radio, MessageContext ctx) {
		if ((volume > 0.0F) && (volume <= 1.0F)) {
			radio.volume = volume;
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		volume = buf.readFloat();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		buf.writeFloat(volume);
	}
}
