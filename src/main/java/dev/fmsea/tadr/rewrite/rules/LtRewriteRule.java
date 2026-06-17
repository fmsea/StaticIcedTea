package dev.fmsea.tadr.rewrite.rules;

import java.util.function.Function;
import java.util.stream.Stream;

import dev.fmsea.absint.scalar.state.Interval32Box;
import dev.fmsea.tadr.LtCmp;
import dev.fmsea.tadr.TADR;
import dev.fmsea.tadr.Value;
import dev.fmsea.tadr.Variable;
import soot.Local;

public class LtRewriteRule extends RewriteRule {

    public boolean canRewrite(TADR expr) {
        if (expr instanceof LtCmp) {
            LtCmp cmp = (LtCmp)expr;
            return ((cmp.left instanceof Variable && cmp.right instanceof Value) ||
                    (cmp.left instanceof Value && cmp.right instanceof Variable));
        }
        return false;
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        if (expr instanceof LtCmp) {
            return rewrite((LtCmp)expr, lookup);
        } else {
            return Stream.of();
        }
    }

    protected Stream<TADR> rewrite(LtCmp expr, Function<Local, Interval32Box> lookup) {
        if (expr.left instanceof Variable && expr.right instanceof Value) {
            var val = ((Value)expr.right).number;
            return Stream.of(
                TADR.newLeExpr(expr.left, TADR.newValue(Interval32Box.subtract(val, Interval32Box.of(1))))
            );
        } else if (expr.left instanceof Value && expr.right instanceof Variable) {
            var val = ((Value)expr.left).number;
            return Stream.of(
                TADR.newGeExpr(expr.right, TADR.newValue(Interval32Box.subtract(val, Interval32Box.of(1))))
            );
        } else {
            return Stream.of();
        }
    }
}
