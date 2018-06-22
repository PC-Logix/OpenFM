package pcl.OpenFM.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pcl.OpenFM.TileEntity.TileEntityRadio;

public class MessageRadioPlaying extends BaseRadioMessage {
	private boolean playing;

	public MessageRadioPlaying() {}

	public MessageRadioPlaying(TileEntityRadio radio, boolean playing) {
		super(radio);
		this.playing = playing;
	}

	public MessageRadioPlaying(int x, int y, int z, boolean playing) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.playing = playing;
	}

	@Override
	public void onMessage(TileEntityRadio radio, MessageContext ctx) {
		if (playing) {
			System.out.println(ctx.side);
			if (radio.isValid) {
				try {
					radio.startStream();
				} catch (Exception e) {
					e.printStackTrace();
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
		playing = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		buf.writeBoolean(playing);
	}
}
