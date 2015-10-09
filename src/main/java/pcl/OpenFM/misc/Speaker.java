 package pcl.OpenFM.misc;
 
 import net.minecraft.world.World;
 
 public class Speaker {
   public double x;
   public double y;
   public double z;
   public World world;
   
   public Speaker(double x, double y, double z, World w) { this.x = x;
     this.y = y;
     this.z = z;
     this.world = w;
   }
 }


