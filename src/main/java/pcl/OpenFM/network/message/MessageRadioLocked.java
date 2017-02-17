package pcl.OpenFM.network.message;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import pcl.OpenFM.TileEntity.TileEntityRadio;

public class MessageRadioLocked extends BaseRadioMessage {
	private boolean locked;

	public MessageRadioLocked() {}

	public MessageRadioLocked(TileEntityRadio radio, boolean locked) {
		super(radio);
		this.locked = locked;
	}

	@Override
	public void onMessage(TileEntityRadio radio, MessageContext ctx) {
		radio.isLocked = locked;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		locked = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		buf.writeBoolean(locked);
	}
}
