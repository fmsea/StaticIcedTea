package dev.fmsea.absint.scalar.state.update;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import dev.fmsea.absint.scalar.state.ConstraintThunk;
import dev.fmsea.absint.scalar.state.Interval32Box;
import dev.fmsea.absint.scalar.state.update.rules.OctagonDiffExprRule;
import dev.fmsea.absint.scalar.state.update.rules.OctagonIntervalGeUpdateRule;
import dev.fmsea.absint.scalar.state.update.rules.OctagonIntervalLeUpdateRule;
import dev.fmsea.absint.scalar.state.update.rules.OctagonNegativeDiffExprRule;
import dev.fmsea.absint.scalar.state.update.rules.OctagonNotEqualRule;
import dev.fmsea.absint.scalar.state.update.rules.OctagonSumExprRule;
import dev.fmsea.absint.scalar.state.update.rules.OctagonUpdateRule;
import dev.fmsea.tadr.TADR;
import dev.fmsea.util.Pair;
import soot.Local;

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
