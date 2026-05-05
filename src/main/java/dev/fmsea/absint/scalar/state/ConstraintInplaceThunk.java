package dev.fmsea.absint.scalar.state;

public class ConstraintInplaceThunk extends ConstraintThunk {

    public final int s;
    public final int sbar;
    public final Constraint c;

    private ConstraintInplaceThunk(int s, Constraint c) {
        this(s, s ^ 1, c);
    }

    private ConstraintInplaceThunk(int s, int sbar, Constraint c) {
        this.s = s;
        this.sbar = sbar;
        this.c = c;
    }

    public <R> R accept(ConstraintThunk.Visitor<R> visitor, OctagonDifferenceBoundedMatrix m, OctagonDifferenceBoundedMatrix in) {
        return visitor.visit(this, m, in);
    }

    public static ConstraintInplaceThunk of(int s, Constraint c) {
        return new ConstraintInplaceThunk(s, c);
    }

    public static ConstraintInplaceThunk of(int s, int sbar, Constraint c) {
        return new ConstraintInplaceThunk(s, sbar, c);
    }

    @Override
    public int compareTo(ConstraintThunk other) {
        return 1;
    }

    @Override
    public String toString() {
        return String.format("(inplace) [%d, %d, %s]", s, sbar, c);
    }
}
