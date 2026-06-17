package dev.fmsea.absint.scalar.state.update;

import java.util.Set;

import dev.fmsea.absint.scalar.state.update.rules.OctagonDiffExprRule;
import dev.fmsea.absint.scalar.state.update.rules.OctagonIntervalGeUpdateRule;
import dev.fmsea.absint.scalar.state.update.rules.OctagonIntervalLeUpdateRule;
import dev.fmsea.absint.scalar.state.update.rules.OctagonNegativeDiffExprRule;
import dev.fmsea.absint.scalar.state.update.rules.OctagonNotEqualRule;
import dev.fmsea.absint.scalar.state.update.rules.OctagonSumExprRule;
import dev.fmsea.absint.scalar.state.update.rules.TrivialExpressionFilterRule;

public class DeferredIncrementalOctagonRefiner extends IncrementalOctagonRefiner {

    public DeferredIncrementalOctagonRefiner() {
        super(Set.of(
            new TrivialExpressionFilterRule(),
            new OctagonNotEqualRule(),
            new OctagonSumExprRule(),
            new OctagonDiffExprRule(),
            new OctagonNegativeDiffExprRule(),
            new OctagonIntervalGeUpdateRule(),
            new OctagonIntervalLeUpdateRule()));
    }
}
