package dev.fmsea.tadr.rewrite.rules;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import dev.fmsea.tadr.TADR;
import dev.fmsea.tadr.GeCmp;
import dev.fmsea.tadr.NegOp;
import dev.fmsea.tadr.Value;
import dev.fmsea.tadr.Variable;

import dev.fmsea.absint.scalar.state.Interval32Box;

import soot.Local;

public class GeNegValueRule extends RewriteRule {
    public boolean canRewrite(TADR expr) {
        return ((expr instanceof GeCmp) &&
                (((GeCmp)expr).left instanceof Variable) &&
                (((GeCmp)expr).right instanceof NegOp) &&
                ((((NegOp)((GeCmp)expr).right).expr instanceof Value)));
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        return rewrite((GeCmp)expr, lookup);
    }

    public Stream<TADR> rewrite(GeCmp expr, Function<Local, Interval32Box> lookup) {
        Interval32Box val = ((Value)((NegOp)expr.right).expr).number;
        return Stream.of(TADR.newLeExpr(expr.left, TADR.newValue(val.negate())));
    }

    @Override
    public String toString() {
        return "(- ≥)";
    }
}
