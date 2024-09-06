package dev.fmsea.benchmark;

import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import dev.fmsea.absint.scalar.state.ZoneDifferenceBoundedMatrix;
import dev.fmsea.absint.scalar.state.providers.ZoneDifferenceBoundedMatrixProvider;

@State(Scope.Thread)
public class RandomClosedZoneState {
    @Param({"0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9"})
    double density;

    @Param({"50", "100", "200"})
    int N;

    // @Param({"1", "2", "3", "4", "5", "6"})
    // int constraintCount;

    ZoneDifferenceBoundedMatrix matrix;

    @Setup
    public void prepare() {
        this.matrix = ZoneDifferenceBoundedMatrixProvider.sample(N, density, 1234567890);
        this.matrix.computeClosure();
    }
}
