package pcl.OpenFM.player;

import java.io.InputStream;
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

public class PlayerDispatcher extends PlaybackListener implements Runnable {
	private String streamURL;
	public AdvancedPlayer mp3Player;
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
				 
				this.mp3Player = new AdvancedPlayer(stream);
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
			//PacketHandler.INSTANCE.sendToServer(new MessageTERadioBlock(this.x, this.y, this.z, this.world, this.streamURL, !isPlaying(), 0.1F, 1));
			//FMLClientHandler.instance().getClient().thePlayer.addChatMessage(new ChatComponentTranslation("msg.OpenFM.invalid_link", new Object[0]));
			//OpenFM.logger.error(e);
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

	public void playbackStarted(PlayBackEvent evt) {}

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