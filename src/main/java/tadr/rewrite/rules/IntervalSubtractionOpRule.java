package tadr.rewrite.rules;

import java.util.function.Function;
import java.util.stream.Stream;

import abstractinterp.scalar.state.Interval32Box;
import soot.Local;
import tadr.SubtractionOp;
import tadr.TADR;
import tadr.Variable;

public class IntervalSubtractionOpRule extends RewriteRule {

    private final IntervalFolder folder = new IntervalFolder();

    public boolean canRewrite(TADR expr) {
        return (expr instanceof SubtractionOp);
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        return rewrite((SubtractionOp)expr, lookup);
    }

    public Stream<TADR> rewrite(SubtractionOp expr, Function<Local, Interval32Box> lookup) {
        if (expr.left instanceof Variable) {
            Local l = ((Variable)expr.left).variable;
            Interval32Box val = lookup.apply(l);
            return Stream.of(TADR.newSubExpr(TADR.newValue(val), expr.right))
                .flatMap(e -> rewrite(e, lookup));
        } else if (expr.right instanceof Variable) {
            Local r = ((Variable)expr.right).variable;
            Interval32Box val = lookup.apply(r);
            return Stream.of(TADR.newSubExpr(expr.left, TADR.newValue(val)))
                .flatMap(e -> rewrite(e, lookup));
        } else {
            return Stream.of(expr.accept(folder));
        }
    }

    @Override
    public String toString() {
        return "Interval -";
    }
}
