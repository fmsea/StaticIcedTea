package dev.fmsea.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

import dev.fmsea.absint.scalar.state.Constraint;

public class SearchIncrementalClosure {

    @Benchmark public void bench(RandomClosedOctagonState state, Blackhole bh) {
        bh.consume(state.matrix.incrementalClosure(0, 79, Constraint.of(-2)));
    }
}
