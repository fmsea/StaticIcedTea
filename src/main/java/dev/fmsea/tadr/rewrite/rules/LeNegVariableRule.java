package dev.fmsea.tadr.rewrite.rules;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import dev.fmsea.tadr.TADR;
import dev.fmsea.tadr.LeCmp;
import dev.fmsea.tadr.NegOp;
import dev.fmsea.tadr.Value;
import dev.fmsea.tadr.Variable;

import dev.fmsea.absint.scalar.state.Interval32Box;

import soot.Local;

public class LeNegVariableRule extends RewriteRule {
    public boolean canRewrite(TADR expr) {
        return ((expr instanceof LeCmp) &&
                (((LeCmp)expr).left instanceof Variable) &&
                (((LeCmp)expr).right instanceof NegOp) &&
                ((((NegOp)((LeCmp)expr).right).expr instanceof Variable)));
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        return rewrite((LeCmp)expr, lookup);
    }

    public Stream<TADR> rewrite(LeCmp expr, Function<Local, Interval32Box> lookup) {
        NegOp op = (NegOp)expr.right;
        Local l = ((Variable)expr.left).variable;
        Local r = ((Variable)op.expr).variable;
        Optional<Interval32Box> lVal = Optional.of(lookup.apply(l));
        Optional<Interval32Box> rVal = Optional.of(lookup.apply(r));
        return Stream.of(
            rVal.map(i -> i.negate())
                .filter(i -> !i.isTop())
                .map(i -> TADR.newLeExpr(expr.left, TADR.newValue(i))),
            lVal.map(i -> i.negate())
                .filter(i -> !i.isTop())
                .map(i -> TADR.newLeExpr(op.expr, TADR.newValue(i))))
            .filter(o -> o.isPresent())
            .map(o -> o.get());
    }

    @Override
    public String toString() {
        return "(- ≤ x)";
    }
}
