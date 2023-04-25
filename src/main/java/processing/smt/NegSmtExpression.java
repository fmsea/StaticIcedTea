package processing.smt;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import soot.Local;
import soot.Value;
import soot.grimp.Grimp;

public class NegSmtExpression extends SmtExpression {
    private SmtExpression expr;

    public NegSmtExpression(SmtExpression expr) {
        super();
        this.expr = expr;
    }

    public Set<Local> getLocals() {
        return this.expr.getLocals();
    }

    public Value getValue() {
        return Grimp.v().newNegExpr(this.expr.getValue());
    }

    public Optional<Value> getValue(Set<Local> variables) {
        return Optional.empty();
    }

    public Map<Local, Set<Local>> getConnectedVariables() {
        return this.expr.getConnectedVariables();
    }

    public Map<Local, Set<Local>> getReachableVariables() {
        return this.expr.getReachableVariables();
    }

    public Optional<Value> getConnectedValue(Local id) {
        return this.expr.getConnectedValue(id).map(v -> Grimp.v().newNegExpr(v));
    }

    public Optional<Value> getConnectedValue(Set<Local> variables) {
        return this.expr.getConnectedValue(variables).map(v -> Grimp.v().newNegExpr(v));
    }

    public Optional<Value> getReachableValue(Set<Local> sources) {
        return this.expr.getReachableValue(sources).map(v -> Grimp.v().newNegExpr(v));
    }

    public boolean containsAll(Set<Local> variables) {
        return this.expr.containsAll(variables);
    }

    public String toSmt2() {
        return String.format("(- %s)", this.expr.toSmt2());
    }

    public Optional<String> toSmt2(Set<Local> variables) {
        return this.expr.toSmt2(variables).map(smt -> String.format("(- %s)", smt));
    }

    public int getPredicateCount() {
        return this.expr.getPredicateCount();
    }

    public SmtGraph toGraph() {
        return this.expr.toGraph();
    }
}
