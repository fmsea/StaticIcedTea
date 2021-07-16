package abstractinterp.scalar.state;

import soot.Local;
import soot.Value;

import solver.SolverWrapper;

public interface State {
    public State copy();
    public boolean isFeasible();
    public void copyTo(State dest);
    // public void update(Local l, B b);
    // public B getValue(Local local);
    public void mergeWith(State inState);
    public void widenWith(State prevBeforeFlow);
    public void updateState(Local var, State inState, Value left, Value right, BinaryOperator operator);
    public void updateState(Local var, State inState, Value v);
    public String toSMT(SolverWrapper solver);
    public String toSMT(Local l, SolverWrapper solver);
    public void updateTop(Local local);
    public boolean updateCond(State inState, Value left, Value right, PredicateType type);
    public void makeInfeasible();
}
