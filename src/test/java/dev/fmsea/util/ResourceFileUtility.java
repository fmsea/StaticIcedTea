package dev.fmsea.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourceFileUtility {
    public static String readResourcesFile(String resourceFileName) {
        try {
            ClassLoader loader = ResourceFileUtility.class.getClassLoader();
            Path resourceFile = Paths.get(loader.getResource(resourceFileName).getFile());
            return Files.readString(resourceFile);
        } catch (IOException ex) {
            System.err.println(ex.toString());
            throw new RuntimeException(ex);
        }
    }
}
