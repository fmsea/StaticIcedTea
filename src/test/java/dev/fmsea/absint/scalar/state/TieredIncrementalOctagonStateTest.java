package dev.fmsea.absint.scalar.state;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import dev.fmsea.absint.scalar.state.factory.TieredIncrementalOctagonStateFactory;
import dev.fmsea.absint.scalar.state.providers.IncrementalFullSMTOctagonProvider;
import dev.fmsea.absint.scalar.state.providers.IncrementalReducedSMTOctagonProvider;
import dev.fmsea.common.Locals;
import dev.fmsea.tadr.TADRReader;
import soot.Local;

public class TieredIncrementalOctagonStateTest extends OctagonStateTest {

    public TieredIncrementalOctagonStateTest() {
        super(new TieredIncrementalOctagonStateFactory());
    }

    @Test
    @Override
    public void testReassignment2Vars() {
        Local[] xs = new Local[] {
            Locals.get("x"),
            Locals.get("y"),
        };
        Set<Local> locals = Stream.of(xs).collect(Collectors.toSet());
        OctagonState s = new OctagonStateBuilder(this.factory, locals, true)
            .addConstraint(TADRReader.parse("(= x (- 10))"))
            .addConstraint(TADRReader.parse("(= y 2)"))
            .close()
            .build();

        assertAll(
            () -> assertEquals("(and (<= x (- 10)) (<= y 2) (>= x (- 10)) (>= y 2))", s.toSmt()),
            () -> assertTrue(s.isFeasible()));

        var t = this.factory.copy(s);
        assertAll(
            () -> assertEquals(s, t),
            () -> assertTrue(t.close()));
        s.update(TADRReader.parse("(= x (+ x y))"), t);

        assertAll(
            () -> assertFalse("false".equals(s.toSmt())),
            () -> assertTrue(s.isFeasible()));
    }

    @ParameterizedTest
    @ArgumentsSource(IncrementalFullSMTOctagonProvider.class)
    void testToSmt(OctagonState state, String oracle) {
        assertEquals(oracle, state.toSmt());
    }

    @ParameterizedTest
    @ArgumentsSource(IncrementalReducedSMTOctagonProvider.class)
    void testReducedSmt(OctagonState state, String oracle) {
        state.reduce();
        assertEquals(oracle, state.toSmt());
    }
}
