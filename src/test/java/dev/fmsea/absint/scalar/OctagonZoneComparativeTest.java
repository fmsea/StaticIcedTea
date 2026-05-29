package dev.fmsea.absint.scalar;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import dev.fmsea.absint.scalar.state.DefaultOctagonState;
import dev.fmsea.absint.scalar.state.ZoneState;
import dev.fmsea.absint.scalar.state.providers.OctagonZoneComparisonProvider;
import dev.fmsea.solver.SolverFactory;
import dev.fmsea.solver.SolverWrapper;
import soot.Body;
import soot.Scene;

public class OctagonZoneComparativeTest extends AbstractNumericalTest {

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

    @ParameterizedTest
    @ArgumentsSource(OctagonZoneComparisonProvider.class)
    void OctagonsZoneComparisonTest(Body body, String resourceFile) {
        IntegerAnalysis octagonAnalysis = new IntegerAnalysisBuilder()
            .withSolver(this.solver)
            .withBody(body)
            .withIterations(2)
            .withType(DefaultOctagonState.class)
            .build();
        IntegerAnalysis zoneAnalysis = new IntegerAnalysisBuilder()
            .withSolver(this.solver)
            .withBody(body)
            .withIterations(2)
            .withType(ZoneState.class)
            .build();
        octagonAnalysis.runAnalysis();
        zoneAnalysis.runAnalysis();
        String expected = readResourcesFile(resourceFile);
        assertTrue(runComparison(
            generateReport(octagonAnalysis),
            generateReport(zoneAnalysis),
            expected,
            this.z3TestFile));
    }
}
