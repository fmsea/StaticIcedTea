package dev.fmsea.driver.providers;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;

public class ZonesPredicateComparativeProvider extends MiniJavaExamplesComparativeProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext)
        throws Exception {

        return Stream.of(example("miniExample1", "zones", "predicate"),
                         example("miniExample2", "zones", "predicate"),
                         example("miniExample3", "zones", "predicate"),
                         example("miniExample4", "zones", "predicate"),
                         example("miniExample5", "zones", "predicate"),
                         example("miniExample6", "zones", "predicate"),
                         example("miniExample7", "zones", "predicate"),
                         example("miniExample8", "zones", "predicate"),
                         example("miniExample9", "zones", "predicate"));
    }
}
