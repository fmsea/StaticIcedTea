package dev.fmsea.absint.scalar.state;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import dev.fmsea.absint.scalar.state.factory.OctagonStateFactory;
import dev.fmsea.common.Locals;
import dev.fmsea.tadr.TADRReader;
import soot.Local;

public abstract class OctagonStateTest {

    protected final OctagonStateFactory factory;

    public OctagonStateTest(OctagonStateFactory factory) {
        this.factory = factory;
    }

    @Test
    public void testReassignment() {
        Local[] xs = new Local[] {
            Locals.get("i0"),
            Locals.get("i1"),
            Locals.get("i2"),
            Locals.get("i3"),
            Locals.get("i4"),
        };
        Set<Local> locals = Stream.of(xs).collect(Collectors.toSet());
        OctagonState s = new OctagonStateBuilder(this.factory, locals, true)
            .addConstraint(TADRReader.parse("(= i1 1)"))
            .addConstraint(TADRReader.parse("(= i2 1)"))
            .addConstraint(TADRReader.parse("(= i3 1)"))
            .addConstraint(TADRReader.parse("(= i4 0)"))
            .addConstraint(TADRReader.parse("(>= i0 1)"))
            .close()
            .build();

        s.reduce();
        assertEquals("(and (<= i1 1) (<= i2 1) (<= i3 1) (<= i4 0) (>= i0 1) (>= i1 1) (>= i2 1) (>= i3 1) (>= i4 0))",
            s.toSmt());

        s.close();

        var t = s.copy();
        s.update(TADRReader.parse("(= i4 (+ i4 1))"), t);

        assertAll(
            () -> assertFalse("false".equals(s.toSmt())),
            () -> assertTrue(s.isFeasible()));

        s.widenWith(t);
        assertTrue(s.isFeasible());
    }

    @Test
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
            () -> assertEquals("(and (<= x (+ y (- 12))) (<= x (- 10)) (<= y (+ x 12)) (<= y (- (- 8) x)) (<= y 2) (>= x (- 10)) (>= y (- 8 x)) (>= y 2))", s.toSmt()),
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

    @Test
    public void testReassignment3Vars() {
        Local[] xs = new Local[] {
            Locals.get("x"),
            Locals.get("y"),
            Locals.get("z"),
        };
        Set<Local> locals = Stream.of(xs).collect(Collectors.toSet());
        OctagonState s = new OctagonStateBuilder(this.factory, locals, true)
            .addConstraint(TADRReader.parse("(<= x 0)"))
            .addConstraint(TADRReader.parse("(>= x (- 1))"))
            .addConstraint(TADRReader.parse("(<= y (+ x (- 1)))"))
            .addConstraint(TADRReader.parse("(<= y (+ z (- 1)))"))
            .addConstraint(TADRReader.parse("(>= z 1)"))
            .close()
            .build();

        var t = this.factory.copy(s);

        assertAll(
            () -> assertEquals(s, t),
            () -> assertTrue(t.close())
        );

        s.update(TADRReader.parse("(= x (+ x z))"), t);

        assertAll(
            () -> assertFalse("false".equals(s.toSmt())),
            () -> assertTrue(s.isFeasible()));
    }

    @Test
    public void testReassignmentSmallBlur() {
        Local[] xs = new Local[] {
            Locals.get("x"),
            Locals.get("y"),
            Locals.get("z"),
        };
        Set<Local> locals = Stream.of(xs).collect(Collectors.toSet());
        OctagonState s = new OctagonStateBuilder(this.factory, locals, true)
            .addConstraint(TADRReader.parse("(>= y 0)"))
            .addConstraint(TADRReader.parse("(<= y 2)"))
            .addConstraint(TADRReader.parse("(<= x (+ y 0))"))
            .addConstraint(TADRReader.parse("(<= y (+ x 0))"))
            .addConstraint(TADRReader.parse("(<= x (+ z (- 1)))"))
            .addConstraint(TADRReader.parse("(>= z 1)"))
            .close()
            .build();

        var t = this.factory.copy(s);

        assertAll(
            () -> assertEquals(s, t),
            () -> assertTrue(t.close())
        );

        s.update(TADRReader.parse("(= x (+ x z))"), t);

        assertAll(
            () -> assertFalse("false".equals(s.toSmt())),
            () -> assertTrue(s.isFeasible()));

        s.reduce();
        s.toGraph().toDot("/tmp/blur-state-1.dot");
        s.close();

    }

    @Test
    public void testReassignmentBlur() {
        Local[] xs = new Local[] {
            Locals.get("$i1"),
            Locals.get("$i11"),
            Locals.get("$i14"),
            Locals.get("$i17"),
            Locals.get("$i19"),
            Locals.get("$i5"),
            Locals.get("b66"),
            Locals.get("b67"),
            Locals.get("b68"),
            Locals.get("b69"),
            Locals.get("i0"),
            Locals.get("i2"),
            Locals.get("i3"),
            Locals.get("i6"),
            Locals.get("i60"),
            Locals.get("i61"),
            Locals.get("i65"),
            Locals.get("i70"),
            Locals.get("i72"),
            Locals.get("i73"),
            Locals.get("i74"),
            Locals.get("i75"),
            Locals.get("i76"),
            Locals.get("i78"),
            Locals.get("i8"),
        };
        Set<Local> locals = Stream.of(xs).collect(Collectors.toSet());
        OctagonState s = new OctagonStateBuilder(this.factory, locals, true)
            .addConstraint(TADRReader.parse("(<= $i5 0)"))
            .addConstraint(TADRReader.parse("(<= $i1 (+ i2 (- 1)))"))
            .addConstraint(TADRReader.parse("(<= $i11 (+ i72 0))"))
            .addConstraint(TADRReader.parse("(<= $i14 (+ i73 0))"))
            .addConstraint(TADRReader.parse("(<= $i17 (+ i74 0))"))
            .addConstraint(TADRReader.parse("(<= $i19 (+ i75 0))"))
            .addConstraint(TADRReader.parse("(<= i0 (+ i3 (- 1)))"))
            .addConstraint(TADRReader.parse("(<= i2 (+ $i1 1))"))
            .addConstraint(TADRReader.parse("(<= i3 (+ i0 1))"))
            .addConstraint(TADRReader.parse("(<= i65 (+ i70 0))"))
            .addConstraint(TADRReader.parse("(<= i65 (+ i8 (- 1)))"))
            .addConstraint(TADRReader.parse("(<= i70 (+ i65 0))"))
            .addConstraint(TADRReader.parse("(<= i70 (+ i8 (- 1)))"))
            .addConstraint(TADRReader.parse("(<= i72 (+ $i11 0))"))
            .addConstraint(TADRReader.parse("(<= i73 (+ $i14 0))"))
            .addConstraint(TADRReader.parse("(<= i74 (+ $i17 0))"))
            .addConstraint(TADRReader.parse("(<= i75 (+ $i19 0))"))
            .addConstraint(TADRReader.parse("(<= b66 0)"))
            .addConstraint(TADRReader.parse("(<= b67 0)"))
            .addConstraint(TADRReader.parse("(<= b68 0)"))
            .addConstraint(TADRReader.parse("(<= b69 0)"))
            .addConstraint(TADRReader.parse("(<= i0 0)"))
            .addConstraint(TADRReader.parse("(<= i3 1)"))
            .addConstraint(TADRReader.parse("(<= i60 0)"))
            .addConstraint(TADRReader.parse("(<= i61 0)"))
            .addConstraint(TADRReader.parse("(<= i65 2)"))
            .addConstraint(TADRReader.parse("(<= i70 2)"))
            .addConstraint(TADRReader.parse("(<= i76 1)"))
            .addConstraint(TADRReader.parse("(<= i78 0)"))
            .addConstraint(TADRReader.parse("(>= b66 0)"))
            .addConstraint(TADRReader.parse("(>= b67 0)"))
            .addConstraint(TADRReader.parse("(>= b68 0)"))
            .addConstraint(TADRReader.parse("(>= b69 0)"))
            .addConstraint(TADRReader.parse("(>= i6 1)"))
            .addConstraint(TADRReader.parse("(>= i60 0)"))
            .addConstraint(TADRReader.parse("(>= i61 0)"))
            .addConstraint(TADRReader.parse("(>= i65 0)"))
            .addConstraint(TADRReader.parse("(>= i70 0)"))
            .addConstraint(TADRReader.parse("(>= i76 1)"))
            .addConstraint(TADRReader.parse("(>= i78 0)"))
            .addConstraint(TADRReader.parse("(>= i8 1)"))
            .close()
            .build();

        var t = this.factory.copy(s);

        assertAll(
            () -> assertEquals(s, t),
            () -> assertTrue(t.close())
        );

        s.update(TADRReader.parse("(= i70 (+ i70 i8))"), t);

        assertAll(
            () -> assertFalse("false".equals(s.toSmt())),
            () -> assertTrue(s.isFeasible()));
    }

    @Test
    void testAssignmentInstances() {
        Local[] xs = new Local[] {
            Locals.get("u0"),
            Locals.get("w0"),
            Locals.get("x0"),
        };
        Set<Local> locals = Stream.of(xs).collect(Collectors.toSet());
        OctagonState s = new OctagonStateBuilder(this.factory, locals, true)
            .addConstraint(TADRReader.parse("(<= u0 (+ x0 0))"))
            .addConstraint(TADRReader.parse("(<= x0 (+ u0 0))"))
            .close()
            .build();

        var t = this.factory.copy(s);


        s.refine(TADRReader.parse("(< x0 20)"), t);

        assertAll(
            () -> assertTrue(s.reduce()),
            () -> assertEquals(Stream.of(
                "(<= u0 (+ x0 0))",
                "(<= u0 19)",
                "(<= x0 (+ u0 0))",
                "(<= x0 19)"
            ).collect(Collectors.joining(" ", "(and ", ")")),
                s.toSmt()),
            () -> assertTrue(s.isFeasible()));
    }
}
