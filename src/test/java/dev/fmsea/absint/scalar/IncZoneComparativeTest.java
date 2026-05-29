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

import dev.fmsea.absint.scalar.state.IncZoneState;
import dev.fmsea.absint.scalar.state.ZoneState;
import dev.fmsea.absint.scalar.state.providers.JimpleProvider;
import dev.fmsea.solver.SolverWrapper;
import dev.fmsea.solver.SolverWrapperZ3;
import soot.Body;
import soot.Scene;

public class IncZoneComparativeTest extends AbstractNumericalTest {

    private SolverWrapper solver;
    private Path z3TestFile;

    @BeforeEach
    void setup() {
        try {
            this.solver = new SolverWrapperZ3();
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
                "inc.pado01.constantValuePropagation.smt.out"),
            Arguments.arguments(
                JimpleProvider.binaryArithmaticMethod("z3ConstantMath"),
                "inc.pado01.constantMathPropagation.smt.out"),
            Arguments.arguments(
                JimpleProvider.simpleIfStatement("z3_simpleIf"),
                "inc.pado01.branching.smt.out"),
            Arguments.arguments(
                JimpleProvider.simpleLoopStatement("z3_simple_loop"),
                "inc.pado01.looping.smt.out"),
            Arguments.arguments(
                JimpleProvider.example5(),
                "inc.pado01.example5.smt.out"),
            Arguments.arguments(
                JimpleProvider.nonsense(),
                "inc.pado01.nonsenseExample.smt.out"),
            Arguments.arguments(
                JimpleProvider.neqLoop(),
                "inc.pado01.neqLoop.smt.out"),
            Arguments.arguments(
                JimpleProvider.ballonGetArrow(),
                "inc.pado01.getArrowSubset.smt.out"),
            Arguments.arguments(
                JimpleProvider.intervalComparison(),
                "inc.pado01.intervalComparison.smt.out"),
            Arguments.arguments(
                JimpleProvider.fibonacci(),
                "inc.pado01.fibonacci.smt.out"));
    }

    @ParameterizedTest
    @MethodSource("comparisons")
    void IncZoneZoneComparisonTest(Body body, String resourceFile) {
        IntegerAnalysis zoneAnalysis = new IntegerAnalysisBuilder()
            .withSolver(this.solver)
            .withBody(body)
            .withIterations(2)
            .withType(IncZoneState.class)
            .build();
        IntegerAnalysis intervalAnalysis = new IntegerAnalysisBuilder()
            .withSolver(this.solver)
            .withBody(body)
            .withIterations(2)
            .withType(ZoneState.class)
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
