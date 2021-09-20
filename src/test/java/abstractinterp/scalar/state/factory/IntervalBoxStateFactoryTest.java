package abstractinterp.scalar.state.factory;

import java.util.HashSet;
import java.util.Set;
import soot.Local;
import soot.IntType;
import soot.jimple.Jimple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import abstractinterp.scalar.state.IntervalBoxState;
import abstractinterp.scalar.state.Interval32Box;

public class IntervalBoxStateFactoryTest {

    private IntervalBoxStateFactory factory;
    private Set<Local> locals;
    private Local[] xs;

    @BeforeEach
    void setup() {
        this.factory = new IntervalBoxStateFactory();
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
    void testInitialFlowCreatesBottoms() {
        IntervalBoxState state = factory.initialFlow(locals);
        assertAll(locals.stream()
                  .map(l -> () -> assertEquals(Interval32Box.BOT(),
                                               state.getValue(l))));
    }

    @Test
    void testEntryFlowCreatesTops() {
        IntervalBoxState state = factory.entryFlow(locals);
        assertAll(locals.stream()
                  .map(l -> () -> assertEquals(Interval32Box.TOP(),
                                               state.getValue(l))));
    }
}
