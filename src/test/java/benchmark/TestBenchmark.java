package benchmark;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import abstractinterp.scalar.state.Constraint;
import abstractinterp.scalar.state.ConstraintUpdateThunk;
import util.Properties;

public class TestBenchmark {

    @Test
    void launchBenchmark() throws Exception {

        Options opt = new OptionsBuilder()
            // Specify which benchmarks to run.
            // You can be more specific if you'd like to run only one benchmark per test.
            .include(this.getClass().getName())
            // Set the following options as needed
            .mode(Mode.AverageTime)
            .timeUnit(TimeUnit.MILLISECONDS)
            .warmupTime(TimeValue.seconds(1))
            .warmupIterations(3)
            .measurementTime(TimeValue.seconds(1))
            .measurementIterations(5)
            .threads(1)
            .forks(1)
            .shouldFailOnError(true)
            .shouldDoGC(true)
            //.jvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintInlining")
            //.addProfiler(WinPerfAsmProfiler.class)
            .build();

        new Runner(opt).run();
    }

    @Benchmark public void OctagonDeferredIncrementalClosure(RandomClosedOctagonState state, ConstraintState c, Blackhole bh) {
        bh.consume(state.matrix.incrementalClosure(
            c.thunks.stream().map(t -> ConstraintUpdateThunk.of(t.s % state.N, t.t % state.N, t.c)).collect(Collectors.toSet())
        ));
    }

    // @Benchmark public void OctagonDeferredIncrementalZClosure(RandomClosedOctagonState state, Blackhole bh) {
    //     bh.consume(state.matrix.incrementalZClosure(
    //         Set.of(
    //             ConstraintUpdateThunk.of(0, 1, Constraint.of(+12)),
    //             ConstraintUpdateThunk.of(1, 0, Constraint.of(-12)),
    //             ConstraintUpdateThunk.of(0, 4, Constraint.of(+3)),
    //             ConstraintUpdateThunk.of(4, 0, Constraint.of(-3)),
    //             ConstraintUpdateThunk.of(0, 2, Constraint.of(+4)),
    //             ConstraintUpdateThunk.of(2, 0, Constraint.of(-4))
    //         )
    //     ));
    // }

    @Benchmark public void OctagonRegularIncrementalClosure(RandomClosedOctagonState state, ConstraintState c, Blackhole bh) {
        bh.consume(
            c.thunks.stream().<Supplier<Boolean>>map(t -> () -> state.matrix.putIncremental(t.s % state.N, t.t % state.N, t.c))
                .map(e -> e.get())
                .reduce((a, b) -> a && b));
    }

    // @Benchmark public void OctagonRegularIncrementalZClosure(RandomClosedOctagonState state, ConstraintState c, Blackhole bh) {
    //     Properties.IncrementalClosureAlgorithm = Properties.OctagonIncrementalClosureAlgorithm.CHAWDHARY;
    //     bh.consume(
    //         c.thunks.stream().<Supplier<Boolean>>map(t -> () -> state.matrix.putIncremental(t.s % state.N, t.t % state.N, t.c))
    //             .map(e -> e.get())
    //             .reduce((a, b) -> a && b));
    // }

    @Benchmark public void LargeFWClosure(RandomClosedOctagonState state, ConstraintState c, Blackhole bh) {
        c.thunks.stream().forEach(t -> {
            state.matrix.putConstraint(t.s % state.N, t.t % state.N, t.c);
            bh.consume(state.matrix.canonicalize());
        });
    }

    @Benchmark public void LargeDeferredFWClosure(RandomClosedOctagonState state, ConstraintState c, Blackhole bh) {
        c.thunks.stream().forEach(t -> state.matrix.putConstraint(t.s % state.N, t.t % state.N, t.c));
        bh.consume(state.matrix.computeClosure());
    }
}
