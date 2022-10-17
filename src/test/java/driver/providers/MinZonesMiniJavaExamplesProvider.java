package driver.providers;

import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;

public class MinZonesMiniJavaExamplesProvider extends MiniJavaExamplesProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext)
        throws Exception {

        return Stream.of(example("miniExample1", "min-zones"),
                         example("miniExample2", "min-zones"),
                         example("miniExample3", "min-zones"),
                         example("miniExample4", "min-zones"),
                         example("miniExample5", "min-zones"),
                         example("miniExample6", "min-zones"),
                         example("miniExample7", "min-zones"),
                         example("miniExample8", "min-zones"));
    }
}
