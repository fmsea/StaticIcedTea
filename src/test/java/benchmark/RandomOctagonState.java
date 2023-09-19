package benchmark;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import abstractinterp.scalar.state.OctagonDifferenceBoundedMatrix;
import abstractinterp.scalar.state.providers.OctagonDifferenceBoundedMatrixProvider;

@State(Scope.Thread)
public class RandomOctagonState {

    OctagonDifferenceBoundedMatrix matrix;

    @Setup
    public void prepare() {
        this.matrix = OctagonDifferenceBoundedMatrixProvider.sampleMatrix(100);
    }
}
