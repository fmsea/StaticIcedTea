package dev.fmsea.tadr.rewrite.rules;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import dev.fmsea.absint.scalar.state.Interval32Box;
import soot.Local;
import dev.fmsea.tadr.GtCmp;
import dev.fmsea.tadr.TADR;
import dev.fmsea.tadr.Variable;

public class OctagonGtRewriteRule extends RewriteRule {

    private final Set<RewriteRule> rules = Set.of(
        RewriteRule.and(new GtMultiplicationOpRule(), this),
        RewriteRule.and(new GtDivisionOpRule(), this),
        new GtVariableRule(),
        new OctagonGtAdditionRule(),
        new OctagonGtSubtractionRule()
    );

    public boolean canRewrite(TADR expr) {
        return ((expr instanceof GtCmp) &&
                (((GtCmp)expr).left instanceof Variable));
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        return rewrite((GtCmp)expr, lookup);
    }

    public Stream<TADR> rewrite(GtCmp expr, Function<Local, Interval32Box> lookup) {
        return rules.stream()
            .filter(r -> r.canRewrite(expr))
            .peek(r -> LOGGER.debug("rule {} wants to rewrite {}", r, expr))
            .flatMap(r -> r.rewrite(expr, lookup));
    }

    @Override
    public String toString() {
        return "Octagon >";
    }
}
