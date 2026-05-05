package dev.fmsea.driver.providers;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.support.ParameterDeclarations;

public class IntMiniJavaExamplesProvider extends MiniJavaExamplesProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext extensionContext)
        throws Exception {

        return Stream.of(example("miniExample1", "int"),
                         example("miniExample2", "int"),
                         example("miniExample3", "int"),
                         example("miniExample4", "int"),
                         example("miniExample5", "int"),
                         example("miniExample6", "int"),
                         example("miniExample9", "int"));
    }
}
