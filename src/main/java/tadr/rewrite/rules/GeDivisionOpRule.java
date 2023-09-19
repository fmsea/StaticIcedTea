package tadr.rewrite.rules;

import java.util.function.Function;
import java.util.stream.Stream;
import tadr.TADR;
import tadr.GeCmp;
import tadr.DivisionOp;
import tadr.NegOp;
import tadr.Value;
import tadr.Variable;

import abstractinterp.scalar.state.Interval32Box;

import soot.Local;

public class GeDivisionOpRule extends RewriteRule {

    private final DivisionOpRule div = new DivisionOpRule();

    public boolean canRewrite(TADR expr) {
        return ((expr instanceof GeCmp) &&
                (((GeCmp)expr).left instanceof Variable) &&
                (((GeCmp)expr).right instanceof DivisionOp));
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        return rewrite((GeCmp)expr, lookup);
    }

    public Stream<TADR> rewrite(GeCmp expr, Function<Local, Interval32Box> lookup) {
        var right = (DivisionOp)expr.right;
        return div.rewrite(right, lookup).map(e -> TADR.newGeExpr(expr.left, e));
    }

    @Override
    public String toString() {
        return "≥ ÷";
    }
}
