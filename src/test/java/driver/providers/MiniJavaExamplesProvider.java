package driver.providers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public abstract class MiniJavaExamplesProvider implements ArgumentsProvider {

    protected Arguments example(String name, String type) throws IOException {
        String source = readResourcesFile("driver/providers/" + name + ".java");
        String output = readResourcesFile("driver/providers/" + name + "." + type + ".analysis.out");
        return Arguments.arguments(name, source, output.trim());
    }

    protected String readResourcesFile(String fileName) throws IOException {
        ClassLoader loader = getClass().getClassLoader();
        Path resourceFile = Paths.get(loader.getResource(fileName).getFile());
        return Files.readString(resourceFile);
    }
}
