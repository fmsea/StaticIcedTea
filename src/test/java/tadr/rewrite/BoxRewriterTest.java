package tadr.rewrite;

import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.Arguments;
import tadr.TADRReader;

import soot.Local;
import common.Locals;
import tadr.providers.BoxRewriteProvider;

public class BoxRewriterTest extends RewriterTest {

    private BoxRewriter rewriter;

    @BeforeEach
    void setup() {
        this.rewriter = new BoxRewriter();
    }

    @ParameterizedTest
    @ArgumentsSource(BoxRewriteProvider.class)
    public void testRewrite(String expr, String expected, String msg) {
        assertEquals(expected,
            peek(this.rewriter.rewrite(TADRReader.parse(expr), RewriterTest::basicMap)),
            msg);
    }
}
