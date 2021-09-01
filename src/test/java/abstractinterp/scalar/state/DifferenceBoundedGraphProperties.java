package abstractinterp.scalar.state;

import java.util.Set;
import java.util.HashSet;
import java.util.stream.Stream;
import net.jqwik.api.ForAll;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.NotEmpty;
import net.jqwik.api.constraints.UniqueElements;
import net.jqwik.api.Property;
import net.jqwik.api.lifecycle.BeforeProperty;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import soot.Local;
import soot.IntType;
import soot.jimple.Jimple;
import soot.jimple.IntConstant;

public class DifferenceBoundedGraphProperties {

    private Local[] xs;
    private Set<Local> locals;

    @BeforeProperty
    void setupLocals() {
        this.xs = new Local[] {
            Jimple.v().newLocal("x0", IntType.v()),
            Jimple.v().newLocal("x1", IntType.v()),
            Jimple.v().newLocal("x2", IntType.v()),
            Jimple.v().newLocal("x3", IntType.v()),
        };
        this.locals = new HashSet<>();
        Stream.of(xs).forEach(x -> locals.add(x));
    }

    @Property
    void incClosureEqualsClosure(@ForAll @IntRange(min = -536870911, max = 536870911) int c,
                                 @ForAll @IntRange(min = -536870911, max = 536870911) int k,
                                 @ForAll @IntRange(min = -536870911, max = 536870911) int w,
                                 @ForAll @IntRange(min = -536870911, max = 536870911) int v) {
        Local ZERO = Variable.ZERO;
        this.locals.add(ZERO);
        DifferenceBoundedGraph g1 = new DifferenceBoundedGraph(locals);
        g1.add(xs[0], xs[1], new Constraint(c, PredicateType.Le));
        g1.add(xs[1], xs[2], new Constraint(k, PredicateType.Le));
        g1.add(xs[2], ZERO, new Constraint(w, PredicateType.Le));
        g1.add(xs[3], ZERO, new Constraint(v));
        g1.add(ZERO, xs[3], new Constraint(v * -1));
        DifferenceBoundedGraph g2 = new DifferenceBoundedGraph(g1);
        assertAll("incremental closure is feasible",
                  locals.stream().map(x -> () -> assertTrue(g1.incrementalClosure(x))));
        assertTrue(g2.computeClosure());
        assertAll("g1: incremental closure is equal to transitive closure",
                  () -> assertEquals(new Constraint(c + k, PredicateType.Le),
                                     g1.eval(xs[0], xs[2])),
                  () -> assertEquals(new Constraint(c + k + w, PredicateType.Le),
                                     g1.eval(xs[0], ZERO)),
                  () -> assertEquals(new Constraint(k + w, PredicateType.Le),
                                     g1.eval(xs[1], ZERO)),
                  () -> assertEquals(Constraint.TOP(), g1.eval(xs[0], xs[3])),
                  () -> assertEquals(Constraint.TOP(), g1.eval(xs[1], xs[3])),
                  () -> assertEquals(Constraint.TOP(), g1.eval(xs[2], xs[3])));
        assertAll("g2: transitive Closure is equal to repeated incremental closure",
                  () -> assertEquals(new Constraint(c + k, PredicateType.Le),
                                     g2.eval(xs[0], xs[2])),
                  () -> assertEquals(new Constraint(c + k + w, PredicateType.Le),
                                     g2.eval(xs[0], ZERO)),
                  () -> assertEquals(new Constraint(k + w, PredicateType.Le),
                                     g2.eval(xs[1], ZERO)),
                  () -> assertEquals(Constraint.TOP(), g2.eval(xs[0], xs[3])),
                  () -> assertEquals(Constraint.TOP(), g2.eval(xs[1], xs[3])),
                  () -> assertEquals(Constraint.TOP(), g2.eval(xs[2], xs[3])));
    }
}
