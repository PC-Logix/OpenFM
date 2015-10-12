 package pcl.OpenFM.network.Message;
 
 import io.netty.buffer.ByteBuf;
import net.minecraft.world.World;
import pcl.OpenFM.TileEntity.TileEntityRadio;
import cpw.mods.fml.common.network.simpleimpl.IMessage;

 public class MessageTERadioBlock implements IMessage {
   public World world;
   public float volume;
   public boolean isPlaying;
   public String streamURL;
   public double tz = 0.0D; public double ty = 0.0D; public double tx = 0.0D;
   
   public int dim;
   
   public int mode;
   
   public double z;
   
   public double y;
   
   public double x;
   
   public MessageTERadioBlock() {}
   
   public MessageTERadioBlock(TileEntityRadio radio) {
     this.x = radio.xCoord;
     this.y = radio.yCoord;
     this.z = radio.zCoord;
     this.world = radio.getWorldObj();
     this.dim = this.world.provider.dimensionId;
     this.streamURL = radio.getStreamURL();
     this.isPlaying = radio.isPlaying();
     this.volume = radio.getVolume();
     int mode = 13;
     if (radio.isListeningToRedstoneInput())
       mode = 14;
     this.mode = mode;
   }
 
   public MessageTERadioBlock(double x, double y, double z, World world, String streamURL, boolean isPlaying, float volume, int mode) {
     this.x = x;
     this.y = y;
     this.z = z;
     this.world = world;
     this.dim = world.provider.dimensionId;
     this.streamURL = streamURL;
     this.isPlaying = isPlaying;
     this.volume = volume;
     this.mode = mode;
   }
   
   public MessageTERadioBlock(double x, double y, double z, World world, String streamURL, boolean isPlaying, float volume, int mode, double tx, double ty, double tz) {
     this.x = x;
     this.y = y;
     this.z = z;
     this.world = world;
     this.dim = world.provider.dimensionId;
     this.streamURL = streamURL;
     this.isPlaying = isPlaying;
     this.volume = volume;
     this.mode = mode;
     this.tx = tx;
     this.ty = ty;
     this.tz = tz;
   }

   public void fromBytes(ByteBuf buf) {
     this.mode = buf.readInt();
     this.x = buf.readDouble();
     this.y = buf.readDouble();
     this.z = buf.readDouble();
     this.dim = buf.readInt();
     int streamURLlenght = buf.readInt();
     this.streamURL = new String(buf.readBytes(streamURLlenght).array());
     
     this.isPlaying = buf.readBoolean();
     this.volume = buf.readFloat();
     this.tx = buf.readDouble();
     this.ty = buf.readDouble();
     this.tz = buf.readDouble();
   }

   public void toBytes(ByteBuf buf) {
     buf.writeInt(this.mode);
     buf.writeDouble(this.x);
     buf.writeDouble(this.y);
     buf.writeDouble(this.z);
     buf.writeInt(this.dim);
     buf.writeInt(this.streamURL.length());
     buf.writeBytes(this.streamURL.getBytes());
     buf.writeBoolean(this.isPlaying);
     buf.writeFloat(this.volume);
     buf.writeDouble(this.tx);
     buf.writeDouble(this.ty);
     buf.writeDouble(this.tz);
   }
 }