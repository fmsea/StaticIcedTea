package dev.fmsea.absint.scalar.state.update.rules;

import java.util.function.Function;
import java.util.stream.Stream;

import dev.fmsea.absint.scalar.state.Constraint;
import dev.fmsea.absint.scalar.state.ConstraintInplaceThunk;
import dev.fmsea.absint.scalar.state.ConstraintThunk;
import dev.fmsea.tadr.PrimeAssign;
import dev.fmsea.tadr.TADR;
import dev.fmsea.util.Pair;
import soot.Local;

public class OctagonInplaceAddExprRule extends OctagonUpdateRule {

    public boolean canUpdate(TADR expr) {
        return expr instanceof PrimeAssign;
    }

    public Stream<ConstraintThunk> update(TADR expr, Function<Local, Pair<Integer, Integer>> indexer) {
        return update((PrimeAssign)expr, indexer);
    }

    public Stream<ConstraintThunk> update(PrimeAssign expr, Function<Local, Pair<Integer, Integer>> indexer) {
        Local s = expr.variable.variable;
        var sidx = indexer.apply(s);
        return expr.constant.number.upperBound()
            .map(b -> makeUpdate(b, sidx, sidx))
            .orElse(Stream.of());
    }

    @Override
    protected Stream<ConstraintThunk> makeUpdate(Integer bound, Pair<Integer, Integer> s, Pair<Integer, Integer> t) {
        Constraint c = Constraint.of(bound);
        return Stream.of(ConstraintInplaceThunk.of(s.fst(), s.snd(), c));
    }

    @Override
    public String toString() {
        return "Octagon Reassignment Sum Rule (x' ≤ x + c)";
    }
}
