package dev.fmsea.absint.scalar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import dev.fmsea.absint.scalar.state.IntervalBoxState;
import dev.fmsea.absint.scalar.state.providers.IntervalNumericalProvider;
import soot.Body;
import soot.Scene;

public class IntervalNumericalTest extends AbstractNumericalTest {

    @BeforeAll
    static void sootSuiteInitialize() {
        Scene.v().loadClassAndSupport("java.lang.Object");
        Scene.v().loadClassAndSupport("java.lang.System");
        Scene.v().loadNecessaryClasses();
    }

    @ParameterizedTest
    @ArgumentsSource(IntervalNumericalProvider.class)
    public void testIntervalNumericalAnalysis(Body body, String resourceOracle) {
        IntegerAnalysis  analysis = new IntegerAnalysisBuilder()
            .withBody(body)
            .withIterations(2)
            .withType(IntervalBoxState.class)
            .build();
        analysis.runAnalysis();
        String actual = generateReport(analysis).trim();
        String expected = readResourcesFile(resourceOracle);
        assertEquals(expected, actual);
    }

}
