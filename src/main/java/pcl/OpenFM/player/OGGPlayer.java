package pcl.OpenFM.player;

import static javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;
import static javax.sound.sampled.AudioSystem.getAudioInputStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import pcl.OpenFM.OpenFM;
import pcl.OpenFM.network.PacketHandler;
import pcl.OpenFM.network.message.MessageRadioPlaying;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class OGGPlayer {

	private final Deque<SourceDataLine> linesPlaying = new ConcurrentLinkedDeque<>();
	private float volume = 0.0F;
	private int posX;
	private int posY;
	private int posZ;
	private World world;
	private String streamURL;
	private SourceDataLine line;
	public OGGPlayer() {
	}

	public void play(String streamURL) throws IOException {
		this.streamURL = streamURL;
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder()
		.url(streamURL)
		.build();
		
		Response response = client.newCall(request).execute();
		InputStream stream = response.body().byteStream();
		
		BufferedInputStream bis = new BufferedInputStream(response.body().byteStream());		 
		try (final AudioInputStream in = getAudioInputStream(bis)) {

			final AudioFormat outFormat = getOutFormat(in.getFormat());
			final Info info = new Info(SourceDataLine.class, outFormat);

			try (final SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
				this.line = line;
				if (line != null) {
					line.open(outFormat);
					linesPlaying.add(line);
					line.start();
					stream(getAudioInputStream(outFormat, in), line);
					line.drain();
					line.stop();
					linesPlaying.remove(line);
				}
			}

		} catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
			PacketHandler.INSTANCE.sendToServer(new MessageRadioPlaying(this.posX, this.posY, this.posZ, false).wrap());
			FMLClientHandler.instance().getClient().thePlayer.addChatMessage(new TextComponentString(I18n.translateToLocal("msg.OpenFM.invalid_link")));
			OpenFM.logger.error(e);
			throw new IllegalStateException(e);
		}
	}

	private AudioFormat getOutFormat(AudioFormat inFormat) {
		final int ch = inFormat.getChannels();
		final float rate = inFormat.getSampleRate();
		return new AudioFormat(PCM_SIGNED, rate, 16, ch, ch * 2, rate, false);
	}

	private void stream(AudioInputStream in, SourceDataLine line) throws IOException {
		final byte[] buffer = new byte[65536];
		for (int n = 0; n != -1; n = in.read(buffer, 0, buffer.length)) {
			line.write(buffer, 0, n);
		}
	}

	public void stop() {
		while (linesPlaying.peek() != null) {
			try (final SourceDataLine line = linesPlaying.pop()) {
				line.stop();
				linesPlaying.clear();
			}
		}
	}
	
	public void setVolume(float f) {
		this.volume = f;
        if (line != null && line.isOpen()) {
            try {
                    FloatControl volumeControl = (FloatControl)line.getControl(FloatControl.Type.MASTER_GAIN);
                    if (this.volume == 0) {
                            volumeControl.setValue(volumeControl.getMinimum());
                    } else {                                        
                            float minimum = volumeControl.getMinimum();
                            float maximum = volumeControl.getMaximum();
    
                            double db = Math.log10(this.volume) * 20; //Map linear volume to logarithmic dB scale
                            
                            volumeControl.setValue(Math.max(minimum, Math.min(maximum, (float)db)));
                    }
            } catch (IllegalArgumentException iae) {
                    throw new RuntimeException(iae);
            }
    }
	}

	public float getVolume() {
		return this.volume;
	}

	public void setID(World world, int x, int y, int z) {
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.world = world;
	}

	public boolean isPlaying() {
		// TODO Auto-generated method stub
		return !this.linesPlaying.isEmpty();
	}

}