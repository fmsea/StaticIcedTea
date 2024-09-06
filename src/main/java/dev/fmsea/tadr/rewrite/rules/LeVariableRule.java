package dev.fmsea.tadr.rewrite.rules;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import dev.fmsea.tadr.TADR;
import dev.fmsea.tadr.LeCmp;
import dev.fmsea.tadr.Value;
import dev.fmsea.tadr.Variable;

import dev.fmsea.absint.scalar.state.Interval32Box;

import soot.Local;

public class LeVariableRule extends RewriteRule {
    public boolean canRewrite(TADR expr) {
        return ((expr instanceof LeCmp) &&
                (((LeCmp)expr).left instanceof Variable) &&
                (((LeCmp)expr).right instanceof Variable));
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        return rewrite((LeCmp)expr, lookup);
    }

    public Stream<TADR> rewrite(LeCmp expr, Function<Local, Interval32Box> lookup) {
        Local l = ((Variable)expr.left).variable;
        Local r = ((Variable)expr.right).variable;
        Interval32Box lVal = lookup.apply(l);
        Interval32Box rVal = lookup.apply(r);
        return Stream.of(
            TADR.newLeExpr(expr.left, TADR.newValue(rVal)),
            TADR.newGeExpr(expr.right, TADR.newValue(lVal)));
    }

    @Override
    public String toString() {
        return "≤ x";
    }
}
