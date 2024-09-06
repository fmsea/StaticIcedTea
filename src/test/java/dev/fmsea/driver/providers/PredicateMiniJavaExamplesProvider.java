package dev.fmsea.driver.providers;

import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;

public class PredicateMiniJavaExamplesProvider extends MiniJavaExamplesProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext)
        throws Exception {

        return Stream.of(example("miniExample1", "predicate"),
                         example("miniExample2", "predicate"),
                         example("miniExample3", "predicate"),
                         example("miniExample4", "predicate"),
                         example("miniExample5", "predicate"),
                         example("miniExample6", "predicate"),
                         example("miniExample7", "predicate"),
                         example("miniExample8", "predicate"),
                         example("miniExample9", "predicate"));
    }
}
