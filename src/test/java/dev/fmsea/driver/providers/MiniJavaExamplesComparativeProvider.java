package dev.fmsea.driver.providers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MiniJavaExamplesComparativeProvider implements ArgumentsProvider {

    private static Logger LOGGER = LoggerFactory.getLogger(MiniJavaExamplesComparativeProvider.class);

    protected Arguments example(String name, String dom1, String dom2) {
        String source = readResourcesFile("dev/fmsea/driver/providers/" + name + ".java");
        String mask = "dev/fmsea/driver/providers/%s.%s.%s.out";
        String dom1ExpectedFullOutput = readResourcesFile(String.format(mask, name, dom1, "smt"));
        String dom2ExpectedFullOutput = readResourcesFile(String.format(mask, name, dom2, "smt"));
        String expectedSmtFormula = readResourcesFile(String.format("dev/fmsea/driver/providers/%s.%s.%s.smt",
                                                                    name,
                                                                    dom1,
                                                                    dom2));
        String expectedSmtResults = readResourcesFile(String.format("dev/fmsea/driver/providers/%s.%s.%s.smt.results",
                                                                    name,
                                                                    dom1,
                                                                    dom2));
        return Arguments.arguments(name,
                                   source,
                                   dom1ExpectedFullOutput,
                                   dom2ExpectedFullOutput,
                                   expectedSmtFormula,
                                   expectedSmtResults);
    }

    protected String readResourcesFile(String filename) {
        ClassLoader loader = getClass().getClassLoader();
        try (InputStream stream = loader.getResourceAsStream(filename);
             InputStreamReader isReader = new InputStreamReader(stream);
             BufferedReader reader = new BufferedReader(isReader)) {
            return reader.lines().collect(Collectors.joining("\n")).trim();
        } catch (Exception ex) {
            LOGGER.error("Error reading test resource file: {}:{} [filename: {}]",
                         ex.toString(),
                         ex.getMessage(),
                         filename);
        }
        return "";
    }
}
