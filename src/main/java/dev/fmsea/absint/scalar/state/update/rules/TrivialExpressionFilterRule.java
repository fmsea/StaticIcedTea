package dev.fmsea.absint.scalar.state.update.rules;

import java.util.function.Function;
import java.util.stream.Stream;

import dev.fmsea.absint.scalar.state.ConstraintThunk;
import dev.fmsea.tadr.BinaryOp;
import dev.fmsea.tadr.TADR;
import dev.fmsea.tadr.Value;
import dev.fmsea.util.Pair;
import soot.Local;

public class TrivialExpressionFilterRule extends OctagonUpdateRule {

    public boolean canUpdate(TADR expr) {
        if (expr instanceof BinaryOp) {
            BinaryOp op = (BinaryOp)expr;
            return (op.left instanceof Value && op.right instanceof Value);
        }
        return false;
    }

    public Stream<ConstraintThunk> update(TADR expr, Function<Local, Pair<Integer, Integer>> indexer) {
        return Stream.of();
    }

    protected Stream<ConstraintThunk> makeUpdate(Integer bound, Pair<Integer, Integer> s, Pair<Integer, Integer> t) {
        return Stream.of();
    }

    @Override
    public String toString() {
        return "Trivial expression filter";
    }
}
