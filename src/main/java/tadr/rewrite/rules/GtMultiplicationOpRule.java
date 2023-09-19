package tadr.rewrite.rules;

import java.util.function.Function;
import java.util.stream.Stream;
import tadr.TADR;
import tadr.GtCmp;
import tadr.MultiplicationOp;
import tadr.NegOp;
import tadr.Value;
import tadr.Variable;

import abstractinterp.scalar.state.Interval32Box;

import soot.Local;

public class GtMultiplicationOpRule extends RewriteRule {

    private final MultiplicationOpRule mul = new MultiplicationOpRule();

    public boolean canRewrite(TADR expr) {
        return ((expr instanceof GtCmp) &&
                (((GtCmp)expr).left instanceof Variable) &&
                (((GtCmp)expr).right instanceof MultiplicationOp));
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        return rewrite((GtCmp)expr, lookup);
    }

    public Stream<TADR> rewrite(GtCmp expr, Function<Local, Interval32Box> lookup) {
        var right = (MultiplicationOp)expr.right;
        return mul.rewrite(right, lookup).map(e -> TADR.newGtExpr(expr.left, e));
    }

    @Override
    public String toString() {
        return "> ×";
    }
}
