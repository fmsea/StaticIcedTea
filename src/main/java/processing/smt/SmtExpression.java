package processing.smt;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import soot.Local;
import soot.Value;

import solver.SolverWrapper;
import solver.SolverFactory;

public abstract class SmtExpression {

    protected SolverWrapper solver;

    public SmtExpression() {
        this.solver = SolverFactory.getSolver();
    }

    public abstract Value getValue();

    /** Return Value which are connected to the local `id`.
     *
     * If `id` is not in the expression, then result shall be empty.
     */
    public abstract Optional<Value> getValue(Local id);

    public abstract Optional<Value> getValue(Set<Local> variables);

    public Set<Local> getLocals() {
        return ValueToMap.getLocals(this.getValue());
    }

    public Set<Local> getLocals(Local id) {
        return this.getConnectedVariables().get(id);
    }

    @Override
    public String toString() {
        return this.getValue().toString();
    }

    public abstract String toSmt2();

    public Optional<String> toSmt2(Local id) {
        return this.getValue(id).map(v -> this.solver.smt2(v));
    }

    public Optional<String> toSmt2(Set<Local> variables) {
        return this.getValue(variables).map(v -> this.solver.smt2(v));
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SmtExpression) {
            return this.equals((SmtExpression) o);
        } else {
            return false;
        }
    }

    public boolean equals(SmtExpression o) {
        return this.getValue().equivTo(o.getValue());
    }

    public abstract Map<Local, Set<Local>> getConnectedVariables();

    public boolean contains(Local identifier) {
        return ValueToMap.getLocals(this.getValue()).contains(identifier);
    }
}
