package pcl.OpenFM.network;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import pcl.OpenFM.TileEntity.TileEntityRadio;
import pcl.OpenFM.network.message.BaseRadioMessage;

public class RadioBlockMessageHandler {

	public static class ClientHandler implements IMessageHandler<MessageRadioBase, IMessage> {
		@Override
		public IMessage onMessage(MessageRadioBase _message, final MessageContext ctx) {
			final BaseRadioMessage message = _message.message;

			TileEntity tileEntity = FMLClientHandler.instance().getClient().theWorld.getTileEntity(message.x, message.y, message.z);
			if ((tileEntity instanceof TileEntityRadio)) {
				message.onMessage((TileEntityRadio) tileEntity, ctx);
			}

			return null;
		}
	}

	public static class ServerHandler implements IMessageHandler<MessageRadioBase, IMessage> {
		@Override
		public IMessage onMessage(MessageRadioBase _message, final MessageContext ctx) {
			final BaseRadioMessage message = _message.message;

			if (message.shouldBroadcast())
				PacketHandler.INSTANCE.sendToAll(_message);

			World world = ctx.getServerHandler().playerEntity.worldObj;
			TileEntity tileEntity = world.getTileEntity(message.x, message.y, message.z);
			if ((tileEntity instanceof TileEntityRadio)) {
				message.onMessage((TileEntityRadio) tileEntity, ctx);
			}

			return null;
		}
	}
}
