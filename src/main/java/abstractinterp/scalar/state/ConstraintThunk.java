package abstractinterp.scalar.state;

public abstract class ConstraintThunk implements Comparable<ConstraintThunk> {
    public interface Visitor<R> {
        R visitThunk(ConstraintUpdateThunk thunk, OctagonDifferenceBoundedMatrix m, OctagonDifferenceBoundedMatrix in);
        R visitForgetThunk(ConstraintForgetThunk thunk, OctagonDifferenceBoundedMatrix m, OctagonDifferenceBoundedMatrix in);
        R visitInplaceThunk(ConstraintInplaceThunk thunk, OctagonDifferenceBoundedMatrix m, OctagonDifferenceBoundedMatrix in);
    }

    public abstract <R> R accept(Visitor<R> visitor, OctagonDifferenceBoundedMatrix m, OctagonDifferenceBoundedMatrix in);
}
