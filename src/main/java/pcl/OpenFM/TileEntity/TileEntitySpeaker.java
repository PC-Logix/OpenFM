package pcl.OpenFM.TileEntity;

import net.minecraftforge.fml.common.Optional;
import li.cil.oc.api.Network;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

@Optional.InterfaceList(value={
		@Optional.Interface(iface = "li.cil.oc.api.network.Environment", modid = "OpenComputers")
})
public class TileEntitySpeaker extends TileEntity implements Environment, ITickable {
	
	protected Node node;
		
	@Optional.Method(modid = "OpenComputers")
	@Override
	public Node node() {
		if (node == null) node = Network.newNode(this, Visibility.Network).create();
		return node;
	}
	
	@Optional.Method(modid = "OpenComputers")
	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if (node != null)
			node.remove();
	}

	@Optional.Method(modid = "OpenComputers")
	@Override
	public void invalidate() {
		super.invalidate();
		if (node != null)
			node.remove();
	}

	@Optional.Method(modid = "OpenComputers")
	@Override
	public void onConnect(final Node node) {
		// TODO Auto-generated method stub
	}

	@Optional.Method(modid = "OpenComputers")
	@Override
	public void onMessage(Message message) {
		// TODO Auto-generated method stub
	}

	@Optional.Method(modid = "OpenComputers")
	@Override
	public void onDisconnect(Node node) {
		// TODO Auto-generated method stub

	}

	@Optional.Method(modid = "OpenComputers")
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if (node != null && node.host() == this) {
			node.load(nbt.getCompoundTag("oc:node"));
		}
	}

	@Optional.Method(modid = "OpenComputers")
	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		if (node != null && node.host() == this) {
			final NBTTagCompound nodeNbt = new NBTTagCompound();
			node.save(nodeNbt);
			nbt.setTag("oc:node", nodeNbt);
		}
	}

	@Optional.Method(modid = "OpenComputers")
	public void updateEntity() {
		if (node != null && node.network() == null) {
			Network.joinOrCreateNetwork(this);
		}
	}

	@Override
	public void update() {
		if (node != null && node.network() == null) {
			Network.joinOrCreateNetwork(this);
		}
	}
}
