package abstractinterp.scalar.state;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.function.BiConsumer;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import soot.Local;
import soot.IntType;
import soot.jimple.Jimple;

import solver.SolverWrapper;
import solver.SolverFactory;

public class ZoneDifferenceBoundedGraphTest {

    SolverWrapper solver;
    Set<Local> locals;
    Local[] xs;

    @BeforeEach
    void setup() {
        this.solver = SolverFactory.getSolver();
        this.xs = new Local[] {
            Variable.ZERO,
            Jimple.v().newLocal("x1", IntType.v()),
            Jimple.v().newLocal("x2", IntType.v()),
        };
        this.locals = Stream.of(this.xs).collect(Collectors.toSet());
    }

    @Test
    void testGetConnectedVariables() {
        {
            ZoneDifferenceBoundedGraph g = new ZoneDifferenceBoundedGraph(this.locals, true);
            g.setConstraint(xs[0], xs[1], Constraint.of(-1));
            g.setConstraint(xs[0], xs[2], Constraint.of(-1));
            g.setConstraint(xs[1], xs[0], Constraint.of(4));
            g.setConstraint(xs[2], xs[0], Constraint.of(3));
            assertAll(() -> assertEquals(Set.of(xs[1]), g.getConnectedVariablesOf(xs[1])),
                      () -> assertEquals(Set.of(xs[2]), g.getConnectedVariablesOf(xs[2])));
        }

        {
            ZoneDifferenceBoundedGraph g = new ZoneDifferenceBoundedGraph(this.locals, true);
            g.setConstraint(xs[1], xs[2], Constraint.of(1));
            assertAll(() -> assertEquals(Set.of(xs[1], xs[2]), g.getConnectedVariablesOf(xs[1])),
                      () -> assertEquals(Set.of(xs[1], xs[2]), g.getConnectedVariablesOf(xs[2])));
        }

        {
            ZoneDifferenceBoundedGraph g = new ZoneDifferenceBoundedGraph(this.locals, true);
            g.setConstraint(xs[0], xs[1], Constraint.of(-1));
            g.setConstraint(xs[0], xs[2], Constraint.of(-1));
            g.setConstraint(xs[1], xs[0], Constraint.of(4));
            g.setConstraint(xs[1], xs[2], Constraint.of(1));
            g.setConstraint(xs[2], xs[0], Constraint.of(3));
            assertAll(() -> assertEquals(Set.of(xs[1], xs[2]), g.getConnectedVariablesOf(xs[1])),
                      () -> assertEquals(Set.of(xs[1], xs[2]), g.getConnectedVariablesOf(xs[2])));
        }

        {
            xs = new Local[] {
                Variable.ZERO,
                Jimple.v().newLocal("x1", IntType.v()),
                Jimple.v().newLocal("x2", IntType.v()),
                Jimple.v().newLocal("x3", IntType.v()),
                Jimple.v().newLocal("x4", IntType.v()),
                Jimple.v().newLocal("x5", IntType.v()),
                Jimple.v().newLocal("x6", IntType.v()),
            };
            locals = Stream.of(this.xs).collect(Collectors.toSet());
            ZoneDifferenceBoundedGraph g = new ZoneDifferenceBoundedGraph(this.locals, true);
            g.setConstraint(xs[0], xs[1], Constraint.of(-1));
            g.setConstraint(xs[0], xs[2], Constraint.of(-2));
            g.setConstraint(xs[0], xs[5], Constraint.of(-3));
            g.setConstraint(xs[1], xs[0], Constraint.of(+4));
            g.setConstraint(xs[1], xs[2], Constraint.of(+1));
            g.setConstraint(xs[2], xs[0], Constraint.of(+3));
            g.setConstraint(xs[3], xs[1], Constraint.of(+2));
            g.setConstraint(xs[3], xs[2], Constraint.of(+3));
            g.setConstraint(xs[3], xs[6], Constraint.of(-4));
            g.setConstraint(xs[5], xs[0], Constraint.of(+5));
            assertAll(() -> assertEquals(Set.of(xs[1], xs[2], xs[3], xs[6]),
                                         g.getConnectedVariablesOf(xs[1])),
                      () -> assertEquals(Set.of(xs[1], xs[2], xs[3], xs[6]),
                                         g.getConnectedVariablesOf(xs[2])),
                      () -> assertEquals(Set.of(xs[5]),
                                         g.getConnectedVariablesOf(xs[5])),
                      () -> assertEquals(Set.of(xs[1], xs[2], xs[3], xs[6]), // ?
                                         g.getConnectedVariablesOf(xs[6])));
        }
    }
}
