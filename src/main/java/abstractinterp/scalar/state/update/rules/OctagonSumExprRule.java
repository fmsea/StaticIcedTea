package abstractinterp.scalar.state.update.rules;

import java.util.function.Function;
import java.util.stream.Stream;

import abstractinterp.scalar.state.Constraint;
import abstractinterp.scalar.state.ConstraintThunk;
import abstractinterp.scalar.state.ConstraintUpdateThunk;
import abstractinterp.scalar.state.Interval32Box;
import soot.Local;
import tadr.LeCmp;
import tadr.SubtractionOp;
import tadr.TADR;
import tadr.Value;
import tadr.Variable;
import util.Pair;

public class OctagonSumExprRule extends OctagonUpdateRule {

    public boolean canUpdate(TADR expr) {
        if (expr instanceof LeCmp) {
            var le = (LeCmp)expr;
            if ((le.left instanceof Variable) &&
                (le.right instanceof SubtractionOp)) {
                var sub = (SubtractionOp)le.right;
                if ((sub.left instanceof Value) &&
                    (sub.right instanceof Variable)) {
                    return !le.left.equals(sub.right);
                }
            }
        }
        return false;
    }

    public Stream<ConstraintThunk> update(TADR expr, Function<Local, Pair<Integer, Integer>> indexer) {
        return update((LeCmp)expr, indexer);
    }

    public Stream<ConstraintThunk> update(LeCmp expr, Function<Local, Pair<Integer, Integer>> indexer) {
        var right = (SubtractionOp)expr.right;
        Local s = ((Variable)expr.left).variable;
        Local t = ((Variable)right.right).variable;
        Interval32Box val = ((Value)right.left).number;
        var sidx = indexer.apply(s);
        var tidx = indexer.apply(t);
        return val.upperBound().map(b -> makeUpdate(b,sidx, tidx)).orElse(Stream.of());
    }

    @Override
    protected Stream<ConstraintThunk> makeUpdate(Integer bound, Pair<Integer, Integer> s, Pair<Integer, Integer> t) {
        return Stream.of(
            ConstraintUpdateThunk.of(s.fst(), t.snd(), Constraint.of(bound)),
            ConstraintUpdateThunk.of(t.fst(), s.snd(), Constraint.of(bound))
        );
    }

    @Override
    public String toString() {
        return "Octagon Sum Rule (x + y ≤ c)";
    }
}
