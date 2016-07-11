package pcl.OpenFM.network;

import pcl.OpenFM.network.message.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("openfm");

	public static void init() {
		INSTANCE.registerMessage(RadioBlockMessageHandler.ServerHandler.class, MessageRadioBase.class, 0, Side.SERVER);
		INSTANCE.registerMessage(RadioBlockMessageHandler.ClientHandler.class, MessageRadioBase.class, 1, Side.CLIENT);

		MessageRadioBase.registerMessage(MessageRadioPlaying.class);
		MessageRadioBase.registerMessage(MessageRadioSync.class);
		MessageRadioBase.registerMessage(MessageRadioAddSpeaker.class);
		MessageRadioBase.registerMessage(MessageRadioAddStation.class);
		MessageRadioBase.registerMessage(MessageRadioDelStation.class);
		MessageRadioBase.registerMessage(MessageRadioLocked.class);
		MessageRadioBase.registerMessage(MessageRadioRedstone.class);
		MessageRadioBase.registerMessage(MessageRadioReadCard.class);
		MessageRadioBase.registerMessage(MessageRadioWriteCard.class);
		MessageRadioBase.registerMessage(MessageRadioStreamURL.class);
		MessageRadioBase.registerMessage(MessageRadioScreenColor.class);
		MessageRadioBase.registerMessage(MessageRadioScreenText.class);
		MessageRadioBase.registerMessage(MessageRadioVolume.class);
	}
}
