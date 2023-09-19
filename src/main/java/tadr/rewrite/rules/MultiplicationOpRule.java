package tadr.rewrite.rules;

import java.util.function.Function;
import java.util.stream.Stream;
import tadr.TADR;
import tadr.MultiplicationOp;
import tadr.NegOp;
import tadr.Value;
import tadr.Variable;

import abstractinterp.scalar.state.Interval32Box;

import soot.Local;

public class MultiplicationOpRule extends RewriteRule {

    private final IntervalFolder folder = new IntervalFolder();

    public boolean canRewrite(TADR expr) {
        return (expr instanceof MultiplicationOp);
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        return rewrite((MultiplicationOp)expr, lookup);
    }

    public Stream<TADR> rewrite(MultiplicationOp expr, Function<Local, Interval32Box> lookup) {
        if (expr.left instanceof Variable && expr.right instanceof Variable) {
            Local l = ((Variable)expr.left).variable;
            Local r = ((Variable)expr.right).variable;
            Interval32Box lVal = lookup.apply(l);
            Interval32Box rVal = lookup.apply(r);
            return Stream.of(
                TADR.newMulExpr(TADR.newValue(lVal), expr.right),
                TADR.newMulExpr(expr.left, TADR.newValue(rVal)))
                .flatMap(e -> rewrite(e, lookup));
        } else if (expr.left instanceof Variable && expr.right instanceof Value) {
            Local l = ((Variable)expr.left).variable;
            Interval32Box val = ((Value)expr.right).number;
            Interval32Box lVal = lookup.apply(l);
            return Stream.of(
                TADR.newValue(Interval32Box.multiply(lVal, val)),
                TADR.newAddExpr(expr.left,
                    TADR.newValue(Interval32Box.multiply(lVal,
                        Interval32Box.subtract(val, Interval32Box.of(1))))));
        } else if (expr.left instanceof Value && expr.right instanceof Variable) {
            Interval32Box val = ((Value)expr.left).number;
            Local r = ((Variable)expr.right).variable;
            Interval32Box rVal = lookup.apply(r);
            return Stream.of(
                TADR.newValue(Interval32Box.multiply(val, rVal)),
                TADR.newAddExpr(TADR.newValue(Interval32Box.multiply(Interval32Box.subtract(val, Interval32Box.of(1)), rVal)),
                    expr.right));
        } else if (expr.left instanceof Variable && expr.right instanceof NegOp) {
            Local l = ((Variable)expr.left).variable;
            Interval32Box val = ((Value)((NegOp)expr.right).expr).number;
            Interval32Box negated = Interval32Box.negate(val);
            Interval32Box lVal = lookup.apply(l);
            return Stream.of(
                TADR.newValue(Interval32Box.multiply(lVal, negated)),
                TADR.newSubExpr(expr.left,
                    TADR.newValue(Interval32Box.multiply(lVal, Interval32Box.subtract(val, Interval32Box.of(1))))));
        } else if (expr.left instanceof NegOp && expr.right instanceof Variable) {
            Interval32Box val = ((Value)((NegOp)expr.left).expr).number.copy().negate();
            Local r = ((Variable)expr.right).variable;
            Interval32Box rVal = lookup.apply(r);
            return Stream.of(
                TADR.newValue(Interval32Box.multiply(val, rVal)),
                TADR.newSubExpr(TADR.newValue(Interval32Box.multiply(Interval32Box.add(val, Interval32Box.of(1)), rVal)),
                    expr.right));
        } else {
            return Stream.of(expr.accept(folder));
        }
    }

    @Override
    public String toString() {
        return "×";
    }
}
