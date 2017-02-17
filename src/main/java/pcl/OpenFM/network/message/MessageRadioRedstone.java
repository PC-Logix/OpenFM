package pcl.OpenFM.network.message;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import pcl.OpenFM.TileEntity.TileEntityRadio;

public class MessageRadioRedstone extends BaseRadioMessage {
	private boolean redstone;

	public MessageRadioRedstone() {}

	public MessageRadioRedstone(TileEntityRadio radio, boolean redstone) {
		super(radio);
		this.redstone = redstone;
	}

	@Override
	public void onMessage(TileEntityRadio radio, MessageContext ctx) {
		radio.listenToRedstone = redstone;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		redstone = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		buf.writeBoolean(redstone);
	}
}
