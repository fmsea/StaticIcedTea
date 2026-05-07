package dev.fmsea.absint.scalar.state;

public class TieredOctagonDifferenceBoundedMatrix extends OctagonDifferenceBoundedMatrix {

    protected boolean[] relational;
    public TieredOctagonDifferenceBoundedMatrix(int N, boolean top) {
        super(N, top);
        this.relational = new boolean[N];
    }

    public TieredOctagonDifferenceBoundedMatrix(TieredOctagonDifferenceBoundedMatrix copy) {
        super(copy);
        this.relational = new boolean[copy.N];
        for (int i = 0; i < this.N; i++) {
            this.relational[i] = copy.relational[i];
        }
    }

    public void copyTo(TieredOctagonDifferenceBoundedMatrix destination) {
        for (int i = 0; i < N; i++) {
            destination.relational[i] = this.relational[i];
            for (int j = 0; j < N; j++) {
                destination.matrix[i][j] = this.matrix[i][j].copy();
            }
        }
        destination.isClosed = this.isClosed;
    }

    @Override
    public void setConstraint(int i, int j, Constraint c) {
        super.setConstraint(i, j, c);
        if ((i ^ j) == 1) {
            this.relational[i] = false;
            this.relational[j] = false;
        } else {
            this.relational[i] = true;
            this.relational[j] = true;
        }
    }

    @Override
    protected boolean closureShouldUpdate(int i, int j, Constraint shortPath, Constraint longPath) {
        return this.relational[i] && this.relational[j] && Constraint.compare(shortPath, longPath) > 0;
    }

    @Override
    public boolean equals(Object o) {
        boolean equal = false;
        if (o instanceof TieredOctagonDifferenceBoundedMatrix) {
            equal = this.equals((TieredOctagonDifferenceBoundedMatrix) o);
        }
        return equal;
    }

    public boolean equals(TieredOctagonDifferenceBoundedMatrix other) {
        if (this.N != other.N) {
            return false;
        } else {
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (!this.matrix[i][j].equals(other.matrix[i][j])) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + N;
        result = prime * result + this.matrix.hashCode();
        result = prime * result + this.relational.hashCode();
        return result;
    }
}
