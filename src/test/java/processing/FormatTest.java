package processing;

import java.io.StringReader;
import util.ResourceFileUtility;

public abstract class FormatTest {

    StringReader getReader(String resource) {
        return new StringReader(ResourceFileUtility.readResourcesFile(resource));
    }
}
