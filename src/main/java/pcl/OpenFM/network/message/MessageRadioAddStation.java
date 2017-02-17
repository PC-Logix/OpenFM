package pcl.OpenFM.network.message;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import pcl.OpenFM.TileEntity.TileEntityRadio;

public class MessageRadioAddStation extends BaseRadioMessage {
	private String streamURL;

	public MessageRadioAddStation() {}

	public MessageRadioAddStation(TileEntityRadio radio, String streamURL) {
		super(radio);
		this.streamURL = streamURL;
	}

	@Override
	public void onMessage(TileEntityRadio radio, MessageContext ctx) {
		if (!radio.stations.contains(streamURL))
			radio.addStation(streamURL);
		radio.setStationCount(radio.stations.size());
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
