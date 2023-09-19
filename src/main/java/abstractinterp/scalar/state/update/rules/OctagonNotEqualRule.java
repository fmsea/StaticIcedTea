package abstractinterp.scalar.state.update.rules;

import java.util.function.Function;
import java.util.stream.Stream;

import abstractinterp.scalar.state.ConstraintThunk;
import soot.Local;
import tadr.EqCmp;
import tadr.NeCmp;
import tadr.NotOp;
import tadr.TADR;
import util.Pair;

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
