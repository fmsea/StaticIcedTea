package dev.fmsea.tadr.rewrite.rules;

import java.util.function.Function;
import java.util.stream.Stream;

import dev.fmsea.absint.scalar.state.Interval32Box;
import soot.Local;
import dev.fmsea.tadr.AdditionOp;
import dev.fmsea.tadr.LeCmp;
import dev.fmsea.tadr.NegOp;
import dev.fmsea.tadr.TADR;
import dev.fmsea.tadr.Variable;

public class OctagonLeAdditionRule extends RewriteRule {

    public boolean canRewrite(TADR expr) {
        return ((expr instanceof LeCmp) &&
                (((LeCmp)expr).left instanceof Variable) &&
                (((LeCmp)expr).right instanceof AdditionOp));
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        return rewrite((LeCmp)expr, lookup);
    }

    public Stream<TADR> rewrite(LeCmp expr, Function<Local, Interval32Box> lookup) {
        var right = (AdditionOp)expr.right;
        if (right.left instanceof Variable && right.right instanceof Variable) {
            Local l = ((Variable)right.left).variable;
            Local r = ((Variable)right.right).variable;
            Interval32Box lVal = lookup.apply(l);
            Interval32Box rVal = lookup.apply(r);
            return Stream.of(
                TADR.newLeExpr(expr.left, TADR.newAddExpr(TADR.newValue(lVal), right.right)),
                TADR.newLeExpr(expr.left, TADR.newAddExpr(right.left, TADR.newValue(rVal))));
        } else if (right.left instanceof Variable && right.right instanceof NegOp) {
            var r = (NegOp)right.right;
            return Stream.of(TADR.newLeExpr(expr.left, TADR.newSubExpr(right.left, r.expr)));
        } else {
            return Stream.of(expr);
        }
    }

    @Override
    public String toString() {
        return "Octagon ≤ +";
    }
}
