package kr.doka.lab.huskx;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.JarLibrary;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

@SuppressWarnings("UnstableApiUsage")
public class HuskXLoader implements PluginLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger("HuskX");

    private String githubRepo;
    private String assetName;
    private String kotlinVersion;

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        loadProperties();

        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/").build());
        resolver.addDependency(new Dependency(new DefaultArtifact("org.jetbrains.kotlin:kotlin-stdlib-jdk8:" + kotlinVersion), null));
        classpathBuilder.addLibrary(resolver);

        Path cacheDir = Path.of("plugins", "HuskX", "cache");
        Path cachedJar = cacheDir.resolve(assetName);
        Path versionFile = cacheDir.resolve("version.txt");

        try {
            Files.createDirectories(cacheDir);
        } catch (IOException e) {
            LOGGER.info("[HuskX] Failed to create cache directory: {}", e.getMessage());
            return;
        }

        String latestVersion = fetchLatestVersion();

        if (latestVersion != null) {
            String cachedVersion = readCachedVersion(versionFile);

            if (!latestVersion.equals(cachedVersion)) {
                LOGGER.info("[HuskX] New version found: " + latestVersion + ". Downloading...");
                boolean success = downloadJar(latestVersion, cachedJar);
                if (success) {
                    writeCachedVersion(versionFile, latestVersion);
                    LOGGER.info("[HuskX] Download complete.");
                } else {
                    LOGGER.warn("[HuskX] Download failed. Falling back to cache.");
                }
            } else {
                LOGGER.info("[HuskX] Already up to date: {}", latestVersion);
            }
        } else {
            LOGGER.warn("[HuskX] Could not reach GitHub. Using cached JAR if available.");
        }

        if (Files.exists(cachedJar)) {
            classpathBuilder.addLibrary(new JarLibrary(cachedJar));
        } else {
            LOGGER.info("[HuskX] No cached JAR found. Plugin cannot load.");
        }
    }

    private void loadProperties() {
        Properties props = new Properties();
        try (InputStream in = HuskXLoader.class
                .getClassLoader()
                .getResourceAsStream("huskx.properties")) {
            if (in == null) throw new IOException("huskx.properties not found");
            props.load(in);
        } catch (IOException e) {
            LOGGER.info("[HuskX] Failed to load huskx.properties: {}", e.getMessage());
        }
        githubRepo    = props.getProperty("github.repo", "");
        assetName     = props.getProperty("github.asset-name", "huskx-paper.jar");
        kotlinVersion = props.getProperty("kotlin.version", "");
    }

    private String fetchLatestVersion() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/repos/" + githubRepo + "/releases/latest"))
                    .header("Accept", "application/vnd.github+json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();

            // 간단한 tag_name 파싱 (JSON 라이브러리 없이)
            String key = "\"tag_name\":\"";
            int start = body.indexOf(key);
            if (start == -1) return null;
            start += key.length();
            int end = body.indexOf("\"", start);
            return body.substring(start, end);

        } catch (Exception e) {
            LOGGER.warn("[HuskX] GitHub API request failed: {}", e.getMessage());
            return null;
        }
    }

    private boolean downloadJar(String version, Path destination) {
        String url = "https://github.com/" + githubRepo + "/releases/download/" + version + "/" + assetName;
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();

            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            Files.copy(response.body(), destination, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (Exception e) {
            LOGGER.warn("[HuskX] Failed to download JAR: {}", e.getMessage());
            return false;
        }
    }

    private String readCachedVersion(Path versionFile) {
        try {
            if (Files.exists(versionFile)) {
                return Files.readString(versionFile).strip();
            }
        } catch (IOException e) {
            LOGGER.warn("[HuskX] Failed to read version file: {}", e.getMessage());
        }
        return null;
    }

    private void writeCachedVersion(Path versionFile, String version) {
        try {
            Files.writeString(versionFile, version);
        } catch (IOException e) {
            LOGGER.warn("[HuskX] Failed to write version file: {}", e.getMessage());
        }
    }
}
