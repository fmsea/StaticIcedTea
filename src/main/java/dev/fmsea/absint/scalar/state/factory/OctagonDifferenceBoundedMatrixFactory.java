package dev.fmsea.absint.scalar.state.factory;

import dev.fmsea.absint.scalar.state.OctagonDifferenceBoundedMatrix;
import dev.fmsea.absint.scalar.state.TieredOctagonDifferenceBoundedMatrix;

public class OctagonDifferenceBoundedMatrixFactory {

    private final OctagonDifferenceBoundedMatrixType type;

    public OctagonDifferenceBoundedMatrixFactory(OctagonDifferenceBoundedMatrixType type) {
        this.type = type;
    }

    public OctagonDifferenceBoundedMatrix mk(int N, boolean top) {
        switch (this.type) {
            case TIERED:
                return new TieredOctagonDifferenceBoundedMatrix(N, top);
            case DEFAULT:
            default:
                return new OctagonDifferenceBoundedMatrix(N, top);
        }
    }
}
