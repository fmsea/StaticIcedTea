package tadr.rewrite.rules;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import tadr.TADR;
import tadr.GtCmp;
import tadr.Value;
import tadr.Variable;

import abstractinterp.scalar.state.Interval32Box;

import soot.Local;

public class GtVariableRule extends RewriteRule {
    public boolean canRewrite(TADR expr) {
        return ((expr instanceof GtCmp) &&
                (((GtCmp)expr).left instanceof Variable) &&
                (((GtCmp)expr).right instanceof Variable));
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        return rewrite((GtCmp)expr, lookup);
    }

    public Stream<TADR> rewrite(GtCmp expr, Function<Local, Interval32Box> lookup) {
        return Stream.of(TADR.newLeExpr(expr.right, TADR.newSubExpr(expr.left, TADR.newValue(1))));
    }

    @Override
    public String toString() {
        return "> x";
    }
}
