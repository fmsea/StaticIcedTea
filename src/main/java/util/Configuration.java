package util;

import java.util.Optional;
import java.util.Map;
import java.util.Properties;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import soot.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration {

    private static Logger LOGGER = LoggerFactory.getLogger(Configuration.class);
    private static Configuration instance;
    private final Properties properties;

    private Configuration() {
        this.properties = new Properties();
        ClassLoader loader = getClass().getClassLoader();
        try (InputStream stream = loader.getResourceAsStream("user.properties")) {
            this.properties.load(stream);
        } catch (Exception ex) {
            LOGGER.error("Unable to load properties: {}", ex.getMessage());
            LOGGER.trace(Stream.of(ex.getStackTrace())
                         .map(StackTraceElement::toString)
                         .collect(Collectors.joining("\n")));
        }
    }

    public static Optional<Boolean> getEnvBoolean(String key) {
        return Optional.of(Boolean.parseBoolean(System.getenv(key)));
    }

    public static Optional<Boolean> getBoolean(String key) {
        if (instance == null) {
            instance = new Configuration();
        }
        if (instance.properties.containsKey(key)) {
            return Optional.of(Boolean.parseBoolean(instance.properties.getProperty(key)));
        } else {
            return Optional.empty();
        }
    }

    public static void LoadArtifactsIntoSootPath() {
        LoadArtifactsIntoSootPath("artifacts/");
    }

    public static void LoadArtifactsIntoSootPath(String path) {
        String artifactsClassPath = Paths.get(path).toAbsolutePath().toString();
        Scene.v().setSootClassPath(Scene.v().getSootClassPath() +
                                   File.pathSeparator +
                                   artifactsClassPath);
    }
}
