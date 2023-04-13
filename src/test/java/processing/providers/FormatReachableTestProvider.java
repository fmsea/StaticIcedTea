package processing.providers;

import java.io.Reader;
import java.io.StringReader;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import util.ResourceFileUtility;

public class FormatReachableTestProvider implements ArgumentsProvider {

    private Reader getReader(String resource) {
        return new StringReader(ResourceFileUtility.readResourcesFile(resource));
    }

    private String getContents(String resource) {
        return ResourceFileUtility.readResourcesFile(resource);
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context)
        throws Exception {

        return Stream.of(Arguments.arguments(getReader("processing/reachable/format.basic.1.in"),
                                             getReader("processing/reachable/format.basic.1.in"),
                                             getContents("processing/reachable/format.basic.out.smt")),
                         Arguments.arguments(getReader("processing/reachable/format.unbalanced.1.in"),
                                             getReader("processing/reachable/format.unbalanced.2.in"),
                                             getContents("processing/reachable/format.unbalanced.out.smt")),
                         Arguments.arguments(getReader("processing/reachable/format.z1.reachable.in"),
                                             getReader("processing/reachable/format.z2.reachable.in"),
                                             getContents("processing/reachable/format.z1z2.out.smt")),
                         Arguments.arguments(getReader("processing/reachable/format.unchanged.1.in"),
                                             getReader("processing/reachable/format.unchanged.2.in"),
                                             getContents("processing/reachable/format.unchanged.out.smt")),
                         Arguments.arguments(getReader("processing/reachable/format.imagedata_10.dom2.in"),
                                             getReader("processing/reachable/format.imagedata_10.zones.in"),
                                             getContents("processing/reachable/format.imagedata_10.out.smt")),
                         Arguments.arguments(getReader("processing/reachable/format.small.base64_17.zones.in"),
                                             getReader("processing/reachable/format.small.base64_17.zones-k.in"),
                                             getContents("processing/reachable/format.small.base64_17.out.smt")),
                         Arguments.arguments(getReader("processing/reachable/format.base64_17.zones.in"),
                                             getReader("processing/reachable/format.base64_17.zones-k.in"),
                                             getContents("processing/reachable/format.base64_17.min.out.smt")));
    }
}
