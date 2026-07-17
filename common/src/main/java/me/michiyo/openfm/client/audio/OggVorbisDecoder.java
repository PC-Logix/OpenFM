package me.michiyo.openfm.client.audio;

import com.jcraft.jogg.*;
import com.jcraft.jorbis.*;

import java.io.InputStream;
import java.util.Arrays;

public final class OggVorbisDecoder implements PcmDecoder {
    private final InputStream in;

    private final SyncState oy = new SyncState();
    private final StreamState os = new StreamState();
    private final Page og = new Page();
    private final Packet op = new Packet();

    private final Info vi = new Info();
    private final Comment vc = new Comment();
    private final DspState vd = new DspState();
    private final Block vb = new Block(vd);

    private boolean headersReady = false;
    private boolean eos = false;

    private int sampleRate = 44100;
    private int channels = 2;

    // Vorbis decode buffer
    private final float[][][] _pcm = new float[1][][];
    private final int[] _index = new int[32];

    public OggVorbisDecoder(InputStream in) throws Exception {
        this.in = in;
        oy.init();
        initHeaders();
    }

    @Override
    public byte[] readPcmChunk(int requestedBytes) throws Exception {
        if (eos) return null;

        byte[] out = new byte[requestedBytes];
        int written = 0;

        while (written < requestedBytes) {
            // First: try to pull PCM from vorbis DSP
            int samples;
            while ((samples = vd.synthesis_pcmout(_pcm, _index)) > 0) {
                int bout = Math.min(samples, 1024);

                // How many bytes can we write this iteration?
                // 16-bit = 2 bytes per sample per channel
                int bytesThis = bout * channels * 2;
                if (written + bytesThis > requestedBytes) {
                    // trim bout to fit
                    int maxBout = (requestedBytes - written) / (channels * 2);
                    if (maxBout <= 0) {
                        return out; // buffer full
                    }
                    bout = maxBout;
                    bytesThis = bout * channels * 2;
                }

                float[][] pcm = _pcm[0];
                int ptr = written;

                for (int i = 0; i < bout; i++) {
                    for (int c = 0; c < channels; c++) {
                        int val = (int) (pcm[c][_index[c] + i] * 32767.0f);
                        if (val > 32767) val = 32767;
                        if (val < -32768) val = -32768;

                        out[ptr++] = (byte) (val & 0xFF);
                        out[ptr++] = (byte) ((val >>> 8) & 0xFF);
                    }
                }

                written += bytesThis;
                vd.synthesis_read(bout);

                if (written >= requestedBytes) return out;
            }

            // No PCM available: we need more packets/pages
            if (!pumpPackets()) {
                // no more data; end of stream
                eos = true;
                return (written == 0) ? null : Arrays.copyOf(out, written);
            }
        }

        return out;
    }

    @Override
    public int getSampleRate() {
        return sampleRate;
    }

    @Override
    public int getChannels() {
        return channels;
    }

    @Override
    public void close() {
        try { os.clear(); } catch (Throwable ignored) {}
        try { vb.clear(); } catch (Throwable ignored) {}
        try { vd.clear(); } catch (Throwable ignored) {}
        try { vi.clear(); } catch (Throwable ignored) {}
        try { oy.clear(); } catch (Throwable ignored) {}
    }

    private void initHeaders() throws Exception {
        vi.init();
        vc.init();

        // Read first page, init stream
        Page firstPage = new Page();
        Packet packet = new Packet();

        while (true) {
            if (oy.pageout(firstPage) != 1) {
                if (!bufferMore()) throw new IllegalStateException("EOF before Ogg header");
                continue;
            }
            os.init(firstPage.serialno());
            os.pagein(firstPage);
            if (os.packetout(packet) != 1) throw new IllegalStateException("Missing initial Vorbis packet");
            break;
        }

        if (vi.synthesis_headerin(vc, packet) < 0) {
            throw new IllegalStateException("Not a Vorbis stream");
        }

        // Need 2 more header packets
        int headers = 0;
        while (headers < 2) {
            int result = os.packetout(packet);
            if (result == 1) {
                vi.synthesis_headerin(vc, packet);
                headers++;
            } else {
                // Need more pages
                if (oy.pageout(firstPage) == 1) {
                    os.pagein(firstPage);
                } else {
                    if (!bufferMore()) throw new IllegalStateException("EOF in Vorbis headers");
                }
            }
        }

        // Init decoder
        vd.synthesis_init(vi);
        vb.init(vd);

        this.sampleRate = vi.rate;
        this.channels = vi.channels;
        this.headersReady = true;
    }

    private boolean pumpPackets() throws Exception {
        if (!headersReady) return false;

        while (true) {
            int result = os.packetout(op);
            if (result == 1) {
                if (vb.synthesis(op) == 0) {
                    vd.synthesis_blockin(vb);
                }
                return true;
            }

            // Need another page
            int pageResult = oy.pageout(og);
            if (pageResult == 1) {
                os.pagein(og);
                if (og.eos() != 0) {
                    // mark eos but still allow final packets to drain
                    eos = true;
                }
                continue;
            }

            // Need more bytes
            if (!bufferMore()) return false;
        }
    }

    private boolean bufferMore() throws Exception {
        int index = oy.buffer(4096);
        byte[] buffer = oy.data;
        int bytes = in.read(buffer, index, 4096);
        if (bytes <= 0) {
            oy.wrote(0);
            return false;
        }
        oy.wrote(bytes);
        return true;
    }
}
