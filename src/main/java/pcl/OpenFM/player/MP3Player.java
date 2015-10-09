package pcl.OpenFM.player;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import pcl.OpenFM.OpenFM;
import pcl.OpenFM.network.PacketHandler;
import pcl.OpenFM.network.Message.MessageTERadioBlock;
import cpw.mods.fml.client.FMLClientHandler;

public class MP3Player
extends DPlaybackListener
implements Runnable {
	private String streamURL;
	private DAdvancedPlayer player;
	private Thread pThread;
	private int x;
	private int y;
	private int z;
	private World world;
	public MP3Player(String mp3url, World w, int a, int b, int c)
	{
		try
		{
			this.world = w;
			this.x = a;
			this.y = b;
			this.z = c;
			this.streamURL = mp3url;
			this.pThread = new Thread(this);
			this.pThread.start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void run()
	{
		try
		{

			this.player = new DAdvancedPlayer(new URL(this.streamURL).openConnection().getInputStream());
			this.player.setID(this.world, this.x, this.y, this.z);
			this.player.setPlayBackListener(this);
			this.player.play();

		}
		catch (Exception e)
		{
			PacketHandler.INSTANCE.sendToServer(new MessageTERadioBlock(this.x, this.y, this.z, this.world, this.streamURL, !isPlaying(), 0.1F, 1));
			FMLClientHandler.instance().getClient().thePlayer.addChatMessage(new ChatComponentTranslation("msg.invalid_link", new Object[0]));
			OpenFM.logger.error(e);
		}
	}

	public void stop()
	{
		if ((this.player != null) && (isPlaying()))
		{
			this.player.stop();
		}
	}

	public void playbackStarted(DPlayBackEvent evt) {}

	public void playbackFinished(DPlayBackEvent evt) {}

	public boolean isPlaying()
	{
		return this.pThread.isAlive();
	}

	public void setVolume(float f)
	{
		if (this.player != null)
		{

			this.player.setVolume(f);
		}
	}

	public float getVolume()
	{
		System.out.println(Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.RECORDS));
		return this.player.getVolume() / Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.RECORDS);
	}
	
	public String getTrackInfo()
	{
		return this.player.getTrack();
	}
}