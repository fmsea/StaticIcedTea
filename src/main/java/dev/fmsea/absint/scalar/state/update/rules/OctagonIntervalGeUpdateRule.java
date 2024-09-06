package dev.fmsea.absint.scalar.state.update.rules;

import java.util.function.Function;
import java.util.stream.Stream;

import dev.fmsea.absint.scalar.state.Constraint;
import dev.fmsea.absint.scalar.state.ConstraintThunk;
import dev.fmsea.absint.scalar.state.ConstraintUpdateThunk;
import dev.fmsea.absint.scalar.state.Interval32Box;
import soot.Local;
import dev.fmsea.tadr.GeCmp;
import dev.fmsea.tadr.NegOp;
import dev.fmsea.tadr.TADR;
import dev.fmsea.tadr.Value;
import dev.fmsea.tadr.Variable;
import dev.fmsea.util.Pair;

public class OctagonIntervalGeUpdateRule extends OctagonUpdateRule {

    public boolean canUpdate(TADR expr) {
        return ((expr instanceof GeCmp) &&
                (((GeCmp)expr).left instanceof Variable) &&
                ((((GeCmp)expr).right instanceof Value) ||
                 (((GeCmp)expr).right instanceof NegOp)));
    }

    public Stream<ConstraintThunk> update(TADR expr, Function<Local, Pair<Integer, Integer>> indexer) {
        return update((GeCmp)expr, indexer);
    }

    public Stream<ConstraintThunk> update(GeCmp expr, Function<Local, Pair<Integer, Integer>> indexer) {
        Local l = ((Variable)expr.left).variable;
        var sidx = indexer.apply(l);
        if (expr.right instanceof Value) {
            Interval32Box val = ((Value)expr.right).number;
            return val.lowerBound().map(b -> makeUpdate(b, sidx, sidx)).orElse(Stream.of());
        } else if (expr.right instanceof NegOp) {
            NegOp right = (NegOp)expr.right;
            Interval32Box val = ((Value)right.expr).number.copy().negate();
            return val.lowerBound().map(b -> makeUpdate(b, sidx, sidx)).orElse(Stream.of());
        } else {
            return Stream.of();
        }
    }

    @Override
    protected Stream<ConstraintThunk> makeUpdate(Integer bound, Pair<Integer, Integer> s, Pair<Integer, Integer> t) {
        return Stream.of(ConstraintUpdateThunk.of(s.snd(), s.fst(), Constraint.of(bound * -2)));
    }

    @Override
    public String toString() {
        return "Octagon Interval Upper Bound Rule (x ≥ c)";
    }
}
