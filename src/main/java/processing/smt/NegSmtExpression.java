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

    public Value getValue() {
        return Grimp.v().newNegExpr(this.expr.getValue());
    }

    public Map<Local, Set<Local>> getConnectedVariables() {
        return this.expr.getConnectedVariables();
    }

    public Map<Local, Set<Local>> getReachableVariables() {
        return this.expr.getReachableVariables();
    }

    public Optional<Value> getValue(Local id) {
        return this.expr.getValue(id).map(v -> Grimp.v().newNegExpr(v));
    }

    public Optional<Value> getValue(Set<Local> variables) {
        return this.expr.getValue(variables).map(v -> Grimp.v().newNegExpr(v));
    }

    public Optional<Value> getReachableValue(Set<Local> sources) {
        return this.expr.getReachableValue(sources).map(v -> Grimp.v().newNegExpr(v));
    }

    public String toSmt2() {
        return String.format("(- %s)", this.expr.toSmt2());
    }

    public boolean containsAll(Set<Local> variables) {
        return this.expr.containsAll(variables);
    }
}
