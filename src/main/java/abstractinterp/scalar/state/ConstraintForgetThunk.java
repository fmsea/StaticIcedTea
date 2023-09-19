package abstractinterp.scalar.state;

public class ConstraintForgetThunk extends ConstraintThunk {
    public final int i;
    public final int ibar;

    private ConstraintForgetThunk(int i) {
        this(i, i ^ 1);
    }

    private ConstraintForgetThunk(int i, int ibar) {
        this.i = i;
        this.ibar = ibar;
    }

    public <R> R accept(ConstraintThunk.Visitor<R> visitor, OctagonDifferenceBoundedMatrix m, OctagonDifferenceBoundedMatrix in) {
        return visitor.visitForgetThunk(this, m, in);
    }

    public static ConstraintForgetThunk of(int i) {
        return new ConstraintForgetThunk(i);
    }

    public static ConstraintForgetThunk of(int i, int ibar) {
        return new ConstraintForgetThunk(i, ibar);
    }

    @Override
    public int compareTo(ConstraintThunk o) {
        return -1;
    }

    @Override
    public String toString() {
        return String.format("forgetting [%d, %d]", this.i, this.ibar);
    }
}
