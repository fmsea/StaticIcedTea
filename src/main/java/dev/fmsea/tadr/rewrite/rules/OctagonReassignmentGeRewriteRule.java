package dev.fmsea.tadr.rewrite.rules;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import dev.fmsea.absint.scalar.state.Interval32Box;
import soot.Local;
import dev.fmsea.tadr.GeCmp;
import dev.fmsea.tadr.TADR;
import dev.fmsea.tadr.Variable;

public class OctagonReassignmentGeRewriteRule extends RewriteRule {

    private final Set<RewriteRule> rules;
    private final Variable variable;

    public OctagonReassignmentGeRewriteRule(Variable variable) {
        this.variable = variable;
        this.rules = Set.of(
            RewriteRule.and(new GeMultiplicationOpRule(), this),
            new OctagonReassignmentGeAdditionRule(this.variable),
            new OctagonReassignmentGeSubtractionRule(this.variable)
        );
    }

    public boolean canRewrite(TADR expr) {
        return ((expr instanceof GeCmp) &&
                (((GeCmp)expr).left instanceof Variable));
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        return rewrite((GeCmp)expr, lookup);
    }

    public Stream<TADR> rewrite(GeCmp expr, Function<Local, Interval32Box> lookup) {
        return this.rules.stream()
            .filter(r -> r.canRewrite(expr))
            .flatMap(r -> r.rewrite(expr, lookup));
    }

    @Override
    public String toString() {
        return "(reassignment) Octagon ≥";
    }
}
