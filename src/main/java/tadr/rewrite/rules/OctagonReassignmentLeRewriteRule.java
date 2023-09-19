package tadr.rewrite.rules;

import java.util.Set;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import tadr.TADR;
import tadr.LeCmp;
import tadr.Value;
import tadr.Variable;

import abstractinterp.scalar.state.Interval32Box;

import soot.Local;

public class OctagonReassignmentLeRewriteRule extends RewriteRule {

    private final Set<RewriteRule> rules = Set.of(
        new OctagonLeVariableRule(),
        RewriteRule.and(new LeMultiplicationOpRule(), this),
        RewriteRule.and(new LeDivisionOpRule(), this),
        RewriteRule.or(new OctagonLeAdditionRule(), new IntervalLeAdditionOpRule()),
        RewriteRule.or(new OctagonLeSubtractionRule(), new IntervalLeSubtractionOpRule())
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
            .flatMap(r -> r.rewrite(expr, lookup));
    }

    @Override
    public String toString() {
        return "(reassignment) Octagon ≤";
    }
}
