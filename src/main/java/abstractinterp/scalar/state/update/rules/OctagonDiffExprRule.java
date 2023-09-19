package abstractinterp.scalar.state.update.rules;

import java.util.function.Function;
import java.util.stream.Stream;

import abstractinterp.scalar.state.Constraint;
import abstractinterp.scalar.state.ConstraintThunk;
import abstractinterp.scalar.state.ConstraintUpdateThunk;
import abstractinterp.scalar.state.Interval32Box;
import soot.Local;
import tadr.AdditionOp;
import tadr.LeCmp;
import tadr.SubtractionOp;
import tadr.TADR;
import tadr.Value;
import tadr.Variable;
import util.Pair;

public class OctagonDiffExprRule extends OctagonUpdateRule {

    public boolean canUpdate(TADR expr) {
        if (expr instanceof LeCmp) {
            var le = (LeCmp)expr;
            if (le.left instanceof Variable) {
                if (le.right instanceof SubtractionOp) {
                    var sub = (SubtractionOp)le.right;
                    if ((sub.left instanceof Variable) &&
                        (sub.right instanceof Value)) {
                        return !le.left.equals(sub.left);
                    }
                } else if (le.right instanceof AdditionOp) {
                    var add = (AdditionOp)le.right;
                    if ((add.left instanceof Value) &&
                        (add.right instanceof Variable)) {
                        return !le.left.equals(add.right);
                    } else if ((add.left instanceof Variable) &&
                               (add.right instanceof Value)) {
                        return !le.left.equals(add.left);
                    }
                }
            }
        }
        return false;
    }

    public Stream<ConstraintThunk> update(TADR expr, Function<Local, Pair<Integer, Integer>> indexer) {
        return update((LeCmp)expr, indexer);
    }

    public Stream<ConstraintThunk> update(LeCmp expr, Function<Local, Pair<Integer, Integer>> indexer) {
        Local s = ((Variable)expr.left).variable;
        var sidx = indexer.apply(s);
        if (expr.right instanceof AdditionOp) {
            var right = (AdditionOp)expr.right;
            if (right.left instanceof Variable && right.right instanceof Value) {
                Local t = ((Variable)right.left).variable;
                Interval32Box val = ((Value)right.right).number;
                var tidx = indexer.apply(t);
                return val.upperBound().map(b -> makeUpdate(b, sidx, tidx)).orElse(Stream.of());
            } else if (right.left instanceof Value && right.right instanceof Variable) {
                Local t = ((Variable)right.right).variable;
                Interval32Box val = ((Value)right.left).number;
                var tidx = indexer.apply(t);
                return val.upperBound().map(b -> makeUpdate(b, sidx, tidx)).orElse(Stream.of());
            } else {
                LOGGER.error("We should not have gotten to this point: {}", expr);
                return Stream.of();
            }
        } else if (expr.right instanceof SubtractionOp) {
            var right = (SubtractionOp)expr.right;
            Interval32Box val = ((Value)right.right).number;
            Local t = ((Variable)right.left).variable;
            var tidx = indexer.apply(t);
            return val.lowerBound()
                .map(b -> b * -1)
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
