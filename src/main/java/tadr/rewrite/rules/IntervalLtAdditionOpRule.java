package tadr.rewrite.rules;

import java.util.function.Function;
import java.util.stream.Stream;

import abstractinterp.scalar.state.Interval32Box;
import soot.Local;
import tadr.AdditionOp;
import tadr.LtCmp;
import tadr.TADR;
import tadr.Variable;

public class IntervalLtAdditionOpRule extends RewriteRule {

    private final IntervalAdditionOpRule add = new IntervalAdditionOpRule();

    public boolean canRewrite(TADR expr) {
        return ((expr instanceof LtCmp) &&
                (((LtCmp)expr).left instanceof Variable) &&
                (((LtCmp)expr).right instanceof AdditionOp));
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        return rewrite((LtCmp)expr, lookup);
    }

    public Stream<TADR> rewrite(LtCmp expr, Function<Local, Interval32Box> lookup) {
        var right = (AdditionOp)expr.right;
        return add.rewrite(right, lookup).map(e -> TADR.newLtExpr(expr.left, e));
    }

    @Override
    public String toString() {
        return "Interval < +";
    }
}
