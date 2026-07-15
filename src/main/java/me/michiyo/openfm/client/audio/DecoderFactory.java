package me.michiyo.openfm.client.audio;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public final class DecoderFactory {
    private DecoderFactory() {}

    public static class DecoderWithStream {
        public final PcmDecoder decoder;
        public final InputStream stream; // the same stream, wrapped for sniffing

        public DecoderWithStream(PcmDecoder decoder, InputStream stream) {
            this.decoder = decoder;
            this.stream = stream;
        }
    }

    public static DecoderWithStream create(InputStream raw) throws Exception {
        PushbackInputStream in = new PushbackInputStream(raw, 64);

        byte[] head = readSome(in, 64);
        if (head.length > 0) {
            in.unread(head);
        }

        // OGG container signature: "OggS"
        if (startsWith(head, new byte[]{'O','g','g','S'})) {
            return new DecoderWithStream(new OggVorbisDecoder(in), in);
        }

        // MP3 often starts with ID3 tag
        if (startsWith(head, new byte[]{'I','D','3'})) {
            return new DecoderWithStream(new Mp3Decoder(in), in);
        }

        // MPEG frame sync: 0xFF 0xFB/0xF3/0xF2 are common
        if (head.length >= 2) {
            int b0 = head[0] & 0xFF;
            int b1 = head[1] & 0xFF;
            if (b0 == 0xFF && (b1 & 0xE0) == 0xE0) {
                return new DecoderWithStream(new Mp3Decoder(in), in);
            }
        }

        // Default: try MP3 first (most forgiving with weird streams)
        return new DecoderWithStream(new Mp3Decoder(in), in);
    }

    private static boolean startsWith(byte[] a, byte[] prefix) {
        if (a.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (a[i] != prefix[i]) return false;
        }
        return true;
    }

    private static byte[] readSome(InputStream in, int max) throws IOException {
        byte[] buf = new byte[max];
        int n = in.read(buf);
        if (n <= 0) return new byte[0];
        byte[] out = new byte[n];
        System.arraycopy(buf, 0, out, 0, n);
        return out;
    }
}
