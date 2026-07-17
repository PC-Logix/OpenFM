package me.michiyo.openfm.client.audio;

import net.sourceforge.jaad.SampleBuffer;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.adts.ADTSDemultiplexer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public final class AacDecoder implements PcmDecoder {
    private final InputStream in;
    private final ADTSDemultiplexer demultiplexer;
    private final Decoder decoder;
    private final SampleBuffer sampleBuffer = new SampleBuffer();

    private int sampleRate;
    private int channels;
    private byte[] pending = new byte[0];
    private int pendingOffset;

    public AacDecoder(InputStream in) throws IOException {
        this.in = in;
        this.demultiplexer = new ADTSDemultiplexer(in);
        this.decoder = Decoder.create(demultiplexer.getDecoderInfo());
        this.sampleBuffer.setBigEndian(false);
        this.sampleRate = demultiplexer.getSampleFrequency();
        this.channels = demultiplexer.getChannelCount();
    }

    @Override
    public byte[] readPcmChunk(int requestedBytes) throws Exception {
        byte[] out = new byte[requestedBytes];
        int written = 0;

        while (written < requestedBytes) {
            if (pendingOffset < pending.length) {
                int count = Math.min(pending.length - pendingOffset, requestedBytes - written);
                System.arraycopy(pending, pendingOffset, out, written, count);
                pendingOffset += count;
                written += count;
                continue;
            }

            byte[] frame = demultiplexer.readNextFrame();
            if (frame == null) {
                return written == 0 ? null : Arrays.copyOf(out, written);
            }

            decoder.decodeFrame(frame, sampleBuffer);
            sampleRate = sampleBuffer.getSampleRate();
            channels = sampleBuffer.getChannels();
            pending = sampleBuffer.getData();
            pendingOffset = 0;
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
        try { in.close(); } catch (IOException ignored) {}
    }
}
