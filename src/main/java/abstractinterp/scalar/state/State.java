package abstractinterp.scalar.state;

import java.util.Optional;
import soot.jimple.BinopExpr;
import soot.Local;
import soot.Value;
import org.jgrapht.Graph;

import solver.SolverWrapper;

public interface State {
    public State copy();
    public boolean isFeasible();
    public void copyTo(State dest);
    public void mergeWith(State inState);

    /** Widen flows according to Cousot and Miné definition
     *
     * Widening definition
     * m_ij ▽ n_ij = { m_ij if n_ij ≤ m_ij else +∞ }
     * where `this` is `m` and `newFlow` is `n`.
     *
     * Definition can be found in §4 of mine-pado-2001.
     * http://dx.doi.org/10.1007/3-540-44978-7_10
     */
    public void widenWith(State newFlow);
    public boolean isSubset(State inState);
    public void updateState(Local var, State inState, Value left, Value right, BinaryOperatorType operator);
    public void updateState(Local var, State inState, Value v);
    public Optional<BinopExpr> toBinop();
    public String toSMT(SolverWrapper solver);
    public String toSMT(Local l, SolverWrapper solver);
    public Graph<Local, ZoneConstraint> toGraph();
    public void updateTop(Local local);
    public boolean updateCond(State inState, Value left, Value right, PredicateType type);
    public void forget(Local local);
    public void makeInfeasible();
}
