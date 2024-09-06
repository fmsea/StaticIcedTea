package dev.fmsea.driver.providers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public abstract class MiniJavaExamplesProvider implements ArgumentsProvider {

    protected Arguments example(String name, String type) {
        String source = readResourcesFile("dev/fmsea/driver/providers/" + name + ".java");
        String expectedFullSmtOutput = readResourcesFile("dev/fmsea/driver/providers/" + name + "." + type + ".smt.out");
        return Arguments.arguments(name,
                                   source,
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
