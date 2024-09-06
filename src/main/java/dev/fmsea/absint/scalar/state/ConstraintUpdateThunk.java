package dev.fmsea.absint.scalar.state;

public class ConstraintUpdateThunk extends ConstraintThunk {
    public final int s;
    public final int sbar;
    public final int t;
    public final int tbar;
    public final Constraint c;

    private ConstraintUpdateThunk(int s, int t, Constraint c) {
        this.s = s;
        this.sbar = s ^ 1;
        this.t = t;
        this.tbar = t ^ 1;
        this.c = c;
    }

    public <R> R accept(ConstraintThunk.Visitor<R> visitor, OctagonDifferenceBoundedMatrix m, OctagonDifferenceBoundedMatrix in) {
        return visitor.visitThunk(this, m, in);
    }

    public static ConstraintUpdateThunk of(int s, int t, Constraint c) {
        return new ConstraintUpdateThunk(s, t, c);
    }

    public static ConstraintUpdateThunk xor(ConstraintUpdateThunk c) {
        return new ConstraintUpdateThunk(c.sbar, c.tbar, c.c);
    }

    @Override
    public int compareTo(ConstraintThunk other) {
        return 1;
    }

    @Override
    public String toString() {
        return String.format("%d, %d = %s", this.s, this.t, this.c);
    }
}
