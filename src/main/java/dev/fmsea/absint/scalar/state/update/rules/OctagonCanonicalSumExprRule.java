package dev.fmsea.absint.scalar.state.update.rules;

import java.util.function.Function;
import java.util.stream.Stream;

import dev.fmsea.absint.scalar.state.Constraint;
import dev.fmsea.absint.scalar.state.ConstraintThunk;
import dev.fmsea.absint.scalar.state.ConstraintUpdateThunk;
import dev.fmsea.absint.scalar.state.Interval32Box;
import soot.Local;
import dev.fmsea.tadr.AdditionOp;
import dev.fmsea.tadr.LeCmp;
import dev.fmsea.tadr.SubtractionOp;
import dev.fmsea.tadr.TADR;
import dev.fmsea.tadr.Value;
import dev.fmsea.tadr.Variable;
import dev.fmsea.util.Pair;

public class OctagonCanonicalSumExprRule extends OctagonUpdateRule {

    public boolean canUpdate(TADR expr) {
        if (expr instanceof LeCmp) {
            var le = (LeCmp)expr;
            if (le.left instanceof AdditionOp && le.right instanceof Value) {
                var left = (AdditionOp)le.left;
                return left.left instanceof Variable && left.right instanceof Variable;
            } else if (le.left instanceof SubtractionOp && le.right instanceof Value) {
                var left = (SubtractionOp)le.left;
                return left.left instanceof Variable && left.right instanceof Variable;
            }
        }
        return false;
    }

    public Stream<ConstraintThunk> update(TADR expr, Function<Local, Pair<Integer, Integer>> indexer) {
        return update((LeCmp)expr, indexer);
    }

    public Stream<ConstraintThunk> update(LeCmp expr, Function<Local, Pair<Integer, Integer>> indexer) {
        if (expr.left instanceof AdditionOp) {
            var left = (AdditionOp)expr.left;
            var val = ((Value)expr.right).number;
            Local s = ((Variable)left.left).variable;
            Local t = ((Variable)left.right).variable;
            var sidx = indexer.apply(s);
            var tidx = indexer.apply(t);
            return val.lowerBound()
                .map(b -> Stream.<ConstraintThunk>of(
                    ConstraintUpdateThunk.of(sidx.fst(), tidx.snd(), Constraint.of(b)),
                    ConstraintUpdateThunk.of(tidx.fst(), sidx.snd(), Constraint.of(b))
                )).orElse(Stream.of());
        } else if (expr.left instanceof SubtractionOp) {
            var left = (SubtractionOp)expr.left;
            var val = ((Value)expr.right).number;
            Local s = ((Variable)left.left).variable;
            Local t = ((Variable)left.right).variable;
            Pair<Integer, Integer> sidx = indexer.apply(s);
            Pair<Integer, Integer> tidx = indexer.apply(t);
            return val.lowerBound()
                .map(b -> makeUpdate(b, sidx, tidx)).orElse(Stream.of());
        } else {
            LOGGER.error("We should not have gotten to this point: {}", expr);
            return Stream.of();
        }
    }

    @Override
    protected Stream<ConstraintThunk> makeUpdate(Integer bound, Pair<Integer, Integer> s, Pair<Integer, Integer> t) {
        return Stream.of(
            ConstraintUpdateThunk.of(s.fst(), t.fst(), Constraint.of(bound)),
            ConstraintUpdateThunk.of(t.snd(), s.snd(), Constraint.of(bound)));
    }

    @Override
    public String toString() {
        return "Octagon Difference Expression Rule (x - y ≤ c)";
    }
}
