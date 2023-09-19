package tadr.rewrite.rules;

import java.util.function.Function;
import java.util.stream.Stream;

import abstractinterp.scalar.state.Interval32Box;
import soot.Local;
import tadr.AdditionOp;
import tadr.LtCmp;
import tadr.NegOp;
import tadr.TADR;
import tadr.Value;
import tadr.Variable;

public class OctagonLtAdditionRule extends RewriteRule {

    private final OctagonLeRewriteRule leRule = new OctagonLeRewriteRule();

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
        if (right.left instanceof Variable && right.right instanceof Variable) {
            Local l = ((Variable)right.left).variable;
            Local r = ((Variable)right.right).variable;
            Interval32Box lVal = lookup.apply(l);
            Interval32Box rVal = lookup.apply(r);
            return Stream.of(
                TADR.newLtExpr(expr.left, TADR.newAddExpr(TADR.newValue(lVal), right.right)),
                TADR.newLtExpr(expr.left, TADR.newAddExpr(right.left, TADR.newValue(rVal))))
                .flatMap(e -> rewrite(e, lookup));
        } else if (right.left instanceof Variable && right.right instanceof Value) {
            Interval32Box val = ((Value)right.right).number;
            return Stream.of(TADR.newLeExpr(expr.left, TADR.newAddExpr(right.left, TADR.newValue(Interval32Box.subtract(val, Interval32Box.of(1))))))
                .flatMap(e -> leRule.rewrite(e, lookup));
        } else if (right.left instanceof Variable && right.right instanceof NegOp) {
            NegOp r = (NegOp)right.right;
            Interval32Box val = ((Value)r.expr).number.copy().negate();
            return Stream.of(TADR.newLeExpr(expr.left, TADR.newAddExpr(right.left, TADR.newValue(Interval32Box.subtract(val, Interval32Box.of(1))))))
                .flatMap(e -> leRule.rewrite(e, lookup));
        } else if (right.left instanceof Value && right.right instanceof Variable) {
            Interval32Box val = ((Value)right.left).number;
            return Stream.of(TADR.newLeExpr(expr.left, TADR.newAddExpr(TADR.newValue(Interval32Box.subtract(val, Interval32Box.of(1))), right.right)))
                .flatMap(e -> leRule.rewrite(e, lookup));
        } else if (right.left instanceof NegOp && right.right instanceof Variable) {
            NegOp l = (NegOp)right.left;
            Interval32Box val = ((Value)l.expr).number.copy().negate();
            return Stream.of(TADR.newLeExpr(expr.left, TADR.newAddExpr(TADR.newValue(Interval32Box.subtract(val, Interval32Box.of(1))), right.right)))
                .flatMap(e -> leRule.rewrite(e, lookup));
        } else {
            return Stream.of(expr);
        }
    }

    @Override
    public String toString() {
        return "Octagon < +";
    }
}
