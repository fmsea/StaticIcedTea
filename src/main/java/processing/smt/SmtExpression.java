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

    /** Return Value which is reachable from the local `id`.
     *
     * This should mimic getValue, except instead of "contains", we are looking
     * to see if `source` is in the first location of the BinOp expression, for
     * example.
     *
     * If the expression does not contain reachable expressions, the result is
     * empty.
     */
    public Optional<Value> getReachableValue(Local source) {
        return this.getReachableValue(Set.of(source));
    }

    /** Return value which is reachable from the local sources.
     *
     * This is essentially a union of expressions over the singular version.
     */
    public abstract Optional<Value> getReachableValue(Set<Local> sources);

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

    public Optional<String> toReachableSmt2(Local id) {
        return this.toReachableSmt2(Set.of(id));
    }

    public Optional<String> toReachableSmt2(Set<Local> sources) {
        return this.getReachableValue(sources).map(v -> this.solver.smt2(v));
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

    public abstract Map<Local, Set<Local>> getReachableVariables();

    public boolean contains(Local identifier) {
        return ValueToMap.getLocals(this.getValue()).contains(identifier);
    }
}
