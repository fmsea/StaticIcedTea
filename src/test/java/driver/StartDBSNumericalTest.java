package driver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import util.Compiler;

import driver.providers.DBSMiniJavaExamplesProvider;

public class StartDBSNumericalTest {

    @ParameterizedTest
    @ArgumentsSource(DBSMiniJavaExamplesProvider.class)
    void testDBSNumericalAnalysis(String name, String source, String expected) throws Exception {
        Path clazz = Compiler.compileSource(name, source);
        Process analysis = Runtime.getRuntime().exec(new String [] {
                "java",
                "-classpath",
                System.getProperty("java.class.path"),
                "driver.StartDBSNumerical",
                "",
                name,
                clazz.getParent().toString(),
                "1",
                "n",
            });
        analysis.waitFor(60l, TimeUnit.SECONDS);
        String out = new BufferedReader(new InputStreamReader(analysis.getInputStream(),
                                                              StandardCharsets.UTF_8))
            .lines()
            .collect(Collectors.joining("\n"));

        assertEquals(expected, out);
    }
}
