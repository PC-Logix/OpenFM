package pcl.OpenFM.network.message;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import pcl.OpenFM.TileEntity.TileEntityRadio;
import pcl.OpenFM.misc.Speaker;

public class MessageRadioAddSpeaker extends BaseRadioMessage {
	private int tx, ty, tz;

	public MessageRadioAddSpeaker() {}

	public MessageRadioAddSpeaker(TileEntityRadio radio, Speaker speaker) {
		super(radio);
		this.tx = speaker.x;
		this.ty = speaker.y;
		this.tz = speaker.z;
	}

	@Override
	public void onMessage(TileEntityRadio radio, MessageContext ctx) {
		radio.addSpeaker(radio.getWorldObj(), tx, ty, tz);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		tx = buf.readInt();
		ty = buf.readInt();
		tz = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		buf.writeInt(tx);
		buf.writeInt(ty);
		buf.writeInt(tz);
	}
}
