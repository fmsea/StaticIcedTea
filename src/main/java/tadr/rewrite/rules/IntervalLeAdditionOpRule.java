package tadr.rewrite.rules;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import abstractinterp.scalar.state.Interval32Box;
import soot.Local;
import tadr.AdditionOp;
import tadr.LeCmp;
import tadr.TADR;
import tadr.Variable;
import tadr.visitors.VariableVisitor;

public class IntervalLeAdditionOpRule extends RewriteRule {

    private final VariableVisitor variableQuery = new VariableVisitor();
    private final IntervalAdditionOpRule add = new IntervalAdditionOpRule();

    public boolean canRewrite(TADR expr) {
        if ((expr instanceof LeCmp) &&
            (((LeCmp)expr).left instanceof Variable) &&
            (((LeCmp)expr).right instanceof AdditionOp)) {
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
        var right = (AdditionOp)expr.right;
        return add.rewrite(right, lookup).map(e -> TADR.newLeExpr(expr.left, e));
    }

    @Override
    public String toString() {
        return "Interval ≤ +";
    }
}
