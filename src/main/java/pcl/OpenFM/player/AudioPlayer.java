package pcl.OpenFM.player;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;

import javazoom.jl.player.advanced.PlaybackEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.world.World;
import pcl.OpenFM.OpenFM;

public class AudioPlayer {

	protected IntBuffer buffer;
	protected IntBuffer source;
	protected float volume = 0.0F;
	protected int posX;
	protected int posY;
	protected int posZ;
	protected World world;
	protected boolean playing = false;
	protected InputStream stream;
	private PlaybackListener listener;

	public AudioPlayer(InputStream stream) {
		this.stream = stream;
	}

	protected boolean alError() {
		if (AL10.alGetError() != AL10.AL_NO_ERROR) {
			OpenFM.logger.error(String.format("AL10 Error: %d: %s", AL10.alGetError(), AL10.alGetString(AL10.alGetError())));
			return true;
		}
		return false;
	}

	public void setID(World w, int x, int y, int z) {
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.world = w;
	}

	public void setPlayBackListener(PlaybackListener listener) {
		this.listener = listener;
	}

	public PlaybackListener getPlayBackListener() {
		return this.listener;
	}

	private AudioFormat getOutFormat(AudioFormat inFormat) {
		final int ch = inFormat.getChannels();
		final float rate = inFormat.getSampleRate();
		return new AudioFormat(Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
	}

	private PlayBackEvent createEvent(int id) {
		return new PlayBackEvent(this, id, 0);
	}

	public void play() throws UnsupportedAudioFileException, IOException {
		AudioInputStream in = (AudioInputStream) this.stream;

		final AudioFormat outFormat = getOutFormat(in.getFormat());

		this.source = BufferUtils.createIntBuffer(1);

		AL10.alGenSources(this.source);
		if (alError()) {
			close();
			return;
		}

		AL10.alSourcei(this.source.get(0), AL10.AL_LOOPING, AL10.AL_FALSE);
		AL10.alSourcef(this.source.get(0), AL10.AL_PITCH, 1.0f);
		AL10.alSourcef(this.source.get(0), AL10.AL_GAIN, this.volume * Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.RECORDS));

		if (alError()) {
			close();
			return;
		}

		this.playing = true;
		if (this.listener != null)
		{
			this.listener.playbackStarted(createEvent(PlaybackEvent.STARTED));
		}
		stream(AudioSystem.getAudioInputStream(outFormat, in));

		if (this.playing) {
			while (AL10.alGetSourcei(this.source.get(0), AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {}
			}
		}

		close();
	}

	private void stream(AudioInputStream in) throws IOException {
		AudioFormat format = in.getFormat();

		byte[] databuffer = new byte[65536];
		for (int n = 0; this.playing && n != -1; n = in.read(databuffer, 0, databuffer.length)) {
			if (n > 0) {
				if (this.buffer == null) {
					this.buffer = BufferUtils.createIntBuffer(1);
				} else {
					int processed = AL10.alGetSourcei(this.source.get(0), AL10.AL_BUFFERS_PROCESSED);
					if (processed > 0) {
						AL10.alSourceUnqueueBuffers(this.source.get(0), this.buffer);
						alError();
					}
				}

				AL10.alGenBuffers(this.buffer);
				ByteBuffer data = (ByteBuffer) BufferUtils.createByteBuffer(n).order(ByteOrder.LITTLE_ENDIAN).put(databuffer, 0, n).flip();
				AL10.alBufferData(this.buffer.get(0), (format.getChannels() > 1) ? AL10.AL_FORMAT_STEREO16 : AL10.AL_FORMAT_MONO16, data, (int)format.getSampleRate());
				alError();
				AL10.alSourceQueueBuffers(this.source.get(0), buffer);

				int state = AL10.alGetSourcei(this.source.get(0), AL10.AL_SOURCE_STATE);
				if(this.playing && state != AL10.AL_PLAYING) {
					AL10.alSourcePlay(this.source.get(0));
				}
			}
		}
	}

	protected void close() {
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

	public boolean isPlaying() {
		return this.playing;
	}
}
