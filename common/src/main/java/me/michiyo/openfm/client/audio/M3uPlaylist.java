package me.michiyo.openfm.client.audio;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;

final class M3uPlaylist {
    static final int MAX_BYTES = 64 * 1024;

    private M3uPlaylist() {}

    static boolean isPlaylist(URI uri, String contentType) {
        String type = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        int parameter = type.indexOf(';');
        if (parameter >= 0) {
            type = type.substring(0, parameter).trim();
        }

        if (type.equals("audio/x-mpegurl")
                || type.equals("audio/mpegurl")
                || type.equals("application/x-mpegurl")
                || type.equals("application/mpegurl")) {
            return true;
        }

        String path = uri.getPath();
        return path != null && path.toLowerCase(Locale.ROOT).endsWith(".m3u");
    }

    static Optional<URI> firstHttpEntry(URI playlistUri, byte[] contents) {
        String playlist = new String(contents, StandardCharsets.UTF_8);
        if (!playlist.isEmpty() && playlist.charAt(0) == '\uFEFF') {
            playlist = playlist.substring(1);
        }

        for (String line : playlist.split("\\R")) {
            String entry = line.trim();
            if (entry.isEmpty() || entry.startsWith("#")) continue;

            try {
                URI resolved = playlistUri.resolve(entry);
                String scheme = resolved.getScheme();
                if (scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
                    return Optional.of(resolved);
                }
            } catch (IllegalArgumentException ignored) {
                // Try the next entry in a malformed or mixed playlist.
            }
        }

        return Optional.empty();
    }
}
