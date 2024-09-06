package dev.fmsea.tadr.rewrite.rules;

import java.util.function.Function;
import java.util.stream.Stream;

import dev.fmsea.absint.scalar.state.Interval32Box;
import soot.Local;
import dev.fmsea.tadr.TADR;
import dev.fmsea.tadr.rewrite.IntervalProjectionRewriter;

public class IntervalRewriteRule extends RewriteRule {

    private final IntervalFolder folder = new IntervalFolder();
    private final IntervalGtLtRewriteRule gtLtRewriteRule = new IntervalGtLtRewriteRule();

    public boolean canRewrite(TADR expr) {
        // you bet it can!
        return true;
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        IntervalProjectionRewriter rewriter = new IntervalProjectionRewriter(lookup);
        return expr.accept(rewriter)
            .peek(e -> LOGGER.debug("projected {} ~> {}", expr, e))
            .map(e -> e.accept(folder))
            .peek(e -> LOGGER.debug("folded {} ~> {}", expr, e))
            .map(e -> e.accept(gtLtRewriteRule))
            .peek(e -> LOGGER.debug("removed < | > {} ~> {}", expr, e));
    }

    @Override
    public String toString() {
        return "[𝒫]";
    }
}
