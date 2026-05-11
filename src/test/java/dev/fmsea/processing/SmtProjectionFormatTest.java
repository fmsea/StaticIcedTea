package dev.fmsea.processing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Set;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import dev.fmsea.processing.providers.SmtProjectionFormatProvider;
import soot.Local;

public class SmtProjectionFormatTest extends FormatTest {

    @ParameterizedTest
    @ArgumentsSource(SmtProjectionFormatProvider.class)
    void testSmtProjectionFormat(Reader is, Set<Local> variables, String expected) {
        Writer out = new StringWriter();
        try {
            Smt2Projection.format(variables, is, out);
            assertEquals(expected, out.toString());
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            assertTrue(false);
        }
    }
}
