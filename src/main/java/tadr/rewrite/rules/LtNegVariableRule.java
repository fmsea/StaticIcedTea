package tadr.rewrite.rules;

import java.util.function.Function;
import java.util.stream.Stream;

import abstractinterp.scalar.state.Interval32Box;
import soot.Local;
import tadr.LtCmp;
import tadr.NegOp;
import tadr.TADR;
import tadr.Value;
import tadr.Variable;

public class LtNegVariableRule extends RewriteRule {
    public boolean canRewrite(TADR expr) {
        return ((expr instanceof LtCmp) &&
                (((LtCmp)expr).left instanceof Variable) &&
                (((LtCmp)expr).right instanceof NegOp) &&
                ((((NegOp)((LtCmp)expr).right).expr instanceof Variable)));
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        return rewrite((LtCmp)expr, lookup);
    }

    public Stream<TADR> rewrite(LtCmp expr, Function<Local, Interval32Box> lookup) {
        return Stream.of(TADR.newLeExpr(expr.left, TADR.newSubExpr(expr.right, TADR.newValue(1))));
    }

    @Override
    public String toString() {
        return "< (- x)";
    }
}
