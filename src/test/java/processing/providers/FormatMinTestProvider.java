package processing.providers;

import java.io.Reader;
import java.io.StringReader;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import util.ResourceFileUtility;

public class FormatMinTestProvider implements ArgumentsProvider {

    private Reader getReader(String resource) {
        return new StringReader(ResourceFileUtility.readResourcesFile(resource));
    }

    private String getContents(String resource) {
        return ResourceFileUtility.readResourcesFile(resource);
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context)
        throws Exception {

        return Stream.of(Arguments.arguments(getReader("processing/format.min.full.in"),
                                             getReader("processing/format.min.changed.in"),
                                             getReader("processing/format.min.full.in"),
                                             getReader("processing/format.min.changed.in"),
                                             getContents("processing/format.min.smt.out")),
                         Arguments.arguments(getReader("processing/format.min.full.with-branch.in"),
                                             getReader("processing/format.min.changed.with-branch.in"),
                                             getReader("processing/format.min.full.with-branch.in"),
                                             getReader("processing/format.min.changed.with-branch.in"),
                                             getContents("processing/format.min.with-branch.smt.out")),
                         Arguments.arguments(getReader("processing/format.testclient6.ints.full.in"),
                                             getReader("processing/format.testclient6.ints.min.in"),
                                             getReader("processing/format.testclient6.zones.full.in"),
                                             getReader("processing/format.testclient6.zones.min.in"),
                                             getContents("processing/format.testclient6.min.out")),
                         Arguments.arguments(getReader("processing/format.imagedata_10.zones.in"),
                                             getReader("processing/format.imagedata_10.zones.min.in"),
                                             getReader("processing/format.imagedata_10.dom2.in"),
                                             getReader("processing/format.imagedata_10.dom2.min.in"),
                                             getContents("processing/format.imagedata_10.min.out")));
    }
}
