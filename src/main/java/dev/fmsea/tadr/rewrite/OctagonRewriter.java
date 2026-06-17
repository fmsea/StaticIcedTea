package dev.fmsea.tadr.rewrite;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.fmsea.absint.scalar.state.Interval32Box;
import dev.fmsea.tadr.TADR;
import dev.fmsea.tadr.rewrite.rules.GeLeNormalizationRewriteRule;
import dev.fmsea.tadr.rewrite.rules.IntervalRewriteRule;
import dev.fmsea.tadr.rewrite.rules.OctagonEqRewriteRule;
import dev.fmsea.tadr.rewrite.rules.OctagonGeRewriteRule;
import dev.fmsea.tadr.rewrite.rules.OctagonGtRewriteRule;
import dev.fmsea.tadr.rewrite.rules.OctagonLeRewriteRule;
import dev.fmsea.tadr.rewrite.rules.OctagonLtRewriteRule;
import dev.fmsea.tadr.rewrite.rules.OctagonReassignmentEqRewriteRule;
import dev.fmsea.tadr.rewrite.rules.OctagonReassignmentFilter;
import dev.fmsea.tadr.visitors.ReassignmentVisitor;
import soot.Local;

public class OctagonRewriter extends Rewriter {

    private static final ReassignmentVisitor reassignmentQuery = new ReassignmentVisitor();

    public OctagonRewriter() {
        super(Set.of(
            new IntervalRewriteRule(),
            new GeLeNormalizationRewriteRule(),
            new OctagonReassignmentEqRewriteRule(),
            new OctagonEqRewriteRule(),
            new OctagonLeRewriteRule(),
            new OctagonLtRewriteRule(),
            new OctagonGeRewriteRule(),
            new OctagonGtRewriteRule()
        ));
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        Set<TADR> exprs = super.rewrite(expr, lookup).collect(Collectors.toSet());
        OctagonReassignmentFilter reassignmentFilter = new OctagonReassignmentFilter(exprs);
        if (expr.accept(reassignmentQuery)) {
            return reassignmentFilter.filter()
                .peek(thunk -> LOGGER.debug("allowed reassignment thunk: {}", thunk));
        } else {
            return exprs.stream();
        }
    }
}
