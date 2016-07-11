package pcl.OpenFM.network.message;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pcl.OpenFM.TileEntity.TileEntityRadio;

public class MessageRadioWriteCard extends BaseRadioMessage {

	public MessageRadioWriteCard() {}

	public MessageRadioWriteCard(TileEntityRadio radio) {
		super(radio);
	}

	@Override
	public boolean shouldBroadcast() {
		return false;
	}

	@Override
	public void onMessage(TileEntityRadio radio, MessageContext ctx) {
		radio.writeDataToCard();
	}
}
