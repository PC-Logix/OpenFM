package pcl.OpenFM.TileEntity;

import li.cil.oc.api.Network;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntitySpeaker extends TileEntity implements Environment {
	protected Node node = Network.newNode(this, Visibility.Network).create();

	@Override
	public Node node() {
		return node;
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if (node != null)
			node.remove();
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (node != null)
			node.remove();
	}

	@Override
	public void onConnect(final Node node) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onMessage(Message message) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDisconnect(Node node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if (node != null && node.host() == this) {
			node.load(nbt.getCompoundTag("oc:node"));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		if (node != null && node.host() == this) {
			final NBTTagCompound nodeNbt = new NBTTagCompound();
			node.save(nodeNbt);
			nbt.setTag("oc:node", nodeNbt);
		}
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (node != null && node.network() == null) {
			Network.joinOrCreateNetwork(this);
		}
	}
}