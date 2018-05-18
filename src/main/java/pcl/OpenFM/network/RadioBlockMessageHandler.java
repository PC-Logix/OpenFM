package pcl.OpenFM.network;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pcl.OpenFM.TileEntity.TileEntityRadio;
import pcl.OpenFM.network.message.BaseRadioMessage;

public class RadioBlockMessageHandler {

	public static class ClientHandler implements IMessageHandler<MessageRadioBase, IMessage> {
		@Override
		public IMessage onMessage(MessageRadioBase _message, final MessageContext ctx) {
			final BaseRadioMessage message = _message.message;

			Minecraft.getMinecraft().addScheduledTask(new Runnable() {
				@Override
				public void run() {
					World world = FMLClientHandler.instance().getClient().world;
					TileEntity tileEntity = world.getTileEntity(new BlockPos(message.x, message.y, message.z));
					if ((tileEntity instanceof TileEntityRadio)) {
						message.onMessage((TileEntityRadio) tileEntity, ctx);
					}
				}
			});
			return null;
		}
	}

	public static class ServerHandler implements IMessageHandler<MessageRadioBase, IMessage> {
		@Override
		public IMessage onMessage(MessageRadioBase _message, final MessageContext ctx) {
			final BaseRadioMessage message = _message.message;

			if (message.shouldBroadcast())
				PacketHandler.INSTANCE.sendToAll(_message);

			((IThreadListener) ctx.getServerHandler().player.world).addScheduledTask(new Runnable() {
				@Override
				public void run() {
					World world = ctx.getServerHandler().player.world;
					TileEntity tileEntity = world.getTileEntity(new BlockPos(message.x, message.y, message.z));
					if ((tileEntity instanceof TileEntityRadio)) {
						message.onMessage((TileEntityRadio) tileEntity, ctx);
					}
				}
			});
			return null;
		}
	}
}
