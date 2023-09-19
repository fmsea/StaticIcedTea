package abstractinterp.scalar;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import abstractinterp.scalar.state.DefaultOctagonState;
import abstractinterp.scalar.state.ZoneState;
import abstractinterp.scalar.state.providers.OctagonZoneComparisonProvider;
import solver.SolverFactory;
import solver.SolverWrapper;
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
        IntegerAnalysis octagonAnalysis = new IntegerAnalysis(this.solver, body, 2, DefaultOctagonState.class);
        IntegerAnalysis zoneAnalysis = new IntegerAnalysis(this.solver, body, 2, ZoneState.class);
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
