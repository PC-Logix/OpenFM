package pcl.OpenFM.player;

import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.decoder.SampleBuffer;
import javazoom.jl.player.advanced.PlaybackEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pcl.OpenFM.ContentRegistry;
import pcl.OpenFM.OpenFM;

public class MP3Player {
	private Bitstream bitstream;
	private Decoder decoder;
	private IntBuffer buffer;
	private IntBuffer source;
	private PlaybackListener listener;
	private float volume = 0.0F;
	private int posX;
	private int posY;
	private int posZ;
	private World world;
	private String streamURL;
	public InputStream ourStream = null;
	private boolean playing = false;

	public MP3Player() throws JavaLayerException {

	}

	public void setID(World w, int x, int y, int z) {
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.world = w;
	}

	private boolean alError() {
		if (AL10.alGetError() != AL10.AL_NO_ERROR) {
			OpenFM.logger.error(String.format("AL10 Error: %d: %s", AL10.alGetError(), AL10.alGetString(AL10.alGetError())));
			return true;
		}
		return false;
	}

	public boolean play(String streamURL) throws JavaLayerException {
		this.streamURL = streamURL;
		return play(Integer.MAX_VALUE);
	}

	public boolean play(int frames) throws JavaLayerException {
		InputStream stream = null;
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder().url(this.streamURL).build();
		Response response;
		try {
			response = client.newCall(request).execute();
			stream = response.body().byteStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ourStream = stream;
		this.bitstream = new Bitstream(stream);
		this.decoder = new Decoder();
		boolean ret = true;

		this.source = BufferUtils.createIntBuffer(1);
		AL10.alGenSources(this.source);
		if (alError()) {
			close();
			return false;
		}

		AL10.alSourcei(this.source.get(0), AL10.AL_LOOPING, AL10.AL_FALSE);
		AL10.alSourcef(this.source.get(0), AL10.AL_PITCH, 1.0f);
		AL10.alSourcef(this.source.get(0), AL10.AL_GAIN, this.volume * Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.RECORDS));
		if (alError()) {
			close();
			return false;
		}

		this.playing = true;
		if (this.listener != null)
		{
			this.listener.playbackStarted(createEvent(PlaybackEvent.STARTED));
		}

		while ((this.playing) && (ret))
		{
			ret = decodeFrame();
		}

		if (this.playing) {
			while (AL10.alGetSourcei(this.source.get(0), AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {}
			}
		}

		close();

		return ret;
	}

	private void close() {
		this.playing = false;
		if (this.listener != null)
		{
			this.listener.playbackFinished(createEvent(PlaybackEvent.STOPPED));
		}
		if (this.source != null) {
			AL10.alSourceStop(this.source);
			AL10.alDeleteSources(this.source);
			this.source = null;
		}
		if (this.buffer != null) {
			AL10.alDeleteBuffers(this.buffer);
			this.buffer = null;
		}
		try
		{
			this.bitstream.close();
		}
		catch (BitstreamException localBitstreamException) {}
	}

	protected boolean decodeFrame() throws JavaLayerException {
		try
		{
			Header h = this.bitstream.readFrame();
			if (h == null)
			{
				return false;
			}

			SampleBuffer output = (SampleBuffer)this.decoder.decodeFrame(h, this.bitstream);
			short[] samples = output.getBuffer();

			if (this.buffer == null) {
				this.buffer = BufferUtils.createIntBuffer(1);
			} else {
				int processed = AL10.alGetSourcei(this.source.get(0), AL10.AL_BUFFERS_PROCESSED);
				if (processed > 0) {
					AL10.alSourceUnqueueBuffers(this.source.get(0), this.buffer);
				}
			}

			AL10.alGenBuffers(this.buffer);
			ShortBuffer data = (ShortBuffer) BufferUtils.createShortBuffer(output.getBufferLength()).put(samples, 0, output.getBufferLength()).flip();
			AL10.alBufferData(this.buffer.get(0), (output.getChannelCount() > 1) ? AL10.AL_FORMAT_STEREO16 : AL10.AL_FORMAT_MONO16, data, output.getSampleFrequency());
			AL10.alSourceQueueBuffers(this.source.get(0), buffer);

			int state = AL10.alGetSourcei(this.source.get(0), AL10.AL_SOURCE_STATE);
			if (this.playing && state != AL10.AL_PLAYING) {
				AL10.alSourcePlay(this.source.get(0));
			}

			this.bitstream.closeFrame();
		}
		catch (RuntimeException ex)
		{
			close();
			//play();
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

	private PlayBackEvent createEvent(int id) {
		return new PlayBackEvent(this, id, 0);
	}

	public void setPlayBackListener(PlaybackListener listener) {
		this.listener = listener;
	}

	public PlaybackListener getPlayBackListener() {
		return this.listener;
	}

	public void stop() {
		this.playing = false;
		if (this.source != null) {
			AL10.alSourcef(this.source.get(0), AL10.AL_GAIN, 0.0f);
			AL10.alSourceStop(this.source);
		}
	}

	public void setVolume(float f) {
		this.volume = f;
		if (this.playing && this.source != null) {
			AL10.alSourcef(this.source.get(0), AL10.AL_GAIN, f * Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.RECORDS));
		}
	}

	public float getVolume() {
		return this.volume;
	}
}