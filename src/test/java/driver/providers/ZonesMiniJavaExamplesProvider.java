package driver.providers;

import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;

public class ZonesMiniJavaExamplesProvider extends MiniJavaExamplesProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext)
        throws Exception {

        return Stream.of(example("miniExample1", "zones"),
                         example("miniExample2", "zones"),
                         example("miniExample3", "zones"),
                         example("miniExample4", "zones"),
                         example("miniExample5", "zones"),
                         example("miniExample6", "zones"),
                         example("miniExample7", "zones"),
                         example("miniExample8", "zones"));
    }
}
