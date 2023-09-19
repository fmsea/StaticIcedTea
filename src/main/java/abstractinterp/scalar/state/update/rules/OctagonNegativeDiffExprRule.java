package abstractinterp.scalar.state.update.rules;

import java.util.function.Function;
import java.util.stream.Stream;

import abstractinterp.scalar.state.Constraint;
import abstractinterp.scalar.state.ConstraintThunk;
import abstractinterp.scalar.state.ConstraintUpdateThunk;
import abstractinterp.scalar.state.Interval32Box;
import soot.Local;
import tadr.GeCmp;
import tadr.SubtractionOp;
import tadr.TADR;
import tadr.Value;
import tadr.Variable;
import util.Pair;

public class OctagonNegativeDiffExprRule extends OctagonUpdateRule {

    public boolean canUpdate(TADR expr) {
        if (expr instanceof GeCmp) {
            var ge = (GeCmp)expr;
            if (ge.left instanceof Variable) {
                if (ge.right instanceof SubtractionOp) {
                    var sub = (SubtractionOp)ge.right;
                    if ((sub.left instanceof Value) &&
                        (sub.right instanceof Variable)) {
                        return !ge.left.equals(sub.right);
                    }
                }
            }
        }
        return false;
    }

    public Stream<ConstraintThunk> update(TADR expr, Function<Local, Pair<Integer, Integer>> indexer) {
        return update((GeCmp)expr, indexer);
    }

    public Stream<ConstraintThunk> update(GeCmp expr, Function<Local, Pair<Integer, Integer>> indexer) {
        var right = (SubtractionOp)expr.right;
        Local s = ((Variable)expr.left).variable;
        Local t = ((Variable)right.right).variable;
        var sidx = indexer.apply(s);
        var tidx = indexer.apply(t);
        Interval32Box val = ((Value)right.left).number.copy().negate();
        return val.upperBound().map(b -> makeUpdate(b, sidx, tidx)).orElse(Stream.of());
    }

    @Override
    protected Stream<ConstraintThunk> makeUpdate(Integer b, Pair<Integer, Integer> s, Pair<Integer, Integer> t) {
        return Stream.of(
            ConstraintUpdateThunk.of(s.snd(), t.fst(), Constraint.of(b)),
            ConstraintUpdateThunk.of(t.snd(), s.fst(), Constraint.of(b))
        );
    }

    @Override
    public String toString() {
        return "Octagon Negative Difference Expression Rule: (-x - y ≤ c)";
    }
}
