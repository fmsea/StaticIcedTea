package dev.fmsea.benchmark;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import dev.fmsea.absint.scalar.state.OctagonDifferenceBoundedMatrix;
import dev.fmsea.absint.scalar.state.providers.OctagonDifferenceBoundedMatrixProvider;

@State(Scope.Thread)
public class RandomOctagonState {

    OctagonDifferenceBoundedMatrix matrix;

    @Setup
    public void prepare() {
        this.matrix = OctagonDifferenceBoundedMatrixProvider.sampleMatrix(100);
    }
}
