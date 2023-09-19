package tadr.rewrite;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import abstractinterp.scalar.state.Interval32Box;
import soot.Local;
import tadr.TADR;
import tadr.rewrite.rules.IntervalFolder;
import tadr.rewrite.rules.RewriteRule;

public abstract class Rewriter {

    protected static final Logger LOGGER = LoggerFactory.getLogger(Rewriter.class);

    private final IntervalFolder folder = new IntervalFolder();
    private final Set<RewriteRule> rules;

    public Rewriter(Set<RewriteRule> rules) {
        this.rules = rules;
    }

    public Stream<TADR> rewrite(Stream<TADR> exprs, Function<Local, Interval32Box> lookup) {
        return exprs.flatMap(e -> rewrite(e, lookup));
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        if (rules.stream().noneMatch(r -> r.canRewrite(expr))) {
            LOGGER.debug("Unable to rewrite an expression: {}", expr);
        }
        return rules
            .stream()
            .filter(r -> r.canRewrite(expr))
            .peek(r -> LOGGER.debug("rule {} will rewrite {}", r, expr))
            .flatMap(r -> r.rewrite(expr, lookup))
            .map(e -> e.accept(folder))
            .peek(e -> LOGGER.debug("rewrote {} ~> {}", expr, e))
            .distinct();
    }
}
