package dev.fmsea.driver.providers;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.support.ParameterDeclarations;

public class DBSMiniJavaExamplesProvider extends MiniJavaExamplesProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext extensionContext)
        throws Exception {

        return Stream.of(example("miniExample1", "dbs"),
                         example("miniExample2", "dbs"),
                         example("miniExample3", "dbs"),
                         example("miniExample4", "dbs"),
                         example("miniExample5", "dbs"));
    }
}
