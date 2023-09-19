package tadr.rewrite;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import tadr.TADRReader;
import tadr.providers.OctagonRewriteProvider;

public class OctagonRewriterTest extends RewriterTest {

    private OctagonRewriter rewriter;

    @BeforeEach
    void setup() {
        this.rewriter = new OctagonRewriter();
    }

    @ParameterizedTest
    @ArgumentsSource(OctagonRewriteProvider.class)
    public void testExpressionRewriting(String expr, String expected, String msg) {
        assertEquals(expected,
            peek(rewriter.rewrite(TADRReader.parse(expr), RewriterTest::basicMap)),
            msg);
    }
}
