package processing;

import java.io.Reader;
import java.io.Writer;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import processing.providers.FormatMinReachableTestProvider;

public class Smt2FormatMinReachableTest extends FormatTest {

    @ParameterizedTest
    @ArgumentsSource(FormatMinReachableTestProvider.class)
    void testSimpleMinFormat(Reader fullLeft,
                             Reader changedLeft,
                             Reader fullRight,
                             Reader changedRight,
                             String expected) {
        Writer out = new StringWriter();
        try {
            Smt2FormatMinReachable.Smt2FormatMinReachable(fullLeft,
                                                          changedLeft,
                                                          fullRight,
                                                          changedRight,
                                                          out);
            assertEquals(expected, out.toString());
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            assertTrue(false);
        }
    }
}
