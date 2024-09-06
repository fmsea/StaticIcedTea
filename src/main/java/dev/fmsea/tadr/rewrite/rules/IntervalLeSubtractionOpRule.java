package dev.fmsea.tadr.rewrite.rules;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import dev.fmsea.tadr.TADR;
import dev.fmsea.tadr.LeCmp;
import dev.fmsea.tadr.SubtractionOp;
import dev.fmsea.tadr.NegOp;
import dev.fmsea.tadr.Value;
import dev.fmsea.tadr.Variable;
import dev.fmsea.tadr.visitors.VariableVisitor;

import dev.fmsea.absint.scalar.state.Interval32Box;

import soot.Local;

public class IntervalLeSubtractionOpRule extends RewriteRule {

    private final VariableVisitor variableQuery = new VariableVisitor();
    private final IntervalSubtractionOpRule sub = new IntervalSubtractionOpRule();

    public boolean canRewrite(TADR expr) {
        if ((expr instanceof LeCmp) &&
            (((LeCmp)expr).left instanceof Variable) &&
            (((LeCmp)expr).right instanceof SubtractionOp)) {
            var le = (LeCmp)expr;
            Set<Local> left = le.left.accept(variableQuery);
            Set<Local> right = le.right.accept(variableQuery);
            return right.isEmpty() || !left.containsAll(right);
        } else {
            return false;
        }
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        return rewrite((LeCmp)expr, lookup);
    }

    public Stream<TADR> rewrite(LeCmp expr, Function<Local, Interval32Box> lookup) {
        var right = (SubtractionOp)expr.right;
        return sub.rewrite(right, lookup).map(e -> TADR.newLeExpr(expr.left, e));
    }

    @Override
    public String toString() {
        return "Interval ≤ -";
    }
}
