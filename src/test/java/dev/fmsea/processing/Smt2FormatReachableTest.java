package dev.fmsea.processing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import dev.fmsea.processing.providers.FormatReachableTestProvider;

public class Smt2FormatReachableTest extends FormatTest {

    @ParameterizedTest
    @ArgumentsSource(FormatReachableTestProvider.class)
    void testSimpleMinFormat(Reader fullLeft,
                             Reader fullRight,
                             String expected,
                             Smt2FormatType type) {
        Writer out = new StringWriter();
        try {
            Smt2FormatReachable.Smt2FormatReachable(fullLeft,
                                                    fullRight,
                                                    out,
                                                    type);
            assertEquals(expected, out.toString());
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            assertTrue(false);
        }
    }
}
