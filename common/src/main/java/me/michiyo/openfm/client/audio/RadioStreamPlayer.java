package me.michiyo.openfm.client.audio;

import me.michiyo.openfm.config.OpenFMClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.openal.AL10;
import org.lwjgl.system.MemoryUtil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class RadioStreamPlayer {
    private static final boolean DEBUG = true;

    private static final int MAX_PLAYLIST_DEPTH = 5;

    private static final int BUFFER_MS = 400;
    private static final int NUM_AL_BUFFERS = 6;

    // Manual attenuation tuning (blocks)
    private static final double REF_DISTANCE = 2.0;     // full volume within this distance
    private static final double MAX_DISTANCE = 32.0;    // silent at/after this distance

    // Gain smoothing (0..1); larger = faster response, smaller = smoother
    private static final float GAIN_SMOOTH = 0.18f;

    private final String url;
    private volatile Vec3 worldPos;
    private volatile int volume0to10;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean stopped = new AtomicBoolean(false);

    private int alSource = 0;
    private int[] alBuffers;

    private volatile int sampleRate = 44100;
    private volatile int channels = 2;
    private volatile int alFormat = AL10.AL_FORMAT_STEREO16;
    private volatile int pcmBytesPerBuffer = 44100 * 2 * 2 * BUFFER_MS / 1000;

    private final ArrayBlockingQueue<byte[]> pcmQueue = new ArrayBlockingQueue<>(32);
    private Thread decodeThread;

    private final Deque<ByteBuffer> directPool = new ArrayDeque<>();
    private int pooledCapacity = 0;

    // For underrun mitigation + smoothing
    private volatile byte[] lastPcm = null;
    private float currentGain = 0.0f;

    // Crossfade (de-click) between buffers
    private static final double XFADE_MS = 6.0;
    private short[] prevTail = null;      // last XFADE frames from previous buffer
    private int prevTailFrames = 0;

    // ICY metadata "now playing"
    private volatile String nowPlaying = null;
    private final Consumer<String> onMetadataTitle;

    public RadioStreamPlayer(String url, Vec3 worldPos, int volume0to10) {
        this(url, worldPos, volume0to10, null);
    }

    public RadioStreamPlayer(String url, Vec3 worldPos, int volume0to10, Consumer<String> onMetadataTitle) {
        this.url = url;
        this.worldPos = worldPos;
        this.volume0to10 = clamp0to10(volume0to10);
        this.onMetadataTitle = onMetadataTitle;
    }

    public boolean matches(String url) { return this.url.equals(url); }
    public void setWorldPos(Vec3 pos) { this.worldPos = pos; }
    public void setVolume(int v) { this.volume0to10 = clamp0to10(v); }
    public boolean isStopped() { return stopped.get(); }

    public String getNowPlaying() { return nowPlaying; }

    public void start() {
        if (!running.compareAndSet(false, true)) return;
        log("start() url=" + url);
        startDecoderThread();
    }

    public void stop() { stopInternal(); }

    public void tick(Vec3 listenerPos) {
        if (!running.get() || stopped.get() || alSource == 0) return;

        double dist = listenerPos.distanceTo(worldPos);

        // Update source position
        Vec3 p = worldPos;
        AL10.alSource3f(alSource, AL10.AL_POSITION, (float) p.x, (float) p.y, (float) p.z);

        // Apply volume * distance attenuation with smoothing
        float targetGain = computeGain(dist, volume0to10);
        currentGain = currentGain + (targetGain - currentGain) * GAIN_SMOOTH;
        AL10.alSourcef(alSource, AL10.AL_GAIN, currentGain);

        // Keep buffers flowing
        int processed = AL10.alGetSourcei(alSource, AL10.AL_BUFFERS_PROCESSED);
        while (processed-- > 0) {
            int buf = AL10.alSourceUnqueueBuffers(alSource);
            if (buf != 0) {
                byte[] pcm = pollPcm(10);

                // Underrun handling: re-use last buffer instead of injecting hard silence
                if (pcm == null) {
                    pcm = lastPcm;
                } else {
                    lastPcm = pcm;
                }

                // If we *still* have nothing (startup race), just skip re-queue this tick
                if (pcm == null) {
                    continue;
                }

                applyRamp16le(pcm, channels, sampleRate);

                ByteBuffer bb = toDirect(pcm);
                AL10.alBufferData(buf, alFormat, bb, sampleRate);
                recycleDirect(bb);

                AL10.alSourceQueueBuffers(alSource, buf);
            }
        }

        int state = AL10.alGetSourcei(alSource, AL10.AL_SOURCE_STATE);
        if (state != AL10.AL_PLAYING) {
            int queued = AL10.alGetSourcei(alSource, AL10.AL_BUFFERS_QUEUED);
            if (queued <= 2) {
                System.out.println("[OpenFM][AL] LOW QUEUED=" + queued + " pcmQueue=" + pcmQueue.size());
            }
            if (queued > 0) {
                AL10.alSourcePlay(alSource);
            }
        }
    }

    private static int clamp0to10(int v) {
        return Math.max(0, Math.min(10, v));
    }

    private static float computeGain(double dist, int volume0to10) {
        if (volume0to10 <= 0) return 0.0f;

        // Block UI volume (0..1)
        double user = volume0to10 / 10.0;

        // Client global radio volume (0..1) — your dedicated slider
        double clientMul = OpenFMClientConfig.RADIO_VOLUME.get();

        double d = dist;
        double dg;
        if (d <= REF_DISTANCE) {
            dg = 1.0;
        } else if (d >= MAX_DISTANCE) {
            dg = 0.0;
        } else {
            double t = (d - REF_DISTANCE) / (MAX_DISTANCE - REF_DISTANCE); // 0..1
            double g = 1.0 - t;
            dg = g * g; // square falloff
        }

        double gain = user * dg * clientMul;

        if (gain <= 0.0) return 0.0f;
        if (gain >= 1.0) return 1.0f;
        return (float) gain;
    }

    private static void applyRamp16le(byte[] pcm, int channels, int sampleRate) {
        if (pcm == null || pcm.length < 4) return;

        int bytesPerSampleFrame = channels * 2; // 16-bit
        int totalFrames = pcm.length / bytesPerSampleFrame;
        if (totalFrames <= 0) return;

        // 4ms ramp
        int rampFrames = Math.min(totalFrames / 2, (int)(sampleRate * 0.004));
        if (rampFrames <= 0) return;

        // Fade-in
        for (int f = 0; f < rampFrames; f++) {
            float g = (float) f / (float) rampFrames;
            int base = f * bytesPerSampleFrame;
            for (int c = 0; c < channels; c++) {
                int i = base + c * 2;
                short s = (short)((pcm[i] & 0xFF) | (pcm[i + 1] << 8));
                int v = (int)(s * g);
                pcm[i] = (byte)(v & 0xFF);
                pcm[i + 1] = (byte)((v >> 8) & 0xFF);
            }
        }

        // Fade-out
        for (int f = 0; f < rampFrames; f++) {
            float g = (float)(rampFrames - 1 - f) / (float) rampFrames;
            int frameIndex = (totalFrames - rampFrames + f);
            int base = frameIndex * bytesPerSampleFrame;
            for (int c = 0; c < channels; c++) {
                int i = base + c * 2;
                short s = (short)((pcm[i] & 0xFF) | (pcm[i + 1] << 8));
                int v = (int)(s * g);
                pcm[i] = (byte)(v & 0xFF);
                pcm[i + 1] = (byte)((v >> 8) & 0xFF);
            }
        }
    }

    private void applyCrossfade16le(byte[] pcm) {
        if (pcm == null || pcm.length < 4) return;
        if (channels < 1) return;

        int bytesPerFrame = channels * 2;
        int totalFrames = pcm.length / bytesPerFrame;
        if (totalFrames <= 0) return;

        int xfadeFrames = (int) Math.max(1, Math.min(totalFrames / 2, (sampleRate * (XFADE_MS / 1000.0))));
        int neededTailSamples = xfadeFrames * channels;

        // If we have a previous tail, blend it into the head of this buffer
        if (prevTail != null && prevTailFrames > 0) {
            int blendFrames = Math.min(xfadeFrames, prevTailFrames);
            for (int f = 0; f < blendFrames; f++) {
                float t = (float) f / (float) blendFrames; // 0..1
                float a = 1.0f - t;

                int baseByte = f * bytesPerFrame;
                int baseTail = f * channels;

                for (int c = 0; c < channels; c++) {
                    int i = baseByte + c * 2;

                    short cur = (short) ((pcm[i] & 0xFF) | (pcm[i + 1] << 8));
                    short tail = prevTail[baseTail + c];

                    int mixed = (int) (tail * a + cur * t);

                    pcm[i] = (byte) (mixed & 0xFF);
                    pcm[i + 1] = (byte) ((mixed >> 8) & 0xFF);
                }
            }
        }

        // Store this buffer's tail for the next buffer
        if (prevTail == null || prevTail.length != neededTailSamples) {
            prevTail = new short[neededTailSamples];
        }
        prevTailFrames = xfadeFrames;

        int startFrame = totalFrames - xfadeFrames;
        for (int f = 0; f < xfadeFrames; f++) {
            int frameIndex = startFrame + f;
            int baseByte = frameIndex * bytesPerFrame;
            int baseTail = f * channels;

            for (int c = 0; c < channels; c++) {
                int i = baseByte + c * 2;
                short s = (short) ((pcm[i] & 0xFF) | (pcm[i + 1] << 8));
                prevTail[baseTail + c] = s;
            }
        }
    }

    private void initOpenALOnRenderThread() {
        Minecraft.getInstance().execute(() -> {
            if (stopped.get() || alSource != 0) return;

            try {
                alSource = AL10.alGenSources();
                alCheck("init: alGenSources");
                log("OpenAL source=" + alSource);

                // We do manual attenuation
                AL10.alDistanceModel(AL10.AL_NONE);

                alBuffers = new int[NUM_AL_BUFFERS];
                for (int i = 0; i < NUM_AL_BUFFERS; i++) {
                    alBuffers[i] = AL10.alGenBuffers();
                }
                alCheck("init: alGenBuffers");

                ensurePoolCapacity(pcmBytesPerBuffer, NUM_AL_BUFFERS + 4);

                // Prime buffers
                for (int i = 0; i < NUM_AL_BUFFERS; i++) {
                    byte[] pcm = pollPcm(500);
                    if (pcm == null) {
                        pcm = pollPcm(500);
                    }
                    if (pcm == null) {
                        log("OpenAL init aborted: no PCM available to prime buffers.");
                        stopInternal();
                        return;
                    }

                    lastPcm = pcm;

                    applyRamp16le(pcm, channels, sampleRate);

                    ByteBuffer bb = toDirect(pcm);
                    AL10.alBufferData(alBuffers[i], alFormat, bb, sampleRate);
                    recycleDirect(bb);
                    applyCrossfade16le(pcm);

                    AL10.alSourceQueueBuffers(alSource, alBuffers[i]);
                }

                Vec3 p = worldPos;
                AL10.alSource3f(alSource, AL10.AL_POSITION, (float) p.x, (float) p.y, (float) p.z);

                currentGain = 0.0f;
                AL10.alSourcef(alSource, AL10.AL_GAIN, currentGain);

                AL10.alSourcePlay(alSource);

                log("OpenAL started: format=" + alFormat + " rate=" + sampleRate + " ch=" + channels + " bufBytes=" + pcmBytesPerBuffer);
            } catch (Throwable t) {
                log("OpenAL init failed: " + t);
                t.printStackTrace();
                stopInternal();
            }
        });
    }

    private byte[] pollPcm(long ms) {
        try {
            return pcmQueue.poll(ms, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private void startDecoderThread() {
        decodeThread = new Thread(() -> {
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

            PcmDecoder decoder = null;

            try {
                HttpResponse<InputStream> resp = openAudioResponse(client, URI.create(url));
                if (resp == null) return;

                int metaInt = parseIcyMetaInt(resp.headers().firstValue("icy-metaint"));
                String contentType = resp.headers().firstValue("content-type").orElse("");
                log("Content-Type=" + contentType);

                try (InputStream raw = resp.body();
                     BufferedInputStream bin0 = new BufferedInputStream(raw, 64 * 1024)) {

                    InputStream audioIn = bin0;

                    if (metaInt > 0) {
                        log("ICY: metaint=" + metaInt);
                        audioIn = new IcyStrippingInputStream(
                                bin0,
                                metaInt,
                                title -> {
                                    if (title == null || title.isBlank()) return;
                                    String prev = nowPlaying;
                                    if (prev != null && prev.equals(title)) return;

                                    nowPlaying = title;
                                    log("ICY StreamTitle=" + title);

                                    if (onMetadataTitle != null) {
                                        try { onMetadataTitle.accept(title); } catch (Throwable ignored) {}
                                    }
                                }
                        );

                        // add buffering AFTER stripping so DecoderFactory has a nice buffer
                        audioIn = new BufferedInputStream(audioIn, 64 * 1024);
                    }

                    DecoderFactory.DecoderWithStream created = DecoderFactory.create(audioIn, contentType);
                    decoder = created.decoder;

                    log("Decoder=" + decoder.getClass().getSimpleName());

                    this.sampleRate = Math.max(8000, decoder.getSampleRate());
                    this.channels = Math.max(1, Math.min(2, decoder.getChannels()));
                    this.alFormat = (this.channels == 1) ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16;
                    this.pcmBytesPerBuffer = (this.sampleRate * this.channels * 2 * BUFFER_MS) / 1000;

                    log("Detected: rate=" + sampleRate + " channels=" + channels + " bufBytes=" + pcmBytesPerBuffer);

                    byte[] first = decoder.readPcmChunk(pcmBytesPerBuffer);
                    if (first == null) {
                        log("Decoder produced EOF immediately (first chunk null).");
                        return;
                    }
                    log("First chunk bytes=" + first.length);

                    pcmQueue.offer(first, 1, TimeUnit.SECONDS);

                    initOpenALOnRenderThread();

                    while (running.get() && !stopped.get()) {
                        byte[] pcm = decoder.readPcmChunk(pcmBytesPerBuffer);
                        if (pcm == null) {
                            log("Decoder EOF.");
                            break;
                        }

                        try {
                            if (!pcmQueue.offer(pcm, 1, TimeUnit.SECONDS)) {
                                // queue full; drop
                            }
                        } catch (InterruptedException ie) {
                            if (stopped.get() || !running.get()) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                            log("Decoder thread interrupted unexpectedly: " + ie);
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            } catch (InterruptedException ie) {
                if (stopped.get() || !running.get()) {
                    Thread.currentThread().interrupt();
                } else {
                    log("Decoder thread interrupted unexpectedly: " + ie);
                    Thread.currentThread().interrupt();
                }
            } catch (Throwable t) {
                if (stopped.get() || !running.get()) {
                    Thread.currentThread().interrupt();
                } else {
                    log("Decoder thread exception: " + t);
                    t.printStackTrace();
                }
            } finally {
                if (decoder != null) {
                    try { decoder.close(); } catch (Throwable ignored) {}
                }
                if (!stopped.get()) {
                    stopInternal();
                }
            }
        }, "OpenFM-Decode-" + url.hashCode());

        decodeThread.setDaemon(true);
        decodeThread.start();
    }

    private static HttpResponse<InputStream> openAudioResponse(HttpClient client, URI initialUri) throws Exception {
        URI currentUri = initialUri;

        for (int depth = 0; depth <= MAX_PLAYLIST_DEPTH; depth++) {
            log("HTTP GET " + currentUri);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(currentUri)
                    .GET()
                    .header("User-Agent", "OpenFM")
                    .header("Icy-MetaData", "1")
                    .build();

            HttpResponse<InputStream> resp = client.send(req, HttpResponse.BodyHandlers.ofInputStream());
            log("HTTP status=" + resp.statusCode());
            if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                resp.body().close();
                return null;
            }

            String contentType = resp.headers().firstValue("content-type").orElse("");
            if (!M3uPlaylist.isPlaylist(currentUri, contentType)) {
                return resp;
            }

            byte[] contents;
            try (InputStream body = resp.body()) {
                contents = body.readNBytes(M3uPlaylist.MAX_BYTES + 1);
            }

            if (contents.length > M3uPlaylist.MAX_BYTES) {
                log("M3U playlist exceeds " + M3uPlaylist.MAX_BYTES + " bytes.");
                return null;
            }

            Optional<URI> entry = M3uPlaylist.firstHttpEntry(currentUri, contents);
            if (entry.isEmpty()) {
                log("M3U playlist contains no HTTP stream URL.");
                return null;
            }

            URI nextUri = entry.get();
            log("M3U resolved to " + nextUri);
            currentUri = nextUri;
        }

        log("M3U playlist nesting exceeds " + MAX_PLAYLIST_DEPTH + ".");
        return null;
    }

    private static int parseIcyMetaInt(Optional<String> v) {
        if (v.isEmpty()) return 0;
        try {
            int n = Integer.parseInt(v.get().trim());
            return Math.max(0, n);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private void stopInternal() {
        if (stopped.getAndSet(true)) return;
        running.set(false);

        if (decodeThread != null) {
            decodeThread.interrupt();
            decodeThread = null;
        }

        prevTail = null;
        prevTailFrames = 0;

        Minecraft.getInstance().execute(() -> {
            try {
                if (alSource != 0) {
                    AL10.alSourceStop(alSource);

                    int queued = AL10.alGetSourcei(alSource, AL10.AL_BUFFERS_QUEUED);
                    while (queued-- > 0) {
                        AL10.alSourceUnqueueBuffers(alSource);
                    }

                    AL10.alDeleteSources(alSource);
                    alSource = 0;
                }
                if (alBuffers != null) {
                    for (int b : alBuffers) {
                        if (b != 0) AL10.alDeleteBuffers(b);
                    }
                    alBuffers = null;
                }
            } catch (Throwable ignored) {
            } finally {
                clearPool();
            }
        });
    }

    private void ensurePoolCapacity(int neededCapacity, int desiredCount) {
        if (pooledCapacity != neededCapacity) {
            clearPool();
            pooledCapacity = neededCapacity;
        }
        while (directPool.size() < desiredCount) {
            directPool.add(MemoryUtil.memAlloc(pooledCapacity));
        }
    }

    private ByteBuffer toDirect(byte[] pcm) {
        ByteBuffer bb = directPool.pollFirst();
        if (bb == null || bb.capacity() < pcm.length) {
            if (bb != null) MemoryUtil.memFree(bb);
            bb = MemoryUtil.memAlloc(Math.max(pooledCapacity, pcm.length));
        }
        bb.clear();
        bb.put(pcm);
        bb.flip();
        return bb;
    }

    private void recycleDirect(ByteBuffer bb) {
        if (bb == null) return;
        if (pooledCapacity > 0 && bb.capacity() == pooledCapacity) {
            directPool.addLast(bb);
        } else {
            MemoryUtil.memFree(bb);
        }
    }

    private void clearPool() {
        ByteBuffer b;
        while ((b = directPool.pollFirst()) != null) {
            MemoryUtil.memFree(b);
        }
        pooledCapacity = 0;
    }

    private static void alCheck(String where) {
        int err = AL10.alGetError();
        if (err != AL10.AL_NO_ERROR) {
            System.out.println("[OpenFM][AL] " + where + " err=" + err);
        }
    }

    private static void log(String s) {
        if (DEBUG) System.out.println("[OpenFM][Stream] " + s);
    }

    /**
     * InputStream wrapper that strips ICY metadata blocks out of a stream.
     * Returns only audio bytes to the decoder.
     */
    private static final class IcyStrippingInputStream extends InputStream {
        private final InputStream in;
        private final int metaInt;
        private int remainingUntilMeta;
        private final Consumer<String> onTitle;

        private String lastTitle = null;

        IcyStrippingInputStream(InputStream in, int metaInt, Consumer<String> onTitle) {
            this.in = in;
            this.metaInt = metaInt;
            this.remainingUntilMeta = metaInt;
            this.onTitle = onTitle;
        }

        @Override
        public int read() throws IOException {
            byte[] b = new byte[1];
            int n = read(b, 0, 1);
            return (n <= 0) ? -1 : (b[0] & 0xFF);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (len <= 0) return 0;

            // If we're exactly at the metadata boundary, consume metadata first
            if (metaInt > 0 && remainingUntilMeta == 0) {
                consumeMetadataBlock();
                remainingUntilMeta = metaInt;
            }

            int maxAudio = len;
            if (metaInt > 0) {
                maxAudio = Math.min(len, remainingUntilMeta);
            }

            int n = in.read(b, off, maxAudio);
            if (n < 0) return -1;

            if (metaInt > 0) {
                remainingUntilMeta -= n;
                if (remainingUntilMeta == 0) {
                    consumeMetadataBlock();
                    remainingUntilMeta = metaInt;
                }
            }

            return n;
        }

        private void consumeMetadataBlock() throws IOException {
            int lenByte = in.read();
            if (lenByte < 0) return;

            int metaLen = lenByte * 16;
            if (metaLen <= 0) return;

            byte[] meta = in.readNBytes(metaLen);
            if (meta.length != metaLen) return;

            String metaStr = decodeMetadata(meta);
            String title = extractStreamTitle(metaStr);
            if (title == null || title.isBlank()) return;

            if (lastTitle != null && lastTitle.equals(title)) return;
            lastTitle = title;

            if (onTitle != null) {
                onTitle.accept(title);
            }
        }

        private static String decodeMetadata(byte[] meta) {
            int length = meta.length;
            while (length > 0 && meta[length - 1] == 0) length--;

            try {
                return StandardCharsets.UTF_8.newDecoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT)
                        .decode(ByteBuffer.wrap(meta, 0, length))
                        .toString()
                        .trim();
            } catch (CharacterCodingException ignored) {
                return new String(meta, 0, length, StandardCharsets.ISO_8859_1).trim();
            }
        }

        private static String extractStreamTitle(String meta) {
            // Common: StreamTitle='Artist - Track';StreamUrl='...';
            int idx = meta.indexOf("StreamTitle='");
            if (idx < 0) return null;
            idx += "StreamTitle='".length();

            int end = meta.indexOf("';", idx);
            if (end < 0) end = meta.indexOf("'", idx);
            if (end < 0) return null;

            String title = meta.substring(idx, end).trim();
            if (title.isEmpty()) return null;

            title = title.replace("\r", " ").replace("\n", " ");
            return title;
        }

        @Override
        public void close() throws IOException {
            in.close();
        }
    }
}
