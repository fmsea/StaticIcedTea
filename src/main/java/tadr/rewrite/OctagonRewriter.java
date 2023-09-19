package tadr.rewrite;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import abstractinterp.scalar.state.Interval32Box;
import soot.Local;
import tadr.TADR;
import tadr.rewrite.rules.IntervalRewriteRule;
import tadr.rewrite.rules.OctagonEqRewriteRule;
import tadr.rewrite.rules.OctagonGeRewriteRule;
import tadr.rewrite.rules.OctagonGtRewriteRule;
import tadr.rewrite.rules.OctagonLeRewriteRule;
import tadr.rewrite.rules.OctagonLtRewriteRule;
import tadr.rewrite.rules.OctagonReassignmentEqRewriteRule;
import tadr.rewrite.rules.OctagonReassignmentFilter;
import tadr.visitors.ReassignmentVisitor;

public class OctagonRewriter extends Rewriter {

    private static final ReassignmentVisitor reassignmentQuery = new ReassignmentVisitor();

    public OctagonRewriter() {
        super(Set.of(
            new IntervalRewriteRule(),
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
