package dev.fmsea.tadr.rewrite.rules;

import java.util.function.Function;
import java.util.stream.Stream;
import dev.fmsea.tadr.TADR;
import dev.fmsea.tadr.GeCmp;
import dev.fmsea.tadr.MultiplicationOp;
import dev.fmsea.tadr.NegOp;
import dev.fmsea.tadr.Value;
import dev.fmsea.tadr.Variable;

import dev.fmsea.absint.scalar.state.Interval32Box;

import soot.Local;

public class GeMultiplicationOpRule extends RewriteRule {

    private final MultiplicationOpRule mul = new MultiplicationOpRule();

    public boolean canRewrite(TADR expr) {
        return ((expr instanceof GeCmp) &&
                (((GeCmp)expr).left instanceof Variable) &&
                (((GeCmp)expr).right instanceof MultiplicationOp));
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        return rewrite((GeCmp)expr, lookup);
    }

    public Stream<TADR> rewrite(GeCmp expr, Function<Local, Interval32Box> lookup) {
        var right = (MultiplicationOp)expr.right;
        return mul.rewrite(right, lookup).map(e -> TADR.newGeExpr(expr.left, e));
    }

    @Override
    public String toString() {
        return "≥ ×";
    }
}
