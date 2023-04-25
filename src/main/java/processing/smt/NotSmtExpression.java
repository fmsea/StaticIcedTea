package processing.smt;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import soot.Local;
import soot.Value;
import soot.grimp.Grimp;
import soot.jimple.IntConstant;
import soot.dava.internal.javaRep.DNotExpr;

public class NotSmtExpression extends SmtExpression {

    private SmtExpression expr;

    public NotSmtExpression(SmtExpression expr) {
        super();
        this.expr = expr;
    }

    public Set<Local> getLocals() {
        return this.expr.getLocals();
    }

    public Value getValue() {
        return new DNotExpr(this.expr.getValue());
    }

    public Optional<Value> getValue(Set<Local> variables) {
        return Optional.empty();
    }

    public Optional<Value> getConnectedValue(Local id) {
        return this.expr.getConnectedValue(id)
            .map(v -> Grimp.v().newNeExpr(v, IntConstant.v(1)));
    }

    public Optional<Value> getConnectedValue(Set<Local> variables) {
        return this.expr.getConnectedValue(variables)
            .map(v -> Grimp.v().newNeExpr(v, IntConstant.v(1)));
    }

    public Optional<Value> getReachableValue(Set<Local> sources) {
        return this.expr.getReachableValue(sources)
            .map(v -> Grimp.v().newNeExpr(v, IntConstant.v(1)));
    }

    public String toSmt2() {
        return String.format("(not %s)", this.expr.toSmt2());
    }

    public Optional<String> toSmt2(Set<Local> variables) {
        return this.expr.toSmt2(variables).map(smt -> String.format("(not %s)", smt));
    }

    public boolean containsAll(Set<Local> variables) {
        return this.expr.containsAll(variables);
    }

    public int getPredicateCount() {
        return this.expr.getPredicateCount();
    }

    public SmtGraph toGraph() {
        return this.expr.toGraph();
    }
}
