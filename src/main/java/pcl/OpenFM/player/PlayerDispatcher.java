package pcl.OpenFM.player;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import cpw.mods.fml.client.FMLClientHandler;
import pcl.OpenFM.OpenFM;
import pcl.OpenFM.TileEntity.TileEntityRadio;
import pcl.OpenFM.network.PacketHandler;
import pcl.OpenFM.network.message.MessageRadioPlaying;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

public class PlayerDispatcher extends PlaybackListener implements Runnable {
	private String streamURL;
	private AudioPlayer player;
	private TileEntityRadio radio;
	private Thread pThread;
	private int x;
	private int y;
	private int z;
	private World world;

	public PlayerDispatcher(TileEntityRadio radio, String streamURL, World w, int x, int y, int z)
	{
		try
		{
			this.world = w;
			this.radio = radio;
			this.x = x;
			this.y = y;
			this.z = z;
			this.streamURL = streamURL;
			this.pThread = new Thread(this);
			this.pThread.start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void run()
	{
		OkHttpClient client = new OkHttpClient();
		Request request = null;
		try {
			request = new Request.Builder().url(streamURL)
				//.addHeader("Icy-MetaData", "1")
				.build();
		} catch (IllegalArgumentException e1) {
			streamURL = null;
			OpenFM.logger.warn(e1);
			return;
		}
		Response response = null;
		AudioInputStream audioStream = null;
		try {
			response = client.newCall(request).execute();
		} catch (IOException e1) {
			streamURL = null;
			OpenFM.logger.warn(e1);
			return;
		}
		OpenFM.logger.info("Content-Type: " + response.header("Content-Type", "unknown"));
		InputStream bis = null;
		try {
			bis = new MarkErrorInputStream(new BufferedInputStream(response.body().byteStream()));
			audioStream = AudioSystem.getAudioInputStream(bis);
		} catch (IOException | UnsupportedAudioFileException e1) {
			streamURL = null;
			OpenFM.logger.warn(e1);
			return;
		}
		OpenFM.logger.info("Starting Stream: " + streamURL + " at X:" + x + " Y:" + y + " Z:" + z);
		try
		{
			this.player = new AudioPlayer(audioStream);
			this.player.setID(this.world, this.x, this.y, this.z);
			this.player.setPlayBackListener(this);
			this.player.play();
		}
		catch (Exception e)
		{
			if (this.player != null) {
				this.player.close();
			}
			PacketHandler.INSTANCE.sendToServer(new MessageRadioPlaying(this.x, this.y, this.z, false).wrap());
			FMLClientHandler.instance().getClient().thePlayer.addChatMessage(new ChatComponentTranslation("msg.OpenFM.invalid_link", new Object[0]));
			OpenFM.logger.error(e);
			e.printStackTrace();
		}
	}

	public void stop()
	{
		if ((this.player != null) && (isPlaying()))
		{
			this.player.stop();
		}
	}

	@Override
	public void playbackStarted(PlayBackEvent evt) {}

	@Override
	public void playbackFinished(PlayBackEvent evt) {}

	public boolean isPlaying()
	{
		if (this.player != null)
			return this.player.isPlaying();
		else
			return this.pThread.isAlive();
	}

	public void setVolume(float f)
	{
		if (this.player != null)
			this.player.setVolume(f);
	}

	public float getVolume()
	{
		if (this.player != null)
			return this.player.getVolume() / Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.RECORDS);
		else
			return 0;
	}
}