package abstractinterp.scalar.state.update;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import abstractinterp.scalar.state.ConstraintForgetThunk;
import abstractinterp.scalar.state.ConstraintThunk;
import abstractinterp.scalar.state.Interval32Box;
import abstractinterp.scalar.state.update.rules.OctagonDiffExprRule;
import abstractinterp.scalar.state.update.rules.OctagonInplaceAddExprRule;
import abstractinterp.scalar.state.update.rules.OctagonIntervalGeUpdateRule;
import abstractinterp.scalar.state.update.rules.OctagonIntervalLeUpdateRule;
import abstractinterp.scalar.state.update.rules.OctagonNegativeDiffExprRule;
import abstractinterp.scalar.state.update.rules.OctagonNotEqualRule;
import abstractinterp.scalar.state.update.rules.OctagonSumExprRule;
import abstractinterp.scalar.state.update.rules.OctagonUpdateRule;
import soot.Local;
import tadr.BinaryOp;
import tadr.TADR;
import tadr.Variable;
import tadr.visitors.ReassignmentVisitor;
import util.Pair;

public class DefaultOctagonUpdater extends OctagonUpdater {

    private final ReassignmentVisitor reassignmentQuery = new ReassignmentVisitor();

    public DefaultOctagonUpdater() {
        this(Set.of(
            new OctagonNotEqualRule(),
            new OctagonInplaceAddExprRule(),
            new OctagonSumExprRule(),
            new OctagonDiffExprRule(),
            new OctagonNegativeDiffExprRule(),
            new OctagonIntervalGeUpdateRule(),
            new OctagonIntervalLeUpdateRule()));
    }

    public DefaultOctagonUpdater(Set<OctagonUpdateRule> rules) {
        super(rules);
    }

    @Override
    public Stream<ConstraintThunk> update(TADR expr,
        Function<Local, Interval32Box> env,
        Function<Local, Pair<Integer, Integer>> indexer) {

        BinaryOp bop = (BinaryOp)expr;
        Local left = ((Variable)bop.left).variable;
        if (expr.accept(reassignmentQuery)) {
            return super.update(expr, env, indexer);
        } else {
            return Stream.concat(this.forgetConstraints(indexer.apply(left)),
                super.update(expr, env, indexer));
        }
    }

    protected Stream<ConstraintThunk> forgetConstraints(Pair<Integer, Integer> idx) {
        return Stream.of(ConstraintForgetThunk.of(idx.fst(), idx.snd()));
    }
}
