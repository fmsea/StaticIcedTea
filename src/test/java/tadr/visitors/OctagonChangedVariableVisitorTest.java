package tadr.visitors;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import common.Locals;
import soot.Local;
import tadr.TADR;
import tadr.TADRReader;

public class OctagonChangedVariableVisitorTest {

    static final OctagonChangedVariableVisitor deltaVisitor = new OctagonChangedVariableVisitor();

    @ParameterizedTest
    @MethodSource("provideArguments")
    void testVisit(TADR expr, Set<Local> oracle) {
        assertEquals(oracle, expr.accept(deltaVisitor), String.format("ΔV(%s) returned incorrect set", expr));
    }

    private static Stream<Arguments> provideArguments() {
        return Stream.of(
            Arguments.of(TADRReader.parse("(= x (+ y z))"), Locals.get("x", "y", "z")),
            Arguments.of(TADRReader.parse("(= x (+ 3 y))"), Locals.get("x", "y")),
            Arguments.of(TADRReader.parse("(= x (+ y 3))"), Locals.get("x", "y")),
            Arguments.of(TADRReader.parse("(= x (+ (- y) z))"), Locals.get("x", "y", "z")),
            Arguments.of(TADRReader.parse("(= x (+ y (- z)))"), Locals.get("x", "y", "z")),
            Arguments.of(TADRReader.parse("(= x (+ (- y) 3))"), Locals.get("x", "y")),
            Arguments.of(TADRReader.parse("(= x (+ 3 (- y)))"), Locals.get("x", "y")),
            Arguments.of(TADRReader.parse("(= x (+ 40 2))"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(= x (- y z))"), Locals.get("x", "y", "z")),
            Arguments.of(TADRReader.parse("(= x (- 3 y))"), Locals.get("x", "y")),
            Arguments.of(TADRReader.parse("(= x (- y 3))"), Locals.get("x", "y")),
            Arguments.of(TADRReader.parse("(= x (- (- y) z))"), Locals.get("x", "y", "z")),
            Arguments.of(TADRReader.parse("(= x (- y (- z)))"), Locals.get("x", "y", "z")),
            Arguments.of(TADRReader.parse("(= x (- (- y) 3))"), Locals.get("x", "y")),
            Arguments.of(TADRReader.parse("(= x (- 3 (- y)))"), Locals.get("x", "y")),
            Arguments.of(TADRReader.parse("(= x (+ 44 2))"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(= x (* y z))"), Locals.get("x", "y", "z")),
            Arguments.of(TADRReader.parse("(= x (* 3 y))"), Locals.get("x", "y")),
            Arguments.of(TADRReader.parse("(= x (* y 3))"), Locals.get("x", "y")),
            Arguments.of(TADRReader.parse("(= x (* (- y) z))"), Locals.get("x", "y", "z")),
            Arguments.of(TADRReader.parse("(= x (* y (- z)))"), Locals.get("x", "y", "z")),
            Arguments.of(TADRReader.parse("(= x (* (- y) 3))"), Locals.get("x", "y")),
            Arguments.of(TADRReader.parse("(= x (* 3 (- y)))"), Locals.get("x", "y")),
            Arguments.of(TADRReader.parse("(= x (* 6 7))"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(= x (div y z))"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(= x (div 3 y))"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(= x (div y 3))"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(= x (div (- y) z))"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(= x (div y (- z)))"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(= x (div (- y) 3))"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(= x (div 3 (- y)))"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(= x (* 126 3))"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(= x y)"), Locals.get("x", "y")),
            Arguments.of(TADRReader.parse("(= y x)"), Locals.get("x", "y")),
            Arguments.of(TADRReader.parse("(= (- x) y)"), Locals.get("x", "y")),
            Arguments.of(TADRReader.parse("(= y (- x))"), Locals.get("x", "y")),
            Arguments.of(TADRReader.parse("(= x 3)"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(= 3 x)"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(= (- x) 3)"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(= 3 (- x))"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(not (= x y))"), Set.of()),
            Arguments.of(TADRReader.parse("(not (= y x))"), Set.of()),
            Arguments.of(TADRReader.parse("(not (= (- x) y))"), Set.of()),
            Arguments.of(TADRReader.parse("(not (= y (- x)))"), Set.of()),
            Arguments.of(TADRReader.parse("(not (= x 3))"), Set.of()),
            Arguments.of(TADRReader.parse("(not (= 3 x))"), Set.of()),
            Arguments.of(TADRReader.parse("(not (= (- x) 3))"), Set.of()),
            Arguments.of(TADRReader.parse("(not (= 3 (- x)))"), Set.of()),
            Arguments.of(TADRReader.parse("(<= x y)"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(<= y x)"), Set.of(Locals.get("y"))),
            Arguments.of(TADRReader.parse("(<= (- x) y)"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(<= y (- x))"), Set.of(Locals.get("y"))),
            Arguments.of(TADRReader.parse("(<= x 3)"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(<= 3 x)"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(<= (- x) 3)"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(<= 3 (- x))"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(< x y)"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(< y x)"), Set.of(Locals.get("y"))),
            Arguments.of(TADRReader.parse("(< (- x) y)"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(< y (- x))"), Set.of(Locals.get("y"))),
            Arguments.of(TADRReader.parse("(< x 3)"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(< 3 x)"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(< (- x) 3)"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(< 3 (- x))"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(>= x y)"), Set.of(Locals.get("y"))),
            Arguments.of(TADRReader.parse("(>= y x)"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(>= (- x) y)"), Set.of(Locals.get("y"))),
            Arguments.of(TADRReader.parse("(>= y (- x))"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(>= x 3)"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(>= 3 x)"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(>= (- x) 3)"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(>= 3 (- x))"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(> x y)"), Set.of(Locals.get("y"))),
            Arguments.of(TADRReader.parse("(> y x)"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(> (- x) y)"), Set.of(Locals.get("y"))),
            Arguments.of(TADRReader.parse("(> y (- x))"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(> x 3)"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(> 3 x)"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(> (- x) 3)"), Set.of(Locals.get("x"))),
            Arguments.of(TADRReader.parse("(> 3 (- x))"), Set.of(Locals.get("x"))),
            Arguments.of(TADR.newNeExpr(TADR.from(Locals.get("x")), TADR.from(Locals.get("y"))), Set.of())
        );
    }
}
