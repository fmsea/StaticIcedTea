package tadr.rewrite.rules;

import java.util.function.Function;
import java.util.stream.Stream;

import abstractinterp.scalar.state.Interval32Box;
import soot.Local;
import tadr.EqCmp;
import tadr.TADR;
import tadr.Variable;
import tadr.visitors.ReassignmentVisitor;

public class OctagonEqRewriteRule extends RewriteRule {

    private final OctagonLeRewriteRule leRule = new OctagonLeRewriteRule();
    private final OctagonGeRewriteRule geRule = new OctagonGeRewriteRule();
    private final ReassignmentVisitor reassignmentQuery = new ReassignmentVisitor();

    public boolean canRewrite(TADR expr) {
        if (expr instanceof EqCmp) {
            return !expr.accept(reassignmentQuery);
        } else {
            return false;
        }
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        return rewrite((EqCmp)expr, lookup);
    }

    public Stream<TADR> rewrite(EqCmp expr, Function<Local, Interval32Box> lookup) {
        ProjectionRewriteRule projection = new ProjectionRewriteRule((Variable)expr.left, lookup);
        TADR right = expr.right.accept(projection);
        return Stream.concat(
            leRule.rewrite(TADR.newLeExpr(expr.left, right), lookup),
            geRule.rewrite(TADR.newGeExpr(expr.left, right), lookup));
    }

    @Override
    public String toString() {
        return "Octagon = Rewrite Rule";
    }
}
