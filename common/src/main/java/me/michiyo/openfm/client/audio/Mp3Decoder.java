package me.michiyo.openfm.client.audio;

import javazoom.jl.decoder.*;

import java.io.InputStream;
import java.util.Arrays;

public final class Mp3Decoder implements PcmDecoder {
    private final Bitstream bitstream;
    private final Decoder decoder;

    private int sampleRate = 44100;
    private int channels = 2;

    public Mp3Decoder(InputStream in) {
        this.bitstream = new Bitstream(in);
        this.decoder = new Decoder();
    }

    @Override
    public byte[] readPcmChunk(int requestedBytes) throws Exception {
        byte[] out = new byte[requestedBytes];
        int written = 0;

        while (written < requestedBytes) {
            Header h = bitstream.readFrame();
            if (h == null) {
                return (written == 0) ? null : Arrays.copyOf(out, written);
            }

            try {
                SampleBuffer sb = (SampleBuffer) decoder.decodeFrame(h, bitstream);

                // Update discovered format
                this.sampleRate = sb.getSampleFrequency();
                this.channels = sb.getChannelCount();

                short[] pcm = sb.getBuffer();
                int shorts = sb.getBufferLength(); // number of samples across channels, interleaved
                int bytesAvailable = shorts * 2;

                int bytesToCopy = Math.min(bytesAvailable, requestedBytes - written);
                int shortsToCopy = bytesToCopy / 2;

                for (int i = 0; i < shortsToCopy; i++) {
                    short s = pcm[i];
                    out[written++] = (byte) (s & 0xFF);
                    out[written++] = (byte) ((s >>> 8) & 0xFF);
                }
            } finally {
                bitstream.closeFrame();
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
        try { bitstream.close(); } catch (Throwable ignored) {}
    }
}
