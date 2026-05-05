package dev.fmsea.tadr.rewrite.rules;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import dev.fmsea.absint.scalar.state.Interval32Box;
import dev.fmsea.tadr.AdditionOp;
import dev.fmsea.tadr.EqCmp;
import dev.fmsea.tadr.MultiplicationOp;
import dev.fmsea.tadr.SubtractionOp;
import dev.fmsea.tadr.TADR;
import dev.fmsea.tadr.Value;
import dev.fmsea.tadr.Variable;
import dev.fmsea.tadr.visitors.ReassignmentVisitor;
import soot.Local;

public class OctagonReassignmentEqRewriteRule extends RewriteRule {

    private final MultiplicationOpRule multRewriter = new MultiplicationOpRule();
    private final ReassignmentVisitor reassignmentQuery = new ReassignmentVisitor();

    public boolean canRewrite(TADR expr) {
        if (expr instanceof EqCmp) {
            EqCmp eq = (EqCmp)expr;
            return eq.accept(reassignmentQuery);
        } else {
            return false;
        }
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        return rewrite((EqCmp)expr, lookup);
    }

    public Stream<TADR> rewrite(EqCmp expr, Function<Local, Interval32Box> lookup) {
        Optional<Variable> variable = Optional.empty();
        Optional<Value> value = Optional.empty();
        if (expr.left instanceof Variable && expr.right instanceof AdditionOp) {
            variable = Optional.of((Variable)expr.left);
            AdditionOp addOp = (AdditionOp)expr.right;
            if (addOp.left instanceof Variable && addOp.right instanceof Value) {
                value = Optional.of((Value)addOp.right);
            } else if (addOp.left instanceof Value && addOp.right instanceof Variable) {
                value = Optional.of((Value)addOp.left);
            }
        } else if (expr.left instanceof Variable && expr.right instanceof SubtractionOp) {
            variable = Optional.of((Variable)expr.left);
            SubtractionOp subOp = (SubtractionOp)expr.right;
            if (subOp.left instanceof Variable && subOp.right instanceof Value) {
                value = Optional.of((Value)subOp.right)
                    .map(o -> TADR.newValue(o.number.negate()));
            } else if (subOp.left instanceof Value && subOp.right instanceof Variable) {
                value = Optional.of((Value)subOp.left);
            }
        } else if (expr.left instanceof Variable && expr.right instanceof MultiplicationOp) {
            MultiplicationOp multExpr = (MultiplicationOp)expr.right;
            return multRewriter.rewrite(multExpr, lookup)
                .filter(e -> e instanceof AdditionOp || e instanceof SubtractionOp)
                .flatMap(sum -> rewrite(TADR.newEqExpr(expr.left, sum), lookup));
        }
        Variable varPrime = variable.get();
        return value.map(o -> Stream.<TADR>of(TADR.newReassignment(varPrime, o)))
                .orElse(Stream.<TADR>of())
            .peek(ts -> LOGGER.debug("Reassignment Rewriter rewrote {} -> {}", expr, ts));
    }

    @Override
    public String toString() {
        return "(reassignment) Octagon = Rewrite Rule";
    }
}
