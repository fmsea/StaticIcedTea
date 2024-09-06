package dev.fmsea.tadr.rewrite.rules;

import java.util.function.Function;
import java.util.stream.Stream;
import dev.fmsea.tadr.TADR;
import dev.fmsea.tadr.DivisionOp;
import dev.fmsea.tadr.NegOp;
import dev.fmsea.tadr.Value;
import dev.fmsea.tadr.Variable;

import dev.fmsea.absint.scalar.state.Interval32Box;

import soot.Local;

public class DivisionOpRule extends RewriteRule {

    private final IntervalFolder folder = new IntervalFolder();

    public boolean canRewrite(TADR expr) {
        return (expr instanceof DivisionOp);
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        return rewrite((DivisionOp)expr, lookup);
    }

    public Stream<TADR> rewrite(DivisionOp expr, Function<Local, Interval32Box> lookup) {
        if (expr.left instanceof Variable && expr.right instanceof Variable) {
            Local l = ((Variable)expr.left).variable;
            Local r = ((Variable)expr.right).variable;
            Interval32Box lVal = lookup.apply(l);
            Interval32Box rVal = lookup.apply(r);
            return Stream.of(TADR.newValue(Interval32Box.divide(lVal, rVal)));
        } else if (expr.left instanceof Variable && expr.right instanceof Value) {
            Local l = ((Variable)expr.left).variable;
            Interval32Box val = ((Value)expr.right).number;
            Interval32Box lVal = lookup.apply(l);
            return Stream.of(TADR.newValue(Interval32Box.divide(lVal, val)));
        } else if (expr.left instanceof Value && expr.right instanceof Variable) {
            Interval32Box val = ((Value)expr.left).number;
            Local r = ((Variable)expr.right).variable;
            Interval32Box rVal = lookup.apply(r);
            return Stream.of(TADR.newValue(Interval32Box.divide(val, rVal)));
        } else if (expr.left instanceof Variable && expr.right instanceof NegOp) {
            NegOp right = (NegOp)expr.right;
            Interval32Box val = ((Value)right.expr).number.copy().negate();
            Local l = ((Variable)expr.left).variable;
            Interval32Box lVal = lookup.apply(l);
            return Stream.of(TADR.newValue(Interval32Box.divide(lVal, val)));
        } else if (expr.left instanceof NegOp && expr.right instanceof Variable) {
            NegOp left = (NegOp)expr.left;
            Interval32Box val = ((Value)left.expr).number.copy().negate();
            Local r = ((Variable)expr.right).variable;
            Interval32Box rVal = lookup.apply(r);
            return Stream.of(TADR.newValue(Interval32Box.divide(val, rVal)));
        } else {
            return Stream.of(expr.accept(folder));
        }
    }

    @Override
    public String toString() {
        return "÷";
    }
}
