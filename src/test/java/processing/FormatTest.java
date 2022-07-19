package processing;

import java.io.StringReader;
import util.ResourceFileUtility;

public abstract class FormatTest {

    String getContents(String resource) {
        return ResourceFileUtility.readResourcesFile(resource);
    }

    StringReader getReader(String resource) {
        return new StringReader(ResourceFileUtility.readResourcesFile(resource));
    }
}
