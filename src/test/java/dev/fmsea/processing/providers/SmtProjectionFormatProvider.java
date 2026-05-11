package dev.fmsea.processing.providers;

import java.io.Reader;
import java.io.StringReader;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;

import dev.fmsea.common.Locals;
import dev.fmsea.util.ResourceFileUtility;

public class SmtProjectionFormatProvider implements ArgumentsProvider {
    private Reader getReader(String resource) {
        return new StringReader(ResourceFileUtility.readResourcesFile(resource));
    }

    private String getContents(String resource) {
        return ResourceFileUtility.readResourcesFile(resource);
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) throws Exception {
        return Stream.of(
            Arguments.arguments(
                getReader("dev/fmsea/processing/projection/test.Toy_1.smt.in"),
                Locals.get("i1", "r0"),
                getContents("dev/fmsea/processing/projection/test.Toy_1.smt.out")),
            Arguments.arguments(
                getReader("dev/fmsea/processing/projection/test.Fibonacci_1.smt.in"),
                Locals.get("i2", "i3"),
                getContents("dev/fmsea/processing/projection/test.Fibonacci_1.smt.out"))
        );
    }
}
