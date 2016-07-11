package pcl.OpenFM.network.message;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pcl.OpenFM.TileEntity.TileEntityRadio;

public class MessageRadioReadCard extends BaseRadioMessage {

	public MessageRadioReadCard() {}

	public MessageRadioReadCard(TileEntityRadio radio) {
		super(radio);
	}

	@Override
	public boolean shouldBroadcast() {
		return false;
	}

	@Override
	public void onMessage(TileEntityRadio radio, MessageContext ctx) {
		radio.readDataFromCard();
	}
}
