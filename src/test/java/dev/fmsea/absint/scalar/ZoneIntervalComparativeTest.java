package dev.fmsea.absint.scalar;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import dev.fmsea.absint.scalar.state.IntervalBoxState;
import dev.fmsea.absint.scalar.state.ZoneState;
import dev.fmsea.absint.scalar.state.providers.JimpleProvider;
import dev.fmsea.solver.SolverFactory;
import dev.fmsea.solver.SolverWrapper;
import soot.Body;
import soot.Scene;

public class ZoneIntervalComparativeTest extends AbstractNumericalTest {

    private SolverWrapper solver;
    private Path z3TestFile;

    @BeforeEach
    void setup() {
        try {
            this.solver = SolverFactory.getSolver();
            this.z3TestFile = Files.createTempFile("dfa-smt",
                                                   String.valueOf(System.nanoTime()));
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    @AfterEach
    void teardown() {
        try {
            Files.deleteIfExists(this.z3TestFile);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    @BeforeAll
    static void sootSuiteInitialize() {
        Scene.v().loadClassAndSupport("java.lang.Object");
        Scene.v().loadClassAndSupport("java.lang.System");
        Scene.v().loadNecessaryClasses();
    }

    private static Stream<Arguments> comparisons() {
        return Stream.of(
            Arguments.arguments(
                JimpleProvider.constantJimpleMethod("z3_constant_test"),
                "pado01.int.constantValuePropagation.smt.out"),
            Arguments.arguments(
                JimpleProvider.binaryArithmaticMethod("z3ConstantMath"),
                "pado01.int.constantMathPropagation.smt.out"),
            Arguments.arguments(
                JimpleProvider.simpleIfStatement("z3_simpleIf"),
                "pado01.int.branching.smt.out"),
            Arguments.arguments(
                JimpleProvider.simpleLoopStatement("z3_simple_loop"),
                "pado01.int.looping.smt.out"),
            Arguments.arguments(
                JimpleProvider.example5(),
                "pado01.int.example5.smt.out"),
            Arguments.arguments(
                JimpleProvider.nonsense(),
                "pado01.int.nonsenseExample.smt.out"),
            Arguments.arguments(
                JimpleProvider.neqLoop(),
                "pado01.int.neqLoop.smt.out"),
            Arguments.arguments(
                JimpleProvider.ballonGetArrow(),
                "pado01.int.getArrowSubset.smt.out"),
            Arguments.arguments(
                JimpleProvider.intervalComparison(),
                "pado01.int.intervalComparison.smt.out"),
            Arguments.arguments(
                JimpleProvider.fibonacci(),
                "pado01.int.fibonacci.smt.out"));
    }

    @ParameterizedTest
    @MethodSource("comparisons")
    void ZoneIntervalComparisonTest(Body body, String resourceFile) {
        IntegerAnalysis zoneAnalysis = new IntegerAnalysisBuilder()
            .withSolver(this.solver)
            .withBody(body)
            .withIterations(2)
            .withType(ZoneState.class)
            .build();
        IntegerAnalysis intervalAnalysis = new IntegerAnalysisBuilder()
            .withSolver(solver)
            .withBody(body)
                .withIterations(2)
            .withType(IntervalBoxState.class)
            .build();
        zoneAnalysis.runAnalysis();
        intervalAnalysis.runAnalysis();
        String expected = readResourcesFile(resourceFile);
        assertTrue(runComparison(
            generateReport(zoneAnalysis),
            generateReport(intervalAnalysis),
            expected,
            this.z3TestFile));
    }
}
