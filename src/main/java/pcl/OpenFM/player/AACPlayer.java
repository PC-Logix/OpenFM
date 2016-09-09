package pcl.OpenFM.player;

import static javax.sound.sampled.AudioSystem.getAudioInputStream;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.adts.ADTSDemultiplexer;
import pcl.OpenFM.OpenFM;
import pcl.OpenFM.network.PacketHandler;
import pcl.OpenFM.network.message.MessageRadioPlaying;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.sourceforge.jaad.Radio;
import net.sourceforge.jaad.adts.ADTSDemultiplexer;

public class AACPlayer {

	private final Deque<SourceDataLine> linesPlaying = new ConcurrentLinkedDeque<>();
	private float volume = 0.0F;
	private int posX;
	private int posY;
	private int posZ;
	private World world;
	private String streamURL;
	private SourceDataLine line;

	public void setID(World world, int x, int y, int z) {
		// TODO Auto-generated method stub
		
	}

	public void play(String streamURL) throws IOException, LineUnavailableException, URISyntaxException {
			this.streamURL = streamURL;
			final SampleBuffer buf = new SampleBuffer();
/*			OkHttpClient client = new OkHttpClient();
			Request request = new Request.Builder()
			.url(streamURL)
			.build();
			
			Response response = client.newCall(request).execute();
			InputStream stream = response.body().byteStream();
			
			BufferedInputStream bis = new BufferedInputStream(response.body().byteStream());	
			try (final AudioInputStream in = getAudioInputStream(bis)) {
				
			} catch (UnsupportedAudioFileException | IOException e) {
				PacketHandler.INSTANCE.sendToServer(new MessageRadioPlaying(this.posX, this.posY, this.posZ, false).wrap());
				FMLClientHandler.instance().getClient().thePlayer.addChatMessage(new TextComponentString(I18n.translateToLocal("msg.OpenFM.invalid_link")));
				OpenFM.logger.error(e);
				throw new IllegalStateException(e);
			}*/
			
			SourceDataLine line = null;
			byte[] b;
			try {
				final URI uri = new URI(this.streamURL);
				final Socket sock = new Socket(uri.getHost(), uri.getPort()>0 ? uri.getPort() : 80);

				//send HTTP request
				final PrintStream out = new PrintStream(sock.getOutputStream());
				String path = uri.getPath();
				if(path==null||path.equals("")) path = "/";
				if(uri.getQuery()!=null) path += "?"+uri.getQuery();
				out.println("GET "+path+" HTTP/1.1");
				out.println("Host: "+uri.getHost());
				out.println();

				//read response (skip header)
				final DataInputStream in = new DataInputStream(sock.getInputStream());
				String x;
				do {
					x = in.readLine();
				}
				while(x!=null&&!x.trim().equals(""));

				final ADTSDemultiplexer adts = new ADTSDemultiplexer(in);
				AudioFormat aufmt = new AudioFormat(adts.getSampleFrequency(), 16, adts.getChannelCount(), true, true);
				final Decoder dec = new Decoder(adts.getDecoderSpecificInfo());

				while(true) {
					b = adts.readNextFrame();
					dec.decodeFrame(b, buf);

					if(line!=null&&formatChanged(line.getFormat(), buf)) {
						//format has changed (e.g. SBR has started)
						line.stop();
						line.close();
						line = null;
						aufmt = new AudioFormat(buf.getSampleRate(), buf.getBitsPerSample(), buf.getChannels(), true, true);
					}
					if(line==null) {
						line = AudioSystem.getSourceDataLine(aufmt);
						line.open();
						line.start();
					}
					b = buf.getData();
					line.write(b, 0, b.length);
				}
			}
			finally {
				if(line!=null) {
					line.stop();
					line.close();
				}
			}
			
	}

	public boolean isPlaying() {
		// TODO Auto-generated method stub
		return line.isActive();
	}

	public void setVolume(float f) {
		// TODO Auto-generated method stub
		
	}

	public float getVolume() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void stop() {
		line.drain();
		line.stop();
		line.close();
	}

	private static boolean formatChanged(AudioFormat af, SampleBuffer buf) {
		return af.getSampleRate()!=buf.getSampleRate()
				||af.getChannels()!=buf.getChannels()
				||af.getSampleSizeInBits()!=buf.getBitsPerSample()
				||af.isBigEndian()!=buf.isBigEndian();
	}
	
}
