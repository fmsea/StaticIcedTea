package dev.fmsea.absint.scalar.state.update;

import java.util.Set;

import dev.fmsea.absint.scalar.state.update.rules.OctagonDiffExprRule;
import dev.fmsea.absint.scalar.state.update.rules.OctagonIntervalGeUpdateRule;
import dev.fmsea.absint.scalar.state.update.rules.OctagonIntervalLeUpdateRule;
import dev.fmsea.absint.scalar.state.update.rules.OctagonNegativeDiffExprRule;
import dev.fmsea.absint.scalar.state.update.rules.OctagonNotEqualRule;
import dev.fmsea.absint.scalar.state.update.rules.OctagonSumExprRule;
import dev.fmsea.absint.scalar.state.update.rules.OctagonUpdateRule;
import dev.fmsea.absint.scalar.state.update.rules.TrivialExpressionFilterRule;

public class IncrementalOctagonRefiner extends DefaultOctagonRefiner {

    public IncrementalOctagonRefiner() {
        this(Set.of(
            new TrivialExpressionFilterRule(),
            new OctagonNotEqualRule(),
            new OctagonSumExprRule(),
            new OctagonDiffExprRule(),
            new OctagonNegativeDiffExprRule(),
            new OctagonIntervalGeUpdateRule(),
            new OctagonIntervalLeUpdateRule()));
    }

    public IncrementalOctagonRefiner(Set<OctagonUpdateRule> rules) {
        super(rules);
    }
}
