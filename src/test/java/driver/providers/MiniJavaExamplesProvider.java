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

    protected Arguments example(String name, String type) {
        String source = readResourcesFile("driver/providers/" + name + ".java");
        String expectedChangedOutput = readResourcesFile("driver/providers/" + name + "." + type + ".changed.out");
        String expectedSubgraphOutput = readResourcesFile("driver/providers/" + name + "." + type + ".subgraph.out");
        String expectedSubgraphMinOutput = readResourcesFile("driver/providers/" + name + "." + type + ".subgraph-min.out");
        String expectedFullSmtOutput = readResourcesFile("driver/providers/" + name + "." + type + ".smt.out");
        return Arguments.arguments(name,
                                   source,
                                   expectedChangedOutput.trim(),
                                   expectedSubgraphOutput.trim(),
                                   expectedSubgraphMinOutput.trim(),
                                   expectedFullSmtOutput.trim());
    }

    protected String readResourcesFile(String fileName) {
        try {
        ClassLoader loader = getClass().getClassLoader();
        Path resourceFile = Paths.get(loader.getResource(fileName).getFile());
        return Files.readString(resourceFile);
        } catch (IOException ex) {
            System.err.println("Unable to read resources files: " + ex.getMessage());
            return "";
        }
    }
}
