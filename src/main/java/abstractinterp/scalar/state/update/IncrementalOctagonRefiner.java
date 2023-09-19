package abstractinterp.scalar.state.update;

import java.util.Set;

import abstractinterp.scalar.state.update.rules.OctagonDiffExprRule;
import abstractinterp.scalar.state.update.rules.OctagonIntervalGeUpdateRule;
import abstractinterp.scalar.state.update.rules.OctagonIntervalLeUpdateRule;
import abstractinterp.scalar.state.update.rules.OctagonNegativeDiffExprRule;
import abstractinterp.scalar.state.update.rules.OctagonNotEqualRule;
import abstractinterp.scalar.state.update.rules.OctagonSumExprRule;
import abstractinterp.scalar.state.update.rules.OctagonUpdateRule;

public class IncrementalOctagonRefiner extends DefaultOctagonRefiner {

    public IncrementalOctagonRefiner() {
        this(Set.of(
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
