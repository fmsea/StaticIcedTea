package tadr.rewrite.rules;

import java.util.function.Function;
import java.util.stream.Stream;

import abstractinterp.scalar.state.Interval32Box;
import soot.Local;
import tadr.AdditionOp;
import tadr.GeCmp;
import tadr.TADR;
import tadr.Value;
import tadr.Variable;

public class OctagonReassignmentGeAdditionRule extends RewriteRule {

    private final Variable variable;

    public OctagonReassignmentGeAdditionRule(Variable variable) {
        this.variable = variable;
    }

    public boolean canRewrite(TADR expr) {
        return ((expr instanceof GeCmp) &&
                (((GeCmp)expr).left instanceof Variable) &&
                (((GeCmp)expr).right instanceof AdditionOp));
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        return rewrite((GeCmp)expr, lookup);
    }

    public Stream<TADR> rewrite(GeCmp expr, Function<Local, Interval32Box> lookup) {
        Local s = ((Variable)expr.left).variable;
        var right = (AdditionOp)expr.right;
        if (right.left instanceof Variable && right.right instanceof Variable) {
            Local l = ((Variable)right.left).variable;
            Local r = ((Variable)right.right).variable;
            Interval32Box lVal = lookup.apply(l);
            Interval32Box rVal = lookup.apply(r);
            return Stream.of(
                TADR.newGeExpr(expr.left, TADR.newAddExpr(TADR.newValue(lVal), right.right)),
                TADR.newGeExpr(expr.left, TADR.newAddExpr(right.left, TADR.newValue(rVal))))
                .flatMap(e -> rewrite(e, lookup));
        } else if (right.left instanceof Variable && right.right instanceof Value) {
            Variable l = (Variable)right.left;
            if (this.variable.equals(l)) {
                return Stream.of();
            } else {
                return Stream.of(TADR.newLeExpr(right.left, TADR.newSubExpr(expr.left, right.right)));
            }
        } else if (right.left instanceof Value && right.right instanceof Variable) {
            Variable r = (Variable)right.right;
            if (this.variable.equals(r)) {
                return Stream.of();
            } else {
                return Stream.of(TADR.newLeExpr(right.right, TADR.newSubExpr(expr.left, right.left)));
            }
        }
        return Stream.of(expr);
    }

    @Override
    public String toString() {
        return "(reassignment) Octagon ≥ +";
    }
}
