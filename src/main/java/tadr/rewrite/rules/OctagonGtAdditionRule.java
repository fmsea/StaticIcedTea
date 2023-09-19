package tadr.rewrite.rules;

import java.util.function.Function;
import java.util.stream.Stream;

import abstractinterp.scalar.state.Interval32Box;
import soot.Local;
import tadr.AdditionOp;
import tadr.GtCmp;
import tadr.NegOp;
import tadr.TADR;
import tadr.Value;
import tadr.Variable;

public class OctagonGtAdditionRule extends RewriteRule {

    private final OctagonGeRewriteRule geRule = new OctagonGeRewriteRule();

    public boolean canRewrite(TADR expr) {
        return ((expr instanceof GtCmp) &&
                (((GtCmp)expr).left instanceof Variable) &&
                (((GtCmp)expr).right instanceof AdditionOp));
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        return rewrite((GtCmp)expr, lookup);
    }

    public Stream<TADR> rewrite(GtCmp expr, Function<Local, Interval32Box> lookup) {
        var right = (AdditionOp)expr.right;
        if (right.left instanceof Variable && right.right instanceof Variable) {
            Local l = ((Variable)right.left).variable;
            Local r = ((Variable)right.right).variable;
            Interval32Box lVal = lookup.apply(l);
            Interval32Box rVal = lookup.apply(r);
            return Stream.of(
                TADR.newGtExpr(expr.left, TADR.newAddExpr(TADR.newValue(lVal), right.right)),
                TADR.newGtExpr(expr.left, TADR.newAddExpr(right.left, TADR.newValue(rVal))))
                .flatMap(e -> rewrite(e, lookup));
        } else if (right.left instanceof Variable && right.right instanceof Value) {
            Interval32Box val = ((Value)right.right).number;
            return Stream.of(TADR.newGeExpr(expr.left,
                TADR.newAddExpr(right.left,
                    TADR.newValue(Interval32Box.add(val, Interval32Box.of(1))))))
                .flatMap(e -> geRule.rewrite(e, lookup));
        } else if (right.left instanceof Variable && right.right instanceof NegOp) {
            NegOp r = (NegOp)right.right;
            Interval32Box val = ((Value)r.expr).number;
            return Stream.of(TADR.newGeExpr(expr.left,
                TADR.newSubExpr(right.left,
                    TADR.newValue(Interval32Box.add(val, Interval32Box.of(1))))))
                .flatMap(e -> geRule.rewrite(e, lookup));
        } else if (right.left instanceof Value && right.right instanceof Variable) {
            Interval32Box val = ((Value)right.left).number;
            return Stream.of(TADR.newGeExpr(expr.left, TADR.newAddExpr(TADR.newValue(Interval32Box.add(val, Interval32Box.of(1))),
                right.right))).flatMap(e -> geRule.rewrite(e, lookup));
        } else if (right.left instanceof NegOp && right.right instanceof Variable) {
            NegOp l = (NegOp)right.left;
            Interval32Box val = ((Value)l.expr).number.copy().negate();
            return Stream.of(TADR.newGeExpr(expr.left, TADR.newAddExpr(TADR.newValue(Interval32Box.add(val, Interval32Box.of(1))), right.right)))
                .flatMap(e -> geRule.rewrite(e, lookup));
        } else {
            return Stream.of(expr);
        }
    }

    @Override
    public String toString() {
        return "Octagon > +";
    }
}
