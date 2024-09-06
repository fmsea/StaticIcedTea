package dev.fmsea.absint.scalar.state.update.rules;

import java.util.function.Function;
import java.util.stream.Stream;

import dev.fmsea.absint.scalar.state.ConstraintThunk;
import soot.Local;
import dev.fmsea.tadr.EqCmp;
import dev.fmsea.tadr.NeCmp;
import dev.fmsea.tadr.NotOp;
import dev.fmsea.tadr.TADR;
import dev.fmsea.util.Pair;

public class OctagonNotEqualRule extends OctagonUpdateRule {

    public boolean canUpdate(TADR expr) {
        return ((expr instanceof NeCmp) ||
                ((expr instanceof NotOp) &&
                 (((NotOp)expr).expr instanceof EqCmp)));
    }

    public Stream<ConstraintThunk> update(TADR expr, Function<Local, Pair<Integer, Integer>> indexer) {
        return update((NeCmp)expr, indexer);
    }

    public Stream<ConstraintThunk> update(NeCmp expr, Function<Local, Pair<Integer, Integer>> indexer) {
        return Stream.of();
    }

    @Override
    protected Stream<ConstraintThunk> makeUpdate(Integer bound, Pair<Integer, Integer> s, Pair<Integer, Integer> t) {
        return Stream.of();
    }

    @Override
    public String toString() {
        return "Octagon (not (= e1 e2))";
    }
}
