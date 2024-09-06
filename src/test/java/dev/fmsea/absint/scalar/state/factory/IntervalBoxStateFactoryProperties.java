package dev.fmsea.absint.scalar.state.factory;

import java.util.Set;
import soot.Local;
import soot.IntType;
import soot.jimple.Jimple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

import dev.fmsea.absint.scalar.state.IntervalBoxState;
import dev.fmsea.absint.scalar.state.Interval32Box;

public class IntervalBoxStateFactoryProperties {

    private IntervalBoxStateFactory factory = new IntervalBoxStateFactory();

    @Property
    void testInitialFlowCreatesBottoms(@ForAll Set<Local> locals) {
        IntervalBoxState state = factory.initialFlow(locals);
        assertAll(locals.stream()
                  .map(l -> () -> assertEquals(Interval32Box.BOT(),
                                               state.getValue(l))));
    }

    @Property
    void testEntryFlowCreatesTops(@ForAll Set<Local> locals) {
        IntervalBoxState state = factory.entryFlow(locals);
        assertAll(locals.stream()
                  .map(l -> () -> assertEquals(Interval32Box.TOP(),
                                               state.getValue(l))));
    }

}
