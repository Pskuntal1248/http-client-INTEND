package com.intend.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Lightweight auto-updater that checks a remote {@code version.json}, downloads
 * the platform-specific installer, and launches it.
 */
public final class UpdateChecker {

    /** Current app version — keep in sync with pom.xml. */
    static final String CURRENT_VERSION = "1.0.0";

    private static final String VERSION_URL =
            "https://raw.githubusercontent.com/pskuntal1248/http-client-intend/main/version.json";

    private UpdateChecker() {}

    /**
     * Checks for updates in a daemon thread.
     *
     * @param onUpdateAvailable called on the FX thread with (latestVersion, downloadUrl)
     */
    public static void checkInBackground(BiConsumer<String, String> onUpdateAvailable) {
        Thread thread = new Thread(() -> {
            try {
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(VERSION_URL))
                        .timeout(Duration.ofSeconds(5))
                        .GET()
                        .build();

                HttpResponse<String> response =
                        client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode root = mapper.readTree(response.body());
                    String latest = root.get("latest").asText();
                    String download = pickDownloadUrl(root);

                    if (isNewer(latest, CURRENT_VERSION)) {
                        Platform.runLater(() -> onUpdateAvailable.accept(latest, download));
                    }
                }
            } catch (Exception ignored) {
            }
        }, "update-checker");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Downloads the installer from {@code url} in a background thread, then
     * launches it and exits the app.
     *
     * @param url        direct download URL for the installer
     * @param onProgress called on FX thread with a status message (e.g. "Downloading 45%")
     * @param onError    called on FX thread if something goes wrong
     */
    public static void downloadAndInstall(String url, Consumer<String> onProgress, Consumer<String> onError) {
        Thread thread = new Thread(() -> {
            try {
                Platform.runLater(() -> onProgress.accept("Connecting..."));

                HttpClient client = HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofMinutes(5))
                        .GET()
                        .build();

                HttpResponse<InputStream> response =
                        client.send(request, HttpResponse.BodyHandlers.ofInputStream());

                if (response.statusCode() != 200) {
                    Platform.runLater(() -> onError.accept("Download failed (HTTP " + response.statusCode() + ")"));
                    return;
                }

                String fileName = url.substring(url.lastIndexOf('/') + 1);
                Path tempDir = Files.createTempDirectory("intend-update");
                Path installerPath = tempDir.resolve(fileName);

                long contentLength = response.headers()
                        .firstValueAsLong("content-length").orElse(-1);

                try (InputStream in = response.body()) {
                    byte[] buffer = new byte[8192];
                    long totalRead = 0;
                    int bytesRead;
                    var out = Files.newOutputStream(installerPath);

                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                        totalRead += bytesRead;
                        if (contentLength > 0) {
                            int percent = (int) (totalRead * 100 / contentLength);
                            long totalMB = totalRead / (1024 * 1024);
                            Platform.runLater(() -> onProgress.accept(
                                    "Downloading... " + percent + "% (" + totalMB + " MB)"));
                        } else {
                            long totalMB = totalRead / (1024 * 1024);
                            Platform.runLater(() -> onProgress.accept(
                                    "Downloading... " + totalMB + " MB"));
                        }
                    }
                    out.close();
                }

                Platform.runLater(() -> onProgress.accept("Installing..."));

                launchInstaller(installerPath.toFile());

                Platform.runLater(() -> {
                    onProgress.accept("Update downloaded — restarting...");
                    Platform.exit();
                    System.exit(0);
                });

            } catch (Exception e) {
                Platform.runLater(() -> onError.accept("Update failed: " + e.getMessage()));
            }
        }, "update-downloader");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Launches the downloaded installer based on the OS and file extension.
     */
    private static void launchInstaller(File installer) throws IOException {
        String name = installer.getName().toLowerCase();
        String os = System.getProperty("os.name", "").toLowerCase();

        ProcessBuilder pb;
        if (os.contains("mac") || os.contains("darwin")) {
            // .dmg — mount and open
            pb = new ProcessBuilder("open", installer.getAbsolutePath());
        } else if (os.contains("win")) {
            if (name.endsWith(".msi")) {
                pb = new ProcessBuilder("msiexec", "/i", installer.getAbsolutePath());
            } else {
                // .exe
                pb = new ProcessBuilder(installer.getAbsolutePath());
            }
        } else {
            // Linux
            if (name.endsWith(".deb")) {
                pb = new ProcessBuilder("xdg-open", installer.getAbsolutePath());
            } else if (name.endsWith(".rpm")) {
                pb = new ProcessBuilder("xdg-open", installer.getAbsolutePath());
            } else {
                pb = new ProcessBuilder("xdg-open", installer.getAbsolutePath());
            }
        }

        pb.inheritIO();
        pb.start();
    }

    static boolean isNewer(String remote, String local) {
        try {
            int[] r = parseVersion(remote);
            int[] l = parseVersion(local);
            for (int i = 0; i < 3; i++) {
                if (r[i] > l[i]) return true;
                if (r[i] < l[i]) return false;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private static String pickDownloadUrl(JsonNode root) {
        String os = System.getProperty("os.name", "").toLowerCase();
        String key;
        if (os.contains("mac") || os.contains("darwin")) {
            key = "downloadMac";
        } else if (os.contains("win")) {
            key = "downloadWindows";
        } else {
            key = "downloadLinux";
        }
        if (root.has(key)) {
            return root.get(key).asText();
        }
        if (root.has("download")) {
            return root.get("download").asText();
        }
        return "https://github.com/pskuntal1248/http-client-intend/releases/latest";
    }

    private static int[] parseVersion(String v) {
        String[] parts = v.split("\\.");
        int[] result = new int[3];
        for (int i = 0; i < Math.min(parts.length, 3); i++) {
            result[i] = Integer.parseInt(parts[i]);
        }
        return result;
    }
}
