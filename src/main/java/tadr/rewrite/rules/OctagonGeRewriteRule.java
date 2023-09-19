package tadr.rewrite.rules;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import abstractinterp.scalar.state.Interval32Box;
import soot.Local;
import tadr.GeCmp;
import tadr.TADR;
import tadr.Variable;

public class OctagonGeRewriteRule extends RewriteRule {

    private final Set<RewriteRule> rules = Set.of(
        new OctagonGeVariableRule(),
        RewriteRule.and(new GeMultiplicationOpRule(), this),
        new OctagonGeAdditionRule(),
        new OctagonGeSubtractionRule()
    );

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
        return "Octagon ≥";
    }
}
