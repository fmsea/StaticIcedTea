package dev.fmsea.absint.scalar.state;

public class TieredOctagonDifferenceBoundedMatrixBuilder {

    private TieredOctagonDifferenceBoundedMatrix matrix;

    public TieredOctagonDifferenceBoundedMatrixBuilder(int N, boolean top) {
        this.matrix = new TieredOctagonDifferenceBoundedMatrix(N, top);
    }

    /** Add a constraint to the underlying matrix.
     *
     * @param: {@link Integer} i, Source variable of difference constraint
     * @param: {@link Integer} j, Target variable of difference constraint
     * @param: {@link Constraint} constraint, difference constraint bound
     * @return self.
     */
    public TieredOctagonDifferenceBoundedMatrixBuilder setConstraint(int i, int j, Constraint c) {
        this.matrix.setConstraint(i, j, c);
        return this;
    }

    public TieredOctagonDifferenceBoundedMatrixBuilder peek() {
        System.err.println(this.matrix.toString());
        return this;
    }

    public TieredOctagonDifferenceBoundedMatrixBuilder close() {
        this.matrix.canonicalize();
        return this;
    }

    public TieredOctagonDifferenceBoundedMatrix build() {
        return this.matrix;
    }
}
