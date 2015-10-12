package pcl.OpenFM.player;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.UnsupportedAudioFileException;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.decoder.SampleBuffer;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;
import javazoom.jl.player.advanced.PlaybackEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.world.World;
import pcl.OpenFM.ContentRegistry;

public class DAdvancedPlayer {
	private Bitstream bitstream;
	private Decoder decoder;
	private AudioDevice audio;
	private DPlaybackListener listener;
	private float volume = 0.0F;
	private int posX;
	private int posY;
	private int posZ;
	private World world;
	public InputStream ourStream = null;
	public MpegInfo tagData = new MpegInfo();
	public DAdvancedPlayer(InputStream stream) throws JavaLayerException {
		this(stream, null);
	}

	public void setID(World w, int x, int y, int z) {
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.world = w;
	}

	public DAdvancedPlayer(InputStream stream, AudioDevice device) throws JavaLayerException {
		ourStream = stream;
		this.bitstream = new Bitstream(stream);
		if (device != null)
		{
			this.audio = device;
		}
		else
		{
			this.audio = FactoryRegistry.systemRegistry().createAudioDevice();
		}

		this.audio.open(this.decoder = new Decoder());
	}

	public void play() throws JavaLayerException {
		play(Integer.MAX_VALUE);
	}

	public boolean play(int frames) throws JavaLayerException {
		boolean ret = true;


		if (this.listener != null)
		{
			this.listener.playbackStarted(createEvent(PlaybackEvent.STARTED));
		}

		while ((frames-- > 0) && (ret))
		{
			if (!ContentRegistry.checkBlock(this.world, this.posX, this.posY, this.posZ)) {
				close();
			}
			ret = decodeFrame();
		}

		AudioDevice out = this.audio;

		if (out != null)
		{
			out.flush();
			synchronized (this)
			{
				close();
			}
			if (this.listener != null)
			{
				this.listener.playbackFinished(createEvent(out, PlaybackEvent.STOPPED));
			}
		}
		return ret;
	}

	public synchronized void close() {
		AudioDevice out = this.audio;

		if (out != null)
		{
			this.audio = null;
			out.close();
			out.getPosition();
			try
			{
				this.bitstream.close();
			}
			catch (BitstreamException localBitstreamException) {}
		}
	}

	protected boolean decodeFrame() throws JavaLayerException {
		try
		{
			AudioDevice out = this.audio;

			if (out == null)
			{
				return false;
			}

			Header h = this.bitstream.readFrame();

			if (h == null)
			{
				return false;
			}


			SampleBuffer output = (SampleBuffer)this.decoder.decodeFrame(h, this.bitstream);

			synchronized (this)
			{
				out = this.audio;
				
				if (out != null)
				{
					short[] samples = output.getBuffer();

					for (int samp = 0; samp < samples.length; samp++)
					{
						samples[samp] = ((short)(int)(samples[samp] * this.volume * (2 * (Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.RECORDS) * Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.MASTER)))));
					}					
					out.write(samples, 0, output.getBufferLength());
				}
			}

			this.bitstream.closeFrame();
		}
		catch (RuntimeException ex)
		{
			stop();
			play();
			throw new JavaLayerException("Exception decoding audio frame", ex);
		}

		return true;
	}

	protected boolean skipFrame() throws JavaLayerException {
		Header h = this.bitstream.readFrame();
		if (h == null)
		{
			return false;
		}
		this.bitstream.closeFrame();
		return true;
	}

	public boolean play(int start, int end) throws JavaLayerException {
		boolean ret = true;
		int offset = start;

		while ((offset-- > 0) && (ret))
		{
			ret = skipFrame();
		}

		return play(end - start);
	}

	private DPlayBackEvent createEvent(int id) {
		return createEvent(this.audio, id);
	}

	private DPlayBackEvent createEvent(AudioDevice dev, int id) {
		return new DPlayBackEvent(this, id, dev.getPosition());
	}

	public void setPlayBackListener(DPlaybackListener listener) {
		this.listener = listener;
	}

	public DPlaybackListener getPlayBackListener() {
		return this.listener;
	}

	public void stop() {
		this.listener.playbackFinished(createEvent(DPlayBackEvent.STOPPED));
		close();
	}

	public void setVolume(float f) {
		this.volume = f;
	}

	public float getVolume() {
		return this.volume;
	}
}