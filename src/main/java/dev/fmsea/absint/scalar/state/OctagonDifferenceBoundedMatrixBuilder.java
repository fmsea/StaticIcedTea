package dev.fmsea.absint.scalar.state;

public class OctagonDifferenceBoundedMatrixBuilder {

    private OctagonDifferenceBoundedMatrix matrix;

    public OctagonDifferenceBoundedMatrixBuilder(int N, boolean top) {
        this.matrix = new OctagonDifferenceBoundedMatrix(N, top);
    }

    /** Add a constraint to the underlying matrix.
     *
     * @param: {@link Integer} i, Source variable of difference constraint
     * @param: {@link Integer} j, Target variable of difference constraint
     * @param: {@link Constraint} constraint, difference constraint bound
     * @return self.
     */
    public OctagonDifferenceBoundedMatrixBuilder setConstraint(int i, int j, Constraint c) {
        this.matrix.setConstraint(i, j, c);
        return this;
    }

    public OctagonDifferenceBoundedMatrixBuilder peek() {
        System.err.println(this.matrix.toString());
        return this;
    }

    public OctagonDifferenceBoundedMatrixBuilder close() {
        this.matrix.canonicalize();
        return this;
    }

    public OctagonDifferenceBoundedMatrix build() {
        return this.matrix;
    }
}
