package tadr.rewrite.rules;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import abstractinterp.scalar.state.Interval32Box;
import soot.Local;
import tadr.LeCmp;
import tadr.TADR;
import tadr.Variable;

public class OctagonLeRewriteRule extends RewriteRule {

    private final Set<RewriteRule> rules = Set.of(
        new OctagonLeVariableRule(),
        RewriteRule.and(new LeMultiplicationOpRule(), this),
        new OctagonLeAdditionRule(),
        new OctagonLeSubtractionRule()
    );

    public boolean canRewrite(TADR expr) {
        return ((expr instanceof LeCmp) &&
                (((LeCmp)expr).left instanceof Variable));
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        return rewrite((LeCmp)expr, lookup);
    }

    public Stream<TADR> rewrite(LeCmp expr, Function<Local, Interval32Box> lookup) {
        return rules.stream()
            .filter(r -> r.canRewrite(expr))
            .peek(r -> LOGGER.debug("rule: {} will rewrite {}", r, expr))
            .flatMap(r -> r.rewrite(expr, lookup));
    }

    @Override
    public String toString() {
        return "Octagon ≤";
    }
}
