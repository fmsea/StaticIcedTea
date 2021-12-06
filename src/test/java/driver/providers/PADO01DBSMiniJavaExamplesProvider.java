package driver.providers;

import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;

public class PADO01DBSMiniJavaExamplesProvider extends MiniJavaExamplesProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext)
        throws Exception {

        return Stream.of(example("miniExample1", "pado"),
                         example("miniExample2", "pado"),
                         example("miniExample3", "pado"),
                         example("miniExample4", "pado"),
                         example("miniExample5", "pado"),
                         example("miniExample6", "pado"));
    }
}
