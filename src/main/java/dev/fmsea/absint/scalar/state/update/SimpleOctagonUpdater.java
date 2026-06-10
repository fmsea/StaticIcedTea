package dev.fmsea.absint.scalar.state.update;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import dev.fmsea.absint.scalar.state.ConstraintThunk;
import dev.fmsea.absint.scalar.state.Interval32Box;
import dev.fmsea.absint.scalar.state.update.rules.OctagonCanonicalNegativeExprRule;
import dev.fmsea.absint.scalar.state.update.rules.OctagonCanonicalSumExprRule;
import dev.fmsea.absint.scalar.state.update.rules.OctagonDiffExprRule;
import dev.fmsea.absint.scalar.state.update.rules.OctagonInplaceAddExprRule;
import dev.fmsea.absint.scalar.state.update.rules.OctagonIntervalGeUpdateRule;
import dev.fmsea.absint.scalar.state.update.rules.OctagonIntervalLeUpdateRule;
import dev.fmsea.absint.scalar.state.update.rules.OctagonNegativeDiffExprRule;
import dev.fmsea.absint.scalar.state.update.rules.OctagonNotEqualRule;
import dev.fmsea.absint.scalar.state.update.rules.OctagonSumExprRule;
import dev.fmsea.absint.scalar.state.update.rules.OctagonUpdateRule;
import dev.fmsea.tadr.TADR;
import dev.fmsea.util.Pair;
import soot.Local;

public class SimpleOctagonUpdater extends OctagonUpdater {

    public SimpleOctagonUpdater() {
        this(Set.of(
            new OctagonNotEqualRule(),
            new OctagonInplaceAddExprRule(),
            new OctagonCanonicalSumExprRule(),
            new OctagonSumExprRule(),
            new OctagonDiffExprRule(),
            new OctagonNegativeDiffExprRule(),
            new OctagonCanonicalNegativeExprRule(),
            new OctagonIntervalGeUpdateRule(),
            new OctagonIntervalLeUpdateRule()));
    }

    public SimpleOctagonUpdater(Set<OctagonUpdateRule> rules) {
        super(rules);
    }

    @Override
    public Stream<ConstraintThunk> update(TADR expr,
        Function<Local, Interval32Box> env,
        Function<Local, Pair<Integer, Integer>> indexer) {
        return update(Stream.of(expr), indexer);
    }
}
