package pcl.OpenFM.network;

import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import pcl.OpenFM.OpenFM;
import pcl.OpenFM.network.message.BaseRadioMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessage;

public class MessageRadioBase implements IMessage {
	BaseRadioMessage message;
	private static int id = 0;
	private static Map<Integer, Class<? extends BaseRadioMessage>> idmap = new HashMap<Integer, Class<? extends BaseRadioMessage>>();
	private static Map<Class<? extends BaseRadioMessage>, Integer> msgmap = new HashMap<Class<? extends BaseRadioMessage>, Integer>();

	public static void registerMessage(Class<? extends BaseRadioMessage> message) {
		idmap.put(id, message);
		msgmap.put(message, id++);
	}

	public MessageRadioBase() {}

	public MessageRadioBase(BaseRadioMessage message) {
		this.message = message;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		int id = buf.readInt();
		if (idmap.containsKey(id)) {
			Class<? extends BaseRadioMessage> radioMessage = idmap.get(id);
			try {
				message = radioMessage.newInstance();
				message.fromBytes(buf);
			} catch (InstantiationException | IllegalAccessException e) {
				OpenFM.logger.error("Failed to create instance of " + radioMessage.getSimpleName(), e);
			}
		} else {
			OpenFM.logger.error("No radio message for ID: " + id);
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		if (this.message != null) {
			buf.writeInt(msgmap.get(this.message.getClass()));
			message.toBytes(buf);
		} else {
			buf.writeInt(-1);
		}
	}
}