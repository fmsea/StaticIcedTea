package dev.fmsea.processing;

import java.io.StringReader;
import dev.fmsea.util.ResourceFileUtility;

public abstract class FormatTest {

    String getContents(String resource) {
        return ResourceFileUtility.readResourcesFile(resource);
    }

    StringReader getReader(String resource) {
        return new StringReader(ResourceFileUtility.readResourcesFile(resource));
    }
}
