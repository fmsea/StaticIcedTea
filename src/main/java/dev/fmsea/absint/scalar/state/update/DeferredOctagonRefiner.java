package dev.fmsea.absint.scalar.state.update;

import java.util.Set;

import dev.fmsea.absint.scalar.state.update.rules.OctagonDiffExprRule;
import dev.fmsea.absint.scalar.state.update.rules.OctagonIntervalGeUpdateRule;
import dev.fmsea.absint.scalar.state.update.rules.OctagonIntervalLeUpdateRule;
import dev.fmsea.absint.scalar.state.update.rules.OctagonNegativeDiffExprRule;
import dev.fmsea.absint.scalar.state.update.rules.OctagonNotEqualRule;
import dev.fmsea.absint.scalar.state.update.rules.OctagonSumExprRule;

public class DeferredOctagonRefiner extends DefaultOctagonRefiner {

    public DeferredOctagonRefiner() {
        super(Set.of(
            new OctagonNotEqualRule(),
            new OctagonSumExprRule(),
            new OctagonDiffExprRule(),
            new OctagonNegativeDiffExprRule(),
            new OctagonIntervalGeUpdateRule(),
            new OctagonIntervalLeUpdateRule()));
    }
}
