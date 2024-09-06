package dev.fmsea.tadr.rewrite.rules;

import java.util.function.Function;
import java.util.stream.Stream;

import dev.fmsea.absint.scalar.state.Interval32Box;
import soot.Local;
import dev.fmsea.tadr.EqCmp;
import dev.fmsea.tadr.TADR;
import dev.fmsea.tadr.visitors.ReassignmentVisitor;

public class OctagonReassignmentEqRewriteRule extends RewriteRule {

    private final OctagonReassignmentLeRewriteRule leRule = new OctagonReassignmentLeRewriteRule();
    private final ReassignmentVisitor reassignmentQuery = new ReassignmentVisitor();
    private final IntervalFolder folder = new IntervalFolder();

    public boolean canRewrite(TADR expr) {
        if (expr instanceof EqCmp) {
            EqCmp eq = (EqCmp)expr;
            return eq.accept(reassignmentQuery);
        } else {
            return false;
        }
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        return rewrite((EqCmp)expr, lookup);
    }

    public Stream<TADR> rewrite(EqCmp expr, Function<Local, Interval32Box> lookup) {
        return leRule.rewrite(TADR.newLeExpr(expr.left, expr.right), lookup)
            .peek(e -> LOGGER.trace("reassignment translated {} -> {}", expr, e));
    }

    @Override
    public String toString() {
        return "(reassignment) Octagon = Rewrite Rule";
    }
}
