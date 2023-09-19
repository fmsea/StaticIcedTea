package abstractinterp.scalar.state.providers;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import abstractinterp.scalar.state.OctagonState;
import abstractinterp.scalar.state.OctagonStateBuilder;
import abstractinterp.scalar.state.factory.OctagonStateFactory;
import common.Locals;
import soot.Local;
import tadr.TADR;
import tadr.TADRReader;

public abstract class FullSMTOctagonProvider implements ArgumentsProvider {

    private OctagonStateFactory factory;
    private final Set<Local> locals;
    private final Local[] xs;

    public FullSMTOctagonProvider(OctagonStateFactory factory) {
        this.factory = factory;
        this.xs = new Local[] {
            Locals.get("x1"),
            Locals.get("x2"),
            Locals.get("x3"),
            Locals.get("x4"),
        };
        this.locals = Stream.of(xs).collect(Collectors.toSet());
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        return Stream.of(
            Arguments.of(new OctagonStateBuilder(this.factory, this.locals, true).build(), "true"),
            Arguments.of(new OctagonStateBuilder(this.factory, this.locals, false).build(), "false"),
            Arguments.of(new OctagonStateBuilder(this.factory, locals, true)
                .addConstraint(TADR.newLeExpr(TADR.newVariable(xs[0]), TADR.newValue(2)))
                .addConstraint(TADR.newGeExpr(TADR.newVariable(xs[0]), TADR.newValue(3)))
                .close()
                .build(), "false"),
            Arguments.of(new OctagonStateBuilder(this.factory, locals, true)
                .addConstraint(TADR.newNeExpr(TADR.newVariable(xs[0]), TADR.newValue(2)))
                .close()
                .build(), "true"),
            Arguments.of(new OctagonStateBuilder(this.factory, locals, true)
                .addConstraint(TADR.newLeExpr(TADR.newVariable(xs[0]), TADR.newValue(2)))
                .addConstraint(TADR.newLeExpr(TADR.newVariable(xs[1]), TADR.newValue(4)))
                .close()
                .build(), "(and (<= x1 2) (<= x2 (- 6 x1)) (<= x2 4))"),
            Arguments.of(new OctagonStateBuilder(this.factory, locals, true)
                .addConstraint(TADR.newGeExpr(TADR.newVariable(xs[0]), TADR.newValue(-2)))
                .addConstraint(TADR.newLeExpr(TADR.newVariable(xs[0]), TADR.newValue(3)))
                .addConstraint(TADR.newLeExpr(TADR.newVariable(xs[1]), TADR.newValue(4)))
                .close()
                .build(), Stream.of(
                    "(<= x1 3)",
                    "(<= x2 (+ x1 6))",
                    "(<= x2 (- 7 x1))",
                    "(<= x2 4)",
                    "(>= x1 (- 2))")
                    .collect(Collectors.joining(" ", "(and ", ")"))),
            Arguments.of(new OctagonStateBuilder(this.factory, locals, true)
                .addConstraint(TADR.newLtExpr(TADR.newVariable(xs[0]), TADR.newValue(2)))
                .close()
                .build(), "(<= x1 1)"),
            Arguments.of(new OctagonStateBuilder(this.factory, locals, true)
                .addConstraint(TADR.newGtExpr(TADR.newVariable(xs[0]), TADR.newValue(2)))
                .close()
                .build(), "(>= x1 3)"),
            Arguments.of(new OctagonStateBuilder(this.factory, locals, true)
                .addConstraint(TADR.newGtExpr(TADR.newVariable(xs[0]), TADR.newValue(-2)))
                .close()
                .build(), "(>= x1 (- 1))"),
            Arguments.of(new OctagonStateBuilder(this.factory, locals, true)
                .addConstraint(TADR.newLtExpr(TADR.newVariable(xs[0]), TADR.newValue(2)))
                .close()
                .build(), "(<= x1 1)"),
            Arguments.of(new OctagonStateBuilder(this.factory, locals, true)
                .addConstraint(TADR.newEqExpr(TADR.newVariable(xs[0]), TADR.newValue(2)))
                .close()
                .build(), "(and (<= x1 2) (>= x1 2))"),
            Arguments.of(new OctagonStateBuilder(this.factory, locals, true)
                .addConstraint(TADR.newEqExpr(TADR.newVariable(xs[0]), TADR.newValue(5)))
                .close()
                .build(), "(and (<= x1 5) (>= x1 5))"),
            Arguments.of(new OctagonStateBuilder(this.factory, locals, true)
                .addConstraint(TADRReader.parse("(= x1 5)"))
                .addConstraint(TADRReader.parse("(<= x1 (- 7 x2))"))
                .addConstraint(TADRReader.parse("(<= x1 (+ x2 3))"))
                .addConstraint(TADRReader.parse("(<= x2 (+ x1 4))"))
                .close()
                .build(), Stream.of(
                    "(<= x1 (+ x2 3))",
                    "(<= x1 5)",
                    "(<= x2 (+ x1 (- 3)))",
                    "(<= x2 (- 7 x1))",
                    "(<= x2 2)",
                    "(>= x1 5)",
                    "(>= x2 (- (- 7) x1))",
                    "(>= x2 2)")
                    .collect(Collectors.joining(" ", "(and ", ")"))),
            Arguments.of(new OctagonStateBuilder(this.factory, locals, true)
                .addConstraint(TADR.newLeExpr(TADR.newVariable(xs[0]), TADR.newValue(3)))
                .addConstraint(TADR.newGeExpr(TADR.newVariable(xs[0]), TADR.newValue(1)))
                .addConstraint(TADR.newLeExpr(TADR.newVariable(xs[1]), TADR.newValue(5)))
                .addConstraint(TADR.newGeExpr(TADR.newVariable(xs[1]), TADR.newValue(2)))
                .addConstraint(TADR.newLeExpr(TADR.newVariable(xs[2]),
                    TADR.newAddExpr(TADR.newVariable(xs[1]), TADR.newValue(3))))
                .addConstraint(TADR.newLeExpr(TADR.newVariable(xs[2]),
                    TADR.newAddExpr(TADR.newVariable(xs[0]), TADR.newValue(5))))
                .close()
                .build(), Stream.of(
                    "(<= x1 (+ x2 1))",
                    "(<= x1 3)",
                    "(<= x2 (+ x1 4))",
                    "(<= x2 (- 8 x1))",
                    "(<= x2 5)",
                    "(<= x3 (+ x1 5))",
                    "(<= x3 (+ x2 3))",
                    "(<= x3 (- 11 x1))",
                    "(<= x3 (- 13 x2))",
                    "(<= x3 8)",
                    "(>= x1 1)",
                    "(>= x2 (- (- 3) x1))",
                    "(>= x2 2)")
                    .collect(Collectors.joining(" ", "(and ", ")"))),
            Arguments.of(new OctagonStateBuilder(this.factory, locals, true)
                .addConstraint(TADR.newLeExpr(TADR.newVariable(xs[1]), TADR.newValue(5)))
                .addConstraint(TADR.newGeExpr(TADR.newVariable(xs[1]), TADR.newValue(2)))
                .addConstraint(TADR.newLeExpr(TADR.newVariable(xs[0]),
                    TADR.newAddExpr(TADR.newVariable(xs[1]), TADR.newValue(5))))
                .addConstraint(TADR.newLeExpr(TADR.newVariable(xs[1]),
                    TADR.newAddExpr(TADR.newVariable(xs[0]), TADR.newValue(-2))))
                .close()
                .build(), Stream.of(
                    "(<= x1 (+ x2 5))",
                    "(<= x1 10)",
                    "(<= x2 (+ x1 (- 2)))",
                    "(<= x2 (- 15 x1))",
                    "(<= x2 5)",
                    "(>= x1 4)",
                    "(>= x2 (- (- 6) x1))",
                    "(>= x2 2)")
                    .collect(Collectors.joining(" ", "(and ", ")"))),
            Arguments.of(new OctagonStateBuilder(this.factory, locals, true)
                .addConstraint(TADRReader.parse("(= x1 (* x2 3))"))
                .close()
                .build(), "true"),
            Arguments.of(new OctagonStateBuilder(this.factory, locals, true)
                .addConstraint(TADR.newLeExpr(TADR.newVariable(xs[0]), TADR.newValue(3)))
                .addConstraint(TADR.newGeExpr(TADR.newVariable(xs[0]), TADR.newValue(1)))
                .addConstraint(TADR.newLeExpr(TADR.newVariable(xs[1]), TADR.newValue(5)))
                .addConstraint(TADR.newGeExpr(TADR.newVariable(xs[1]), TADR.newValue(2)))
                .addConstraint(TADR.newLeExpr(TADR.newVariable(xs[2]),
                    TADR.newAddExpr(TADR.newVariable(xs[1]), TADR.newValue(3))))
                .addConstraint(TADR.newLeExpr(TADR.newVariable(xs[2]),
                    TADR.newAddExpr(TADR.newVariable(xs[0]), TADR.newValue(5))))
                .close()
                .build(), Stream.of(
                    "(<= x1 (+ x2 1))",
                    "(<= x1 3)",
                    "(<= x2 (+ x1 4))",
                    "(<= x2 (- 8 x1))",
                    "(<= x2 5)",
                    "(<= x3 (+ x1 5))",
                    "(<= x3 (+ x2 3))",
                    "(<= x3 (- 11 x1))",
                    "(<= x3 (- 13 x2))",
                    "(<= x3 8)",
                    "(>= x1 1)",
                    "(>= x2 (- (- 3) x1))",
                    "(>= x2 2)")
                    .collect(Collectors.joining(" ", "(and ", ")"))),
            Arguments.of(new OctagonStateBuilder(this.factory, locals, true)
                .addConstraint(TADRReader.parse("(= x1 (* 3 x2))"))
                .close()
                .build(), "true"),
            Arguments.of(new OctagonStateBuilder(this.factory, locals, true)
                .addConstraint(TADRReader.parse("(= x2 5)"))
                .addConstraint(TADRReader.parse("(= x1 (* 3 x2))"))
                .close()
                .build(), Stream.of(
                    "(<= x1 (+ x2 10))",
                    "(<= x1 15)",
                    "(<= x2 (+ x1 (- 10)))",
                    "(<= x2 (- 20 x1))",
                    "(<= x2 5)",
                    "(>= x1 15)",
                    "(>= x2 (- (- 20) x1))",
                    "(>= x2 5)")
                    .collect(Collectors.joining(" ", "(and ", ")"))),
            Arguments.of(new OctagonStateBuilder(this.factory, locals, true)
                .addConstraint(TADRReader.parse("(= x2 5)"))
                .addConstraint(TADRReader.parse("(= x1 (+ 10 x2))"))
                .close()
                .build(), Stream.of(
                    "(<= x1 (+ x2 10))",
                    "(<= x1 15)",
                    "(<= x2 (+ x1 (- 10)))",
                    "(<= x2 (- 20 x1))",
                    "(<= x2 5)",
                    "(>= x1 15)",
                    "(>= x2 (- (- 20) x1))",
                    "(>= x2 5)")
                    .collect(Collectors.joining(" ", "(and ", ")"))),
            Arguments.of(new OctagonStateBuilder(this.factory, locals, true)
                .addConstraint(TADRReader.parse("(= x2 5)"))
                .addConstraint(TADRReader.parse("(= x1 (+ x2 10))"))
                .close()
                .build(), Stream.of(
                    "(<= x1 (+ x2 10))",
                    "(<= x1 15)",
                    "(<= x2 (+ x1 (- 10)))",
                    "(<= x2 (- 20 x1))",
                    "(<= x2 5)",
                    "(>= x1 15)",
                    "(>= x2 (- (- 20) x1))",
                    "(>= x2 5)")
                    .collect(Collectors.joining(" ", "(and ", ")"))),
            Arguments.of(new OctagonStateBuilder(this.factory, locals, true)
                .addConstraint(TADRReader.parse("(= x1 (div x2 2))"))
                .close()
                .build(), "true"),
            Arguments.of(new OctagonStateBuilder(this.factory, locals, true)
                .addConstraint(TADRReader.parse("(= x2 7)"))
                .addConstraint(TADRReader.parse("(= x1 (div x2 2))"))
                .close()
                .build(), Stream.of(
                    "(<= x1 (+ x2 (- 4)))",
                    "(<= x1 3)",
                    "(<= x2 (+ x1 4))",
                    "(<= x2 (- 10 x1))",
                    "(<= x2 7)",
                    "(>= x1 3)",
                    "(>= x2 (- (- 10) x1))",
                    "(>= x2 7)")
                    .collect(Collectors.joining(" ", "(and ", ")"))),
            Arguments.of(new OctagonStateBuilder(this.factory, locals, true)
                .addConstraint(TADRReader.parse("(<= x1 2)"))
                .addConstraint(TADRReader.parse("(>= x1 1)"))
                .addConstraint(TADRReader.parse("(= x2 (+ x1 x1))"))
                .close()
                .build(), Stream.of(
                    "(<= x1 (+ x2 (- 1)))",
                    "(<= x1 2)",
                    "(<= x2 (+ x1 2))",
                    "(<= x2 (- 6 x1))",
                    "(<= x2 4)",
                    "(>= x1 1)",
                    "(>= x2 (- (- 3) x1))",
                    "(>= x2 2)")
                    .collect(Collectors.joining(" ", "(and ", ")"))),
            Arguments.of(new OctagonStateBuilder(this.factory, locals, true)
                .addConstraint(TADRReader.parse("(<= x1 2)"))
                .addConstraint(TADRReader.parse("(>= x1 1)"))
                .addConstraint(TADRReader.parse("(= x2 (* 2 x1))"))
                .close()
                .build(), Stream.of(
                    "(<= x1 (+ x2 (- 1)))",
                    "(<= x1 2)",
                    "(<= x2 (+ x1 2))",
                    "(<= x2 (- 6 x1))",
                    "(<= x2 4)",
                    "(>= x1 1)",
                    "(>= x2 (- (- 3) x1))",
                    "(>= x2 2)")
                    .collect(Collectors.joining(" ", "(and ", ")"))),
            Arguments.of(new OctagonStateBuilder(this.factory, locals, true)
                .addConstraint(TADRReader.parse("(<= x1 2)"))
                .addConstraint(TADRReader.parse("(>= x1 1)"))
                .addConstraint(TADRReader.parse("(= x2 (* x1 2))"))
                .close()
                .build(), Stream.of(
                    "(<= x1 (+ x2 (- 1)))",
                    "(<= x1 2)",
                    "(<= x2 (+ x1 2))",
                    "(<= x2 (- 6 x1))",
                    "(<= x2 4)",
                    "(>= x1 1)",
                    "(>= x2 (- (- 3) x1))",
                    "(>= x2 2)")
                    .collect(Collectors.joining(" ", "(and ", ")"))),
            Arguments.of(new OctagonStateBuilder(this.factory, locals, true)
                .addConstraint(TADRReader.parse("(<= x1 2)"))
                .addConstraint(TADRReader.parse("(>= x1 1)"))
                .addConstraint(TADRReader.parse("(= x2 (* x1 x1))"))
                .close()
                .build(), Stream.of(
                    "(<= x1 (+ x2 0))",
                    "(<= x1 2)",
                    "(<= x2 (+ x1 2))",
                    "(<= x2 (- 6 x1))",
                    "(<= x2 4)",
                    "(>= x1 1)",
                    "(>= x2 (- (- 2) x1))",
                    "(>= x2 1)")
                    .collect(Collectors.joining(" ", "(and ", ")"))),
            Arguments.of(new OctagonStateBuilder(this.factory, locals, true)
                .addConstraint(TADRReader.parse("(<= x1 2)"))
                .addConstraint(TADRReader.parse("(>= x1 1)"))
                .addConstraint(TADRReader.parse("(= x2 (- x1 x1))"))
                .close()
                .build(), Stream.of(
                    "(<= x1 (+ x2 2))",
                    "(<= x1 2)",
                    "(<= x2 (+ x1 (- 1)))",
                    "(<= x2 (- 2 x1))",
                    "(<= x2 0)",
                    "(>= x1 1)",
                    "(>= x2 (- (- 1) x1))",
                    "(>= x2 0)")
                    .collect(Collectors.joining(" ", "(and ", ")"))),
            Arguments.of(new OctagonStateBuilder(this.factory, locals, true)
                .addConstraint(TADRReader.parse("(<= x1 2)"))
                .addConstraint(TADRReader.parse("(>= x1 1)"))
                .addConstraint(TADRReader.parse("(<= x3 2)"))
                .addConstraint(TADRReader.parse("(>= x3 1)"))
                .addConstraint(TADRReader.parse("(= x2 (- x1 x3))"))
                .close()
                .build(), Stream.of(
                    "(<= x1 (+ x2 2))",
                    "(<= x1 (+ x3 1))",
                    "(<= x1 2)",
                    "(<= x2 (+ x1 (- 1)))",
                    "(<= x2 (+ x3 0))",
                    "(<= x2 (- 3 x1))",
                    "(<= x2 1)",
                    "(<= x3 (+ x1 1))",
                    "(<= x3 (+ x2 3))",
                    "(<= x3 (- 2 x2))",
                    "(<= x3 (- 4 x1))",
                    "(<= x3 2)",
                    "(>= x1 1)",
                    "(>= x2 (- 0 x1))",
                    "(>= x2 (- 1))",
                    "(>= x3 (- (- 1) x2))",
                    "(>= x3 (- (- 2) x1))",
                    "(>= x3 1)")
                    .collect(Collectors.joining(" ", "(and ", ")"))),
            Arguments.of(new OctagonStateBuilder(this.factory, locals, true)
                .addConstraint(TADRReader.parse("(= x1 2)"))
                .addConstraint(TADRReader.parse("(= x2 (- x1 x1))"))
                .close()
                .build(), Stream.of(
                    "(<= x1 (+ x2 2))",
                    "(<= x1 2)",
                    "(<= x2 (+ x1 (- 2)))",
                    "(<= x2 (- 2 x1))",
                    "(<= x2 0)",
                    "(>= x1 2)",
                    "(>= x2 (- (- 2) x1))",
                    "(>= x2 0)")
                    .collect(Collectors.joining(" ", "(and ", ")"))),
            Arguments.of(new OctagonStateBuilder(this.factory, locals, true)
                .addConstraint(TADRReader.parse("(<= x1 2)"))
                .addConstraint(TADRReader.parse("(<= x3 5)"))
                .addConstraint(TADRReader.parse("(>= x1 1)"))
                .addConstraint(TADRReader.parse("(>= x3 3)"))
                .addConstraint(TADRReader.parse("(= x2 (+ x1 x3))"))
                .close()
                .build(), Stream.of(
                    "(<= x1 (+ x2 (- 3)))",
                    "(<= x1 (+ x3 (- 1)))",
                    "(<= x1 2)",
                    "(<= x2 (+ x1 5))",
                    "(<= x2 (+ x3 2))",
                    "(<= x2 (- 9 x1))",
                    "(<= x2 7)",
                    "(<= x3 (+ x1 4))",
                    "(<= x3 (+ x2 (- 1)))",
                    "(<= x3 (- 12 x2))",
                    "(<= x3 (- 7 x1))",
                    "(<= x3 5)",
                    "(>= x1 1)",
                    "(>= x2 (- (- 5) x1))",
                    "(>= x2 4)",
                    "(>= x3 (- (- 4) x1))",
                    "(>= x3 (- (- 7) x2))",
                    "(>= x3 3)")
                    .collect(Collectors.joining(" ", "(and ", ")")))
        );
    }
}
