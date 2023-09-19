package tadr.rewrite.rules;

import java.util.function.Function;
import java.util.stream.Stream;

import abstractinterp.scalar.state.Interval32Box;
import soot.Local;
import tadr.LeCmp;
import tadr.SubtractionOp;
import tadr.TADR;
import tadr.Variable;

public class OctagonLeSubtractionRule extends RewriteRule {

    public boolean canRewrite(TADR expr) {
        return ((expr instanceof LeCmp) &&
                (((LeCmp)expr).left instanceof Variable) &&
                (((LeCmp)expr).right instanceof SubtractionOp));
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        return rewrite((LeCmp)expr, lookup);
    }

    public Stream<TADR> rewrite(LeCmp expr, Function<Local, Interval32Box> lookup) {
        var right = (SubtractionOp)expr.right;
        if (right.left instanceof Variable && right.right instanceof Variable) {
            Local l = ((Variable)right.left).variable;
            Local r = ((Variable)right.right).variable;
            Interval32Box lVal = lookup.apply(l);
            Interval32Box rVal = lookup.apply(r);
            return Stream.of(
                TADR.newLeExpr(expr.left, TADR.newSubExpr(TADR.newValue(lVal), right.right)),
                TADR.newLeExpr(expr.left, TADR.newSubExpr(right.left, TADR.newValue(rVal))));
        } else {
            return Stream.of(expr);
        }
    }

    @Override
    public String toString() {
        return "Octagon ≤ -";
    }
}
