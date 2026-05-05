package dev.fmsea.absint.scalar.state;

public abstract class ConstraintThunk implements Comparable<ConstraintThunk> {
    public interface Visitor<R> {
        R visit(ConstraintUpdateThunk thunk, OctagonDifferenceBoundedMatrix m, OctagonDifferenceBoundedMatrix in);
        R visit(ConstraintForgetThunk thunk, OctagonDifferenceBoundedMatrix m, OctagonDifferenceBoundedMatrix in);
        R visit(ConstraintInplaceThunk thunk, OctagonDifferenceBoundedMatrix m, OctagonDifferenceBoundedMatrix in);
    }

    public abstract <R> R accept(Visitor<R> visitor, OctagonDifferenceBoundedMatrix m, OctagonDifferenceBoundedMatrix in);
}
