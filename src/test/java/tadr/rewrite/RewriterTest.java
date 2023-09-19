package tadr.rewrite;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import abstractinterp.scalar.state.Interval32Box;
import common.Locals;
import soot.Local;
import tadr.TADR;

public abstract class RewriterTest {

    protected static Interval32Box basicMap(Local g) {
        // [x1 ↦ 1, x2 ↦ 3, x3 ↦ 2]
        if (g.equals(Locals.get("x1"))) {
            return Interval32Box.of(1);
        } else if (g.equals(Locals.get("x2"))) {
            return Interval32Box.of(3);
        } else if (g.equals(Locals.get("x3"))) {
            return Interval32Box.of(2);
        } else return Interval32Box.TOP();
    }

    protected static String peek(Stream<TADR> exprs) {
        return exprs.map(expr -> expr.toSmt())
            .sorted()
            .collect(Collectors.joining(" "));
    }
}
