package pcl.OpenFM.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pcl.OpenFM.TileEntity.TileEntityRadio;

public class MessageRadioDelStation extends BaseRadioMessage {
	private String streamURL;

	public MessageRadioDelStation() {}

	public MessageRadioDelStation(TileEntityRadio radio, String streamURL) {
		super(radio);
		this.streamURL = streamURL;
	}

	@Override
	public void onMessage(TileEntityRadio radio, MessageContext ctx) {
		radio.delStation(streamURL);
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
