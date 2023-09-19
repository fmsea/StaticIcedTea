package tadr.rewrite.rules;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import abstractinterp.scalar.state.Interval32Box;
import soot.Local;
import tadr.LtCmp;
import tadr.TADR;
import tadr.Variable;

public class OctagonLtRewriteRule extends RewriteRule {

    private final Set<RewriteRule> rules = Set.of(
        RewriteRule.compose(new LtMultiplicationOpRule(), this),
        RewriteRule.compose(new LtDivisionOpRule(), this),
        new LtVariableRule(),
        new OctagonLtAdditionRule(),
        new OctagonLtSubtractionRule()
    );

    public boolean canRewrite(TADR expr) {
        return ((expr instanceof LtCmp) &&
                (((LtCmp)expr).left instanceof Variable));
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        return rewrite((LtCmp)expr, lookup);
    }

    public Stream<TADR> rewrite(LtCmp expr, Function<Local, Interval32Box> lookup) {
        return rules.stream()
            .filter(r -> r.canRewrite(expr))
            .peek(r -> LOGGER.debug("rule {} wants to rewrite {}", r, expr))
            .flatMap(r -> r.rewrite(expr, lookup));
    }

    @Override
    public String toString() {
        return "Octagon <";
    }
}
