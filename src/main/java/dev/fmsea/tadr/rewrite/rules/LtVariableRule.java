package dev.fmsea.tadr.rewrite.rules;

import java.util.function.Function;
import java.util.stream.Stream;

import dev.fmsea.absint.scalar.state.Interval32Box;
import dev.fmsea.tadr.LtCmp;
import dev.fmsea.tadr.TADR;
import dev.fmsea.tadr.Variable;
import soot.Local;

public class LtVariableRule extends RewriteRule {
    public boolean canRewrite(TADR expr) {
        return ((expr instanceof LtCmp) &&
                (((LtCmp)expr).left instanceof Variable) &&
                (((LtCmp)expr).right instanceof Variable));
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        return rewrite((LtCmp)expr, lookup);
    }

    public Stream<TADR> rewrite(LtCmp expr, Function<Local, Interval32Box> lookup) {
        return Stream.of(TADR.newLeExpr(expr.left, TADR.newSubExpr(expr.right, TADR.newValue(1))));
    }

    @Override
    public String toString() {
        return "< x";
    }
}
