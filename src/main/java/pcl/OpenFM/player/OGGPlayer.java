package pcl.OpenFM.player;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import pcl.OpenFM.OpenFM;
import pcl.OpenFM.network.PacketHandler;
import pcl.OpenFM.network.Message.MessageTERadioBlock;
import static javax.sound.sampled.AudioSystem.getAudioInputStream;
import static javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;

public class OGGPlayer {

	private IntBuffer buffer;
	private IntBuffer source;
	private float volume = 0.0F;
	private int posX;
	private int posY;
	private int posZ;
	private World world;
	private String streamURL;
	private boolean playing = false;

	public OGGPlayer() {
	}

	private boolean alError() {
		if (AL10.alGetError() != AL10.AL_NO_ERROR) {
			OpenFM.logger.error(String.format("AL10 Error: %d: %s", AL10.alGetError(), AL10.alGetString(AL10.alGetError())));
			return true;
		}
		return false;
	}

	public void play(String streamURL) throws IOException {
		this.streamURL = streamURL;
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder()
		.url(streamURL)
		.build();

		Response response = client.newCall(request).execute();
		BufferedInputStream bis = new BufferedInputStream(response.body().byteStream());
		try (final AudioInputStream in = getAudioInputStream(bis)) {

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
			stream(getAudioInputStream(outFormat, in));

			if (this.playing) {
				while (AL10.alGetSourcei(this.source.get(0), AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {}
				}
			}

			close();
		} catch (UnsupportedAudioFileException | IOException e) {
			PacketHandler.INSTANCE.sendToServer(new MessageTERadioBlock(this.posX, this.posY, this.posZ, this.world, this.streamURL, false, 0.1F, 1));
			FMLClientHandler.instance().getClient().thePlayer.addChatMessage(new ChatComponentTranslation("msg.OpenFM.invalid_link", new Object[0]));
			OpenFM.logger.error(e);
			throw new IllegalStateException(e);
		}
	}

	private AudioFormat getOutFormat(AudioFormat inFormat) {
		final int ch = inFormat.getChannels();
		final float rate = inFormat.getSampleRate();
		return new AudioFormat(PCM_SIGNED, rate, 16, ch, ch * 2, rate, false);
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
				AL10.alSourceQueueBuffers(this.source.get(0), buffer);

				int state = AL10.alGetSourcei(this.source.get(0), AL10.AL_SOURCE_STATE);
				if(this.playing && state != AL10.AL_PLAYING) {
					AL10.alSourcePlay(this.source.get(0));
				}
			}
		}
	}

	private void close() {
		this.playing = false;
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

	public void setID(World world, int x, int y, int z) {
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.world = world;
	}

	public boolean isPlaying() {
		return this.playing;
	}

}