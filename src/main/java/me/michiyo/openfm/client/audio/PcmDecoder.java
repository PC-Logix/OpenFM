package me.michiyo.openfm.client.audio;

public interface PcmDecoder extends AutoCloseable {
    /**
     * Read up to requestedBytes of interleaved signed 16-bit little-endian PCM.
     * Returns null on EOF.
     */
    byte[] readPcmChunk(int requestedBytes) throws Exception;

    int getSampleRate();
    int getChannels();

    @Override
    void close();
}
