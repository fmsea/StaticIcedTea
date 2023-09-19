package tadr.rewrite.rules;

import java.util.function.Function;
import java.util.stream.Stream;
import tadr.TADR;
import tadr.LtCmp;
import tadr.DivisionOp;
import tadr.NegOp;
import tadr.Value;
import tadr.Variable;

import abstractinterp.scalar.state.Interval32Box;

import soot.Local;

public class LtDivisionOpRule extends RewriteRule {

    private final DivisionOpRule div = new DivisionOpRule();

    public boolean canRewrite(TADR expr) {
        return ((expr instanceof LtCmp) &&
                (((LtCmp)expr).left instanceof Variable) &&
                (((LtCmp)expr).right instanceof DivisionOp));
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        return rewrite((LtCmp)expr, lookup);
    }

    public Stream<TADR> rewrite(LtCmp expr, Function<Local, Interval32Box> lookup) {
        var right = (DivisionOp)expr.right;
        return div.rewrite(right, lookup).map(e -> TADR.newLtExpr(expr.left, e));
    }

    @Override
    public String toString() {
        return "> ÷";
    }
}
