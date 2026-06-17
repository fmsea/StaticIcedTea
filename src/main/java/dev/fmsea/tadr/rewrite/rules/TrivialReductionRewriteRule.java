package dev.fmsea.tadr.rewrite.rules;

import java.util.function.Function;
import java.util.stream.Stream;

import dev.fmsea.absint.scalar.state.Interval32Box;
import dev.fmsea.tadr.TADR;
import dev.fmsea.tadr.visitors.VariableVisitor;
import soot.Local;

public class TrivialReductionRewriteRule extends RewriteRule {

    public boolean canRewrite(TADR expr) {
        return expr.accept(new VariableVisitor()).isEmpty();
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        return Stream.of();
    }
}
