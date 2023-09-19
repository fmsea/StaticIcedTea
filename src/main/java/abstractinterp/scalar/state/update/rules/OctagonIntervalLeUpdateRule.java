package abstractinterp.scalar.state.update.rules;

import java.util.function.Function;
import java.util.stream.Stream;

import abstractinterp.scalar.state.Constraint;
import abstractinterp.scalar.state.ConstraintThunk;
import abstractinterp.scalar.state.ConstraintUpdateThunk;
import abstractinterp.scalar.state.Interval32Box;
import soot.Local;
import tadr.LeCmp;
import tadr.NegOp;
import tadr.TADR;
import tadr.Value;
import tadr.Variable;
import util.Pair;

public class OctagonIntervalLeUpdateRule extends OctagonUpdateRule {

    public boolean canUpdate(TADR expr) {
        return ((expr instanceof LeCmp) &&
                (((LeCmp)expr).left instanceof Variable) &&
                ((((LeCmp)expr).right instanceof Value) ||
                 (((LeCmp)expr).right instanceof NegOp)));
    }

    public Stream<ConstraintThunk> update(TADR expr, Function<Local, Pair<Integer, Integer>> indexer) {
        return update((LeCmp)expr, indexer);
    }

    public Stream<ConstraintThunk> update(LeCmp expr, Function<Local, Pair<Integer, Integer>> indexer) {
        Local l = ((Variable)expr.left).variable;
        var sidx = indexer.apply(l);
        if (expr.right instanceof Value) {
            Interval32Box val = ((Value)expr.right).number;
            return val.upperBound().map(b -> makeUpdate(b, sidx, sidx)).orElse(Stream.of());
        } else if (expr.right instanceof NegOp) {
            NegOp right = (NegOp)expr.right;
            Interval32Box val = ((Value)right.expr).number.copy().negate();
            return val.upperBound().map(b -> makeUpdate(b, sidx, sidx)).orElse(Stream.of());
        } else {
            return Stream.of();
        }
    }

    @Override
    protected Stream<ConstraintThunk> makeUpdate(Integer bound, Pair<Integer, Integer> s, Pair<Integer, Integer> t) {
        return Stream.of(ConstraintUpdateThunk.of(s.fst(), s.snd(), Constraint.of(bound * 2)));
    }

    @Override
    public String toString() {
        return "Octagon Interval Lower Bound Rule (x ≤ c)";
    }
}
