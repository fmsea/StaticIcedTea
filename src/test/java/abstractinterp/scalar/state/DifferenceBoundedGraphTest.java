package abstractinterp.scalar.state;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
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

public class DifferenceBoundedGraphTest {

    private Set<Local> locals;
    private Local[] xs;

    @BeforeEach
    void setupLocals() {
        this.xs = new Local[] {
            Jimple.v().newLocal("x0", IntType.v()),
            Jimple.v().newLocal("x1", IntType.v()),
            Jimple.v().newLocal("x2", IntType.v()),
            Jimple.v().newLocal("x3", IntType.v()),
        };
        this.locals = new HashSet<>(4);
        for (Local x : this.xs) { this.locals.add(x); }
    }

    @Test
    void evalReturnsTOPWhenNoEdge() {
        DifferenceBoundedGraph graph = new DifferenceBoundedGraph(locals);
        for (int i = 0; i < xs.length; i++) {
            for (int j = 0; j < xs.length; j++) {
                if (i == j) {
                    continue;
                }
                assertEquals(Constraint.TOP(), graph.eval(xs[i], xs[j]));
            }
        }
    }

    @Test
    void copyTo() {
        Consumer<DifferenceBoundedGraph> check = (graph -> {
                assertAll("copy",
                                     () -> assertEquals(new Constraint(3),
                                                                   graph.eval(xs[0], xs[1])),
                                     () -> assertEquals(new Constraint(3),
                                                                   graph.eval(xs[1], xs[2])),
                                     () -> assertEquals(new Constraint(3),
                                                                   graph.eval(xs[2], xs[3])),
                                     () -> assertEquals(new Constraint(9),
                                                                   graph.eval(xs[0], xs[3])));
            });
        DifferenceBoundedGraph graph = new DifferenceBoundedGraph(locals);
        graph.add(xs[0], xs[1], new Constraint(3));
        graph.add(xs[1], xs[2], new Constraint(3));
        graph.add(xs[2], xs[3], new Constraint(3));
        graph.add(xs[0], xs[3], new Constraint(9));
        DifferenceBoundedGraph copy = new DifferenceBoundedGraph(graph);
        check.accept(copy);
        copy = new DifferenceBoundedGraph(locals);
        graph.copyTo(copy);
        check.accept(copy);
    }

    @Test
    void addConstraint() {
        DifferenceBoundedGraph graph = new DifferenceBoundedGraph(locals);
        graph.add(xs[0], xs[1], new Constraint(3));
        assertEquals(new Constraint(3), graph.eval(xs[0], xs[1]));
        graph.add(xs[1], xs[2], new Constraint(3, PredicateType.Le));
        assertEquals(new Constraint(3, PredicateType.Le),
                                graph.eval(xs[1], xs[2]));
        assertEquals(Constraint.TOP(), graph.eval(xs[2], xs[1]));
    }

    @Test
    void replaceConstraint() {
        DifferenceBoundedGraph graph = new DifferenceBoundedGraph(locals);
        graph.add(xs[0], xs[1], new Constraint(3));
        assertEquals(new Constraint(3), graph.eval(xs[0], xs[1]));
        graph.add(xs[0], xs[1], new Constraint(4));
        assertEquals(new Constraint(4), graph.eval(xs[0], xs[1]));
    }

    @Test
    void addingBottomMakesInfeasible() {
        DifferenceBoundedGraph graph = new DifferenceBoundedGraph(locals);
        graph.add(xs[0], xs[1], Constraint.BOT());
        assertFalse(graph.isFeasible());
    }

    @Test
    void testGetConstraints() {
        DifferenceBoundedGraph graph = new DifferenceBoundedGraph(locals);
        graph.add(xs[0], xs[1], new Constraint(3));
        graph.add(xs[1], xs[2], new Constraint(3, PredicateType.Le));
        List<DBSTriple> triples = graph.getConstraints();
        assertEquals(2, triples.size());
        triples = graph.getConstraints(xs[0]);
        assertEquals(1, triples.size());
        triples = graph.getConstraints(xs[3]);
        assertEquals(0, triples.size());
    }

    @Test
    void testGetValue() {
        DifferenceBoundedGraph graph = new DifferenceBoundedGraph(locals);
        Optional<Constraint> empty = graph.getValue(xs[0], xs[1]);
        assertFalse(empty.isPresent());
        graph.add(xs[0], xs[1], new Constraint(0));
        Optional<Constraint> c = graph.getValue(xs[0], xs[1]);
        assertAll(() -> assertTrue(c.isPresent()),
                  () -> assertEquals(new Constraint(0), c.get()));
    }

    @Test
    void testProjectionLearnsTransitiveEdge() {
        DifferenceBoundedGraph graph = new DifferenceBoundedGraph(locals);
        graph.add(xs[0], xs[1], new Constraint(+3, PredicateType.Le));
        graph.add(xs[1], xs[2], new Constraint(-1, PredicateType.Le));

        {
            DifferenceBoundedGraph test = new DifferenceBoundedGraph(graph);
            test.projectInterval(xs[0], xs[2]);
            assertEquals(new Constraint(2, PredicateType.Le), test.eval(xs[0], xs[2]));
        }

        {
            DifferenceBoundedGraph test = new DifferenceBoundedGraph(graph);
            test.computeClosure();
            assertEquals(new Constraint(2, PredicateType.Le), test.eval(xs[0], xs[2]));
        }
    }

    @Test
    @DisplayName("closure does not find nonsense edges")
    void testClosureDoesNotTraverseZERO() {
        Local ZERO = Variable.ZERO;
        locals.add(ZERO);

        {
            DifferenceBoundedGraph graph = new DifferenceBoundedGraph(locals);
            graph.add(xs[0], ZERO, new Constraint(5));
            graph.add(xs[1], ZERO, new Constraint(-5));
            graph.add(xs[2], ZERO, new Constraint(10));
            graph.add(ZERO, xs[2], new Constraint(-11));
            assertAll("closure detects (nearby) negative cycles",
                      () -> assertFalse(graph.computeClosure()));
        }

        {
            DifferenceBoundedGraph graph = new DifferenceBoundedGraph(locals);
            graph.add(xs[0], xs[1], new Constraint(3));
            graph.add(xs[1], xs[2], new Constraint(2));
            graph.add(xs[2], ZERO, new Constraint(5));
            graph.add(ZERO, xs[3], new Constraint(2));
            graph.add(xs[3], ZERO, new Constraint(-2));
            assertAll("closure computes transitive closure without nonsense edges",
                      () -> assertTrue(graph.computeClosure()),
                      () -> assertEquals(new Constraint(3), graph.eval(xs[0], xs[1])),
                      () -> assertEquals(new Constraint(2), graph.eval(xs[1], xs[2])),
                      () -> assertEquals(new Constraint(5), graph.eval(xs[0], xs[2])),
                      () -> assertEquals(new Constraint(5), graph.eval(xs[2], ZERO)),
                      () -> assertEquals(new Constraint(7), graph.eval(xs[1], ZERO)),
                      () -> assertEquals(new Constraint(10), graph.eval(xs[0], ZERO)),
                      () -> assertEquals(new Constraint(-2), graph.eval(xs[3], ZERO)),
                      () -> assertEquals(new Constraint(2), graph.eval(ZERO, xs[3])),
                      () -> assertEquals(Constraint.TOP(), graph.eval(xs[0], xs[3])),
                      () -> assertEquals(Constraint.TOP(), graph.eval(xs[1], xs[3])),
                      () -> assertEquals(Constraint.TOP(), graph.eval(xs[2], xs[3])),
                      () -> assertEquals(Constraint.TOP(), graph.eval(xs[3], xs[0])),
                      () -> assertEquals(Constraint.TOP(), graph.eval(xs[3], xs[1])),
                      () -> assertEquals(Constraint.TOP(), graph.eval(xs[3], xs[2])));
        }

        {
            DifferenceBoundedGraph graph = new DifferenceBoundedGraph(locals);
            graph.add(xs[0], xs[1], new Constraint(1));
            graph.add(xs[1], xs[2], new Constraint(1));
            graph.add(xs[2], xs[3], new Constraint(-2));
            graph.add(xs[3], xs[0], new Constraint(-2));
            assertAll("closure detects (far away) negative cycles",
                      () -> assertFalse(graph.computeClosure()));
        }
    }

    @Test
    @DisplayName("closure cannot find inner edge of transitive relation")
    void testClosureDoesNotImplyExtraThings() {
        DifferenceBoundedGraph graph = new DifferenceBoundedGraph(locals);
        graph.add(xs[1], xs[2], new Constraint(3, PredicateType.Le));
        graph.add(xs[1], xs[0], new Constraint(4));
        assertAll(() -> assertTrue(graph.computeClosure()),
                  () -> assertEquals(Constraint.TOP(), graph.eval(xs[0], xs[2])));
    }

    @Test
    void testTransitiveClosureStable() {
        DifferenceBoundedGraph graph = new DifferenceBoundedGraph(locals);
        graph.add(xs[0], xs[1], new Constraint(3));
        graph.add(xs[1], xs[2], new Constraint(5));
        graph.add(xs[1], xs[3], new Constraint(1, PredicateType.Le));
        assertTrue(graph.computeClosure());
        Consumer<DifferenceBoundedGraph> check = (g) -> {
            assertAll("transitive closure",
                      () -> assertEquals(new Constraint(3), g.eval(xs[0], xs[1])),
                      () -> assertEquals(new Constraint(5), g.eval(xs[1], xs[2])),
                      () -> assertEquals(new Constraint(8), g.eval(xs[0], xs[2])),
                      () -> assertEquals(new Constraint(1, PredicateType.Le),
                                         g.eval(xs[1], xs[3])),
                      () -> assertEquals(new Constraint(4, PredicateType.Le),
                                         g.eval(xs[0], xs[3])));
        };
        check.accept(graph);
        assertTrue(graph.computeClosure());
        check.accept(graph);
    }

    @Test
    void testIntersectionIsIdempotent() {
        DifferenceBoundedGraph g1 = new DifferenceBoundedGraph(locals);
        g1.add(xs[0], xs[1], new Constraint(3));
        g1.add(xs[1], xs[2], new Constraint(5));
        g1.add(xs[1], xs[3], new Constraint(1, PredicateType.Le));
        DifferenceBoundedGraph g2 = new DifferenceBoundedGraph(locals);
        g2.add(xs[0], xs[1], new Constraint(2));
        g2.add(xs[1], xs[2], new Constraint(5));
        g2.add(xs[2], xs[3], new Constraint(2, PredicateType.Le));
        {
            DifferenceBoundedGraph m = new DifferenceBoundedGraph(g1);
            DifferenceBoundedGraph n = new DifferenceBoundedGraph(g2);
            Consumer<DifferenceBoundedGraph> check = (g) -> {
                assertAll("m ⊓ n",
                          () -> assertTrue(m.isFeasible()),
                          () -> assertEquals(new Constraint(2), g.eval(xs[0], xs[1])),
                          () -> assertEquals(new Constraint(5), g.eval(xs[1], xs[2])),
                          () -> assertEquals(new Constraint(1, PredicateType.Le),
                                             g.eval(xs[1], xs[3])),
                          () -> assertEquals(new Constraint(2, PredicateType.Le),
                                             g.eval(xs[2], xs[3])),
                          () -> assertEquals(new Constraint(7), g.eval(xs[0], xs[2]))
                          );
            };
            m.intersection(n);
            check.accept(m);
            m.intersection(n);
            check.accept(m);
        }

        {
            DifferenceBoundedGraph m = new DifferenceBoundedGraph(g1);
            DifferenceBoundedGraph n = new DifferenceBoundedGraph(g2);
            Consumer<DifferenceBoundedGraph> check = (g) -> {
                assertAll("n ⊓ m",
                          () -> assertTrue(m.isFeasible()),
                          () -> assertEquals(new Constraint(2), g.eval(xs[0], xs[1])),
                          () -> assertEquals(new Constraint(5), g.eval(xs[1], xs[2])),
                          () -> assertEquals(new Constraint(1, PredicateType.Le),
                                             g.eval(xs[1], xs[3])),
                          () -> assertEquals(new Constraint(2, PredicateType.Le),
                                             g.eval(xs[2], xs[3])),
                          () -> assertEquals(new Constraint(7), g.eval(xs[0], xs[2]))
                          );
            };
            n.intersection(m);
            check.accept(n);
            n.intersection(m);
            check.accept(n);
        }
    }

    @Test
    void testEquals() {
        DifferenceBoundedGraph graph = new DifferenceBoundedGraph(locals);
        assertFalse(graph.equals(null));
        assertFalse(graph.equals(new Object()));
        assertTrue(graph.equals(graph));
        assertTrue(graph.equals(new DifferenceBoundedGraph(graph)));
        graph.add(xs[0], xs[1], new Constraint(3));
        graph.add(xs[1], xs[2], new Constraint(3, PredicateType.Le));
        graph.add(xs[2], xs[3], new Constraint(6, PredicateType.Le));
        graph.add(xs[2], xs[0], new Constraint(3));
        DifferenceBoundedGraph g2 = new DifferenceBoundedGraph(graph);
        assertTrue(graph.equals(g2));
        assertTrue(g2.equals(graph));
        g2.add(xs[0], xs[1], new Constraint(2));
        assertFalse(graph.equals(g2));
        assertFalse(g2.equals(graph));
        g2.add(xs[1], xs[0], new Constraint(-2));
        assertFalse(graph.equals(g2));
        assertFalse(g2.equals(graph));
    }

    @Test
    void testEqualsWithTOP() {
        DifferenceBoundedGraph graph = new DifferenceBoundedGraph(locals);
        graph.add(xs[0], xs[1], new Constraint(3));
        graph.add(xs[1], xs[2], new Constraint(4, PredicateType.Le));
        graph.add(xs[2], xs[3], Constraint.TOP());
        DifferenceBoundedGraph test = new DifferenceBoundedGraph(locals);
        test.add(xs[0], xs[1], new Constraint(3));
        test.add(xs[1], xs[2], new Constraint(4, PredicateType.Le));
        test.add(xs[2], xs[3], Constraint.TOP());
        assertTrue(graph.equals(test));
        assertTrue(test.equals(graph));
        assertTrue(graph.computeClosure());
        assertTrue(test.computeClosure());
        assertTrue(graph.equals(test));
        assertTrue(test.equals(graph));
    }
}
