package abstractinterp.scalar.state.update;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import abstractinterp.scalar.state.ConstraintThunk;
import abstractinterp.scalar.state.Interval32Box;
import abstractinterp.scalar.state.update.rules.OctagonDiffExprRule;
import abstractinterp.scalar.state.update.rules.OctagonIntervalGeUpdateRule;
import abstractinterp.scalar.state.update.rules.OctagonIntervalLeUpdateRule;
import abstractinterp.scalar.state.update.rules.OctagonNegativeDiffExprRule;
import abstractinterp.scalar.state.update.rules.OctagonNotEqualRule;
import abstractinterp.scalar.state.update.rules.OctagonSumExprRule;
import abstractinterp.scalar.state.update.rules.OctagonUpdateRule;
import soot.Local;
import tadr.TADR;
import util.Pair;

public class DefaultOctagonRefiner extends OctagonUpdater {

    public DefaultOctagonRefiner() {
        this(Set.of(
            new OctagonNotEqualRule(),
            new OctagonSumExprRule(),
            new OctagonDiffExprRule(),
            new OctagonNegativeDiffExprRule(),
            new OctagonIntervalGeUpdateRule(),
            new OctagonIntervalLeUpdateRule()));
    }

    public DefaultOctagonRefiner(Set<OctagonUpdateRule> rules) {
        super(rules);
    }

    @Override
    public Stream<ConstraintThunk> update(TADR expr,
        Function<Local, Interval32Box> env,
        Function<Local, Pair<Integer, Integer>> indexer) {
        return super.update(expr, env, indexer);
    }
}
