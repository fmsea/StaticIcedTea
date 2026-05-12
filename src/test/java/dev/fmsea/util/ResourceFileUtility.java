package dev.fmsea.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

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

    public static List<String> ls(String folderName) {
        try {
            URL resource = ResourceFileUtility.class.getClassLoader().getResource(folderName);
            if (resource != null) {
                Path path = Paths.get(resource.toURI());
                return Files.list(path)
                    .map(Path::toFile)
                    .map(p -> p.getName())
                    .collect(Collectors.toList());
            } else {
                System.err.println("URL was null");
                return List.of();
            }
        } catch (IOException ex) {
            System.err.println(ex.toString());
            ex.printStackTrace(System.err);
            throw new RuntimeException(ex);
        } catch (URISyntaxException ex) {
            System.err.println(ex.toString());
            ex.printStackTrace(System.err);
            throw new RuntimeException(ex);
        }
    }
}
