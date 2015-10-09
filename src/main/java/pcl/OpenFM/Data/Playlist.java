 package pcl.OpenFM.Data;
 
 import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 
 
 
 
 public class Playlist
 {
   public ArrayList<String> playlist = new ArrayList();
   public int currentStream;
   private static String[] configTemplate = new String[2];
   private FMLPreInitializationEvent configEvent;
   
   public Playlist(FMLPreInitializationEvent event) {
     configHandler(event);
   }
   
   public void configHandler(FMLPreInitializationEvent event) {
     this.configEvent = event;
     Configuration config = new Configuration(new File(event.getModConfigurationDirectory() + "/openfm/playlist.cfg"));
     
     config.load();
     Integer counter = Integer.valueOf(0);
     
     ConfigCategory category = config.getCategory("general");
     category.setComment("Example: streamUrl[number]:your_stream_url, streamName[number]: your_stream_name");
     Iterator streams = category.getChildren().iterator();
     Integer localInteger1; for (int i = 0; i < category.size(); i++) {
       Property streamUrl = config.get("general", "streamUrl" + counter.toString(), "");
       if (!streamUrl.getString().isEmpty()) {
         this.playlist.add(streamUrl.getString());
       }
       localInteger1 = counter;Integer localInteger2 = counter = Integer.valueOf(counter.intValue() + 1);
     }
     config.save();
   }
   
   public void add(String URL) {
     Configuration config = new Configuration(new File(this.configEvent.getModConfigurationDirectory() + "/openfm/playlist.cfg"));
     this.playlist.add(URL);
     config.load();
     ConfigCategory category = config.getCategory("general");
     int size = category.size();
     Property streamUrl = config.get("general", "streamUrl" + size, URL);
     config.save();
   }
   
   public void remove(String URL) {
     Configuration config = new Configuration(new File(this.configEvent.getModConfigurationDirectory() + "/openfm/playlist.cfg"));
     ConfigCategory category = config.getCategory("general");
     config.load();
     Integer counter = Integer.valueOf(0);
     config.removeCategory(category);
     this.playlist.remove(URL);
     for (int i = 0; i < this.playlist.size(); i++) {
       if (!((String)this.playlist.get(i)).equals(URL)) {
         Property localProperty = config.get("general", "streamUrl" + i, (String)this.playlist.get(i));
       }
     }
     config.save();
   }
 }


