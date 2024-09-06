package dev.fmsea.tadr.rewrite.rules;

import java.util.function.Function;
import java.util.stream.Stream;

import dev.fmsea.absint.scalar.state.Interval32Box;
import soot.Local;
import dev.fmsea.tadr.GtCmp;
import dev.fmsea.tadr.NegOp;
import dev.fmsea.tadr.SubtractionOp;
import dev.fmsea.tadr.TADR;
import dev.fmsea.tadr.Value;
import dev.fmsea.tadr.Variable;

public class OctagonGtSubtractionRule extends RewriteRule {

    private final OctagonLtRewriteRule ltRule = new OctagonLtRewriteRule();
    private final OctagonGeRewriteRule geRule = new OctagonGeRewriteRule();

    public boolean canRewrite(TADR expr) {
        return ((expr instanceof GtCmp) &&
                (((GtCmp)expr).left instanceof Variable) &&
                (((GtCmp)expr).right instanceof SubtractionOp));
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        return rewrite((GtCmp)expr, lookup);
    }

    public Stream<TADR> rewrite(GtCmp expr, Function<Local, Interval32Box> lookup) {
        var right = (SubtractionOp)expr.right;
        if (right.left instanceof Variable && right.right instanceof Variable) {
            Local l = ((Variable)right.left).variable;
            Local r = ((Variable)right.right).variable;
            Interval32Box lVal = lookup.apply(l);
            Interval32Box rVal = lookup.apply(r);
            return Stream.of(
                TADR.newGtExpr(expr.left, TADR.newSubExpr(TADR.newValue(lVal), right.right)),
                TADR.newGtExpr(expr.left, TADR.newSubExpr(right.left, TADR.newValue(rVal))))
                .flatMap(e -> rewrite(e, lookup));
        } else if (right.left instanceof Variable && right.right instanceof Value) {
            return Stream.of(TADR.newLtExpr(right.left, TADR.newAddExpr(expr.left, right.right)))
                .flatMap(e -> ltRule.rewrite(e, lookup));
        } else if (right.left instanceof Variable && right.right instanceof NegOp) {
            NegOp r = (NegOp)right.right;
            return Stream.of(TADR.newLtExpr(right.left, TADR.newSubExpr(expr.left, r.expr)))
                .flatMap(e -> ltRule.rewrite(e, lookup));
        } else if (right.left instanceof Value && right.right instanceof Variable) {
            Interval32Box val = ((Value)right.left).number;
            return Stream.of(TADR.newGeExpr(expr.left,
                TADR.newSubExpr(TADR.newValue(Interval32Box.add(val, Interval32Box.of(1))),
                    right.right)))
                .flatMap(e -> geRule.rewrite(e, lookup));
        }
        return Stream.of(expr);
    }

    @Override
    public String toString() {
        return "Octagon > -";
    }
}
