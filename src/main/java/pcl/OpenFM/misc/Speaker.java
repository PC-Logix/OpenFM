 package pcl.OpenFM.misc;
 
 import net.minecraft.world.World;
 
 public class Speaker {
   public int x;
   public int y;
   public int z;
   public World world;
   
   public Speaker(int x, int y, int z, World w) {
     this.x = x;
     this.y = y;
     this.z = z;
     this.world = w;
   }
 }


