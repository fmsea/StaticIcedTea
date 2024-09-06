package dev.fmsea.tadr.rewrite.rules;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import dev.fmsea.tadr.TADR;
import dev.fmsea.tadr.LeCmp;
import dev.fmsea.tadr.AdditionOp;
import dev.fmsea.tadr.NegOp;
import dev.fmsea.tadr.Value;
import dev.fmsea.tadr.Variable;

import dev.fmsea.absint.scalar.state.Interval32Box;

import soot.Local;

public class IntervalLeRewriteRule extends RewriteRule {

    private final Set<RewriteRule> rules = Set.of(
        new IntervalLeAdditionOpRule(),
        new IntervalLeSubtractionOpRule(),
        new LeDivisionOpRule(),
        new LeMultiplicationOpRule()
    );

    public boolean canRewrite(TADR expr) {
        return (expr instanceof LeCmp);
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
        return "Interval ≤";
    }
}
