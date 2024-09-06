package dev.fmsea.benchmark;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import dev.fmsea.absint.scalar.state.Constraint;
import dev.fmsea.absint.scalar.state.OctagonDifferenceBoundedMatrix;
import dev.fmsea.absint.scalar.state.OctagonDifferenceBoundedMatrixBuilder;

@State(Scope.Thread)
public class SimpleOctagonState {

    OctagonDifferenceBoundedMatrix matrix;

    @Setup
    public void prepare() {
        this.matrix = new OctagonDifferenceBoundedMatrixBuilder(6, true)
            .setConstraint(2, 3, Constraint.of(+4))
            .setConstraint(3, 2, Constraint.of(-4))
            .setConstraint(4, 5, Constraint.of(+6))
            .setConstraint(5, 4, Constraint.of(-6))
            .close()
            .build();
    }
}
