package processing.smt;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import soot.Value;
import processing.providers.SmtExpressionProvider;

public class SmtExpressionReaderTest {

    @ParameterizedTest
    @ArgumentsSource(SmtExpressionProvider.class)
    void testParseExpression(String smtExpression,
                             Value expected,
                             String message) {
        SmtExpressionReader reader = new SmtExpressionReader(smtExpression);
        SmtExpression expr = reader.getSmtExpression();
        assertEquals(expected.toString(), expr.getValue().toString(), message);
    }
}
