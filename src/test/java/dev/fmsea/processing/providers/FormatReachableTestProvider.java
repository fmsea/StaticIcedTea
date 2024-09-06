package dev.fmsea.processing.providers;

import java.io.Reader;
import java.io.StringReader;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import dev.fmsea.processing.Smt2FormatType;
import dev.fmsea.util.ResourceFileUtility;

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

        return Stream.of(Arguments.arguments(getReader("dev/fmsea/processing/reachable/format.basic.1.in"),
                                             getReader("dev/fmsea/processing/reachable/format.basic.1.in"),
                                             getContents("dev/fmsea/processing/reachable/format.basic.out.smt"),
                                             Smt2FormatType.MIN),
                         Arguments.arguments(getReader("dev/fmsea/processing/reachable/format.unbalanced.1.in"),
                                             getReader("dev/fmsea/processing/reachable/format.unbalanced.2.in"),
                                             getContents("dev/fmsea/processing/reachable/format.unbalanced.out.smt"),
                                             Smt2FormatType.MIN),
                         Arguments.arguments(getReader("dev/fmsea/processing/reachable/format.z1.reachable.in"),
                                             getReader("dev/fmsea/processing/reachable/format.z2.reachable.in"),
                                             getContents("dev/fmsea/processing/reachable/format.z1z2.out.smt"),
                                             Smt2FormatType.MIN),
                         Arguments.arguments(getReader("dev/fmsea/processing/reachable/format.unchanged.1.in"),
                                             getReader("dev/fmsea/processing/reachable/format.unchanged.2.in"),
                                             getContents("dev/fmsea/processing/reachable/format.unchanged.out.smt"),
                                             Smt2FormatType.MIN),
                         Arguments.arguments(getReader("dev/fmsea/processing/reachable/format.imagedata_10.dom2.in"),
                                             getReader("dev/fmsea/processing/reachable/format.imagedata_10.zones.in"),
                                             getContents("dev/fmsea/processing/reachable/format.imagedata_10.out.smt"),
                                             Smt2FormatType.MIN),
                         Arguments.arguments(getReader("dev/fmsea/processing/reachable/format.small.base64_17.zones.in"),
                                             getReader("dev/fmsea/processing/reachable/format.small.base64_17.zones-k.in"),
                                             getContents("dev/fmsea/processing/reachable/format.small.base64_17.out.smt"),
                                             Smt2FormatType.MIN),
                         Arguments.arguments(getReader("dev/fmsea/processing/reachable/format.base64_17.zones.in"),
                                             getReader("dev/fmsea/processing/reachable/format.base64_17.zones-k.in"),
                                             getContents("dev/fmsea/processing/reachable/format.base64_17.min.out.smt"),
                                             Smt2FormatType.MIN),
                         Arguments.arguments(getReader("dev/fmsea/processing/reachable/format.infblocks_1.zones-thres.in"),
                                             getReader("dev/fmsea/processing/reachable/format.infblocks_1.dom5_4.in"),
                                             getContents("dev/fmsea/processing/reachable/format.infblocks_1.min.out.smt"),
                                             Smt2FormatType.MIN),
                         Arguments.arguments(getReader("dev/fmsea/processing/reachable/format.wbs_2.zones-thres.in"),
                                             getReader("dev/fmsea/processing/reachable/format.wbs_2.dom5_4.in"),
                                             getContents("dev/fmsea/processing/reachable/format.wbs_2.min.out.smt"),
                                             Smt2FormatType.MIN),
                         Arguments.arguments(getReader("dev/fmsea/processing/reachable/format.wbs_2.zones-thres.in"),
                                             getReader("dev/fmsea/processing/reachable/format.wbs_2.dom5_4.in"),
                                             getContents("dev/fmsea/processing/reachable/format.wbs_2.full.out.smt"),
                                             Smt2FormatType.FULL),
                         Arguments.arguments(getReader("dev/fmsea/processing/reachable/format.infblocks_1.zones-thres.in"),
                                             getReader("dev/fmsea/processing/reachable/format.infblocks_1.dom5_4.in"),
                                             getContents("dev/fmsea/processing/reachable/format.infblocks_1.full.out.smt"),
                                             Smt2FormatType.FULL),
                         Arguments.arguments(getReader("dev/fmsea/processing/reachable/format.constantvalue_1.zones-thres.in"),
                                             getReader("dev/fmsea/processing/reachable/format.constantvalue_1.dom5_4.in"),
                                             getContents("dev/fmsea/processing/reachable/format.constantvalue_1.full.out.smt"),
                                             Smt2FormatType.FULL),
                         Arguments.arguments(getReader("dev/fmsea/processing/reachable/format.constantvalue_1.zones-thres.in"),
                                             getReader("dev/fmsea/processing/reachable/format.constantvalue_1.dom5_4.in"),
                                             getContents("dev/fmsea/processing/reachable/format.constantvalue_1.min.out.smt"),
                                             Smt2FormatType.MIN)
                         );
    }
}
