package dev.fmsea.tadr.rewrite.rules;

import java.util.function.Function;
import java.util.stream.Stream;
import dev.fmsea.tadr.TADR;
import dev.fmsea.tadr.LeCmp;
import dev.fmsea.tadr.DivisionOp;
import dev.fmsea.tadr.NegOp;
import dev.fmsea.tadr.Value;
import dev.fmsea.tadr.Variable;

import dev.fmsea.absint.scalar.state.Interval32Box;

import soot.Local;

public class LeDivisionOpRule extends RewriteRule {

    private final DivisionOpRule div = new DivisionOpRule();

    public boolean canRewrite(TADR expr) {
        return ((expr instanceof LeCmp) &&
                (((LeCmp)expr).left instanceof Variable) &&
                (((LeCmp)expr).right instanceof DivisionOp));
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        return rewrite((LeCmp)expr, lookup);
    }

    public Stream<TADR> rewrite(LeCmp expr, Function<Local, Interval32Box> lookup) {
        var right = (DivisionOp)expr.right;
        return div.rewrite(right, lookup).map(e -> TADR.newLeExpr(expr.left, e));
    }

    @Override
    public String toString() {
        return "≤ ÷";
    }
}
