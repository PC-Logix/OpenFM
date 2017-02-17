package pcl.OpenFM.player;

import java.io.InputStream;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import cpw.mods.fml.client.FMLClientHandler;
import pcl.OpenFM.OpenFM;
import pcl.OpenFM.network.PacketHandler;
import pcl.OpenFM.network.message.MessageRadioPlaying;
import pcl.OpenFM.player.MP3Player;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

public class PlayerDispatcher extends PlaybackListener implements Runnable {
	private String streamURL;
	public MP3Player mp3Player;
	public OGGPlayer oggPlayer;
	public String decoder;
	private Thread pThread;
	private int x;
	private int y;
	private int z;
	private World world;
	public PlayerDispatcher(String decoder, String mp3url, World w, int a, int b, int c)
	{
		try
		{
			this.world = w;
			this.decoder = decoder;
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
			if (decoder.equals("mp3")) {
				OkHttpClient client = new OkHttpClient();
				Request request = new Request.Builder()
				.url(streamURL)
				.build();
				
				Response response = client.newCall(request).execute();
				InputStream stream = response.body().byteStream();
				 
				this.mp3Player = new MP3Player(stream);
				this.mp3Player.setID(this.world, this.x, this.y, this.z);
				this.mp3Player.setPlayBackListener(this);
				this.mp3Player.play();
			} else if (decoder.equals("ogg")) {
				this.oggPlayer = new OGGPlayer();
				this.oggPlayer.setID(this.world, this.x, this.y, this.z);
				this.oggPlayer.play(this.streamURL);
			}
		}
		catch (Exception e)
		{
			PacketHandler.INSTANCE.sendToServer(new MessageRadioPlaying(this.x, this.y, this.z, false).wrap());
			FMLClientHandler.instance().getClient().thePlayer.addChatMessage(new ChatComponentTranslation("msg.OpenFM.invalid_link", new Object[0]));
			OpenFM.logger.error(e);
		}
	}

	public void stop()
	{
		if ((this.mp3Player != null || this.oggPlayer != null) && (isPlaying()))
		{
			if (decoder.equals("mp3")) {
				this.mp3Player.stop();
			} else if (decoder.equals("ogg")) {
				this.oggPlayer.stop();
			}
		}
	}

	@Override
	public void playbackStarted(PlayBackEvent evt) {}

	@Override
	public void playbackFinished(PlayBackEvent evt) {}

	public boolean isPlaying()
	{
		if (decoder.equals("mp3")) {
			return this.pThread.isAlive();
		} else if (decoder.equals("ogg")) {
			return this.oggPlayer.isPlaying();
		} else {
			return false;
		}

	}

	public void setVolume(float f)
	{
		if (this.mp3Player != null) {
			this.mp3Player.setVolume(f);
		} else if (this.oggPlayer != null) {
			this.oggPlayer.setVolume(f);
		}
	}

	public float getVolume()
	{
		if (decoder.equals("mp3")) {
			return this.mp3Player.getVolume() / Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.RECORDS);
		} else if (decoder.equals("ogg")) {
			return this.oggPlayer.getVolume() / Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.RECORDS);
		} else {
			return 0;
		}
	}
}