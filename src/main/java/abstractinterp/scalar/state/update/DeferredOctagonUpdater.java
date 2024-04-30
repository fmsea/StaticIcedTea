package abstractinterp.scalar.state.update;

import java.util.Set;

import abstractinterp.scalar.state.update.rules.OctagonDiffExprRule;
import abstractinterp.scalar.state.update.rules.OctagonInplaceAddExprRule;
import abstractinterp.scalar.state.update.rules.OctagonIntervalGeUpdateRule;
import abstractinterp.scalar.state.update.rules.OctagonIntervalLeUpdateRule;
import abstractinterp.scalar.state.update.rules.OctagonNegativeDiffExprRule;
import abstractinterp.scalar.state.update.rules.OctagonNotEqualRule;
import abstractinterp.scalar.state.update.rules.OctagonSumExprRule;

public class DeferredOctagonUpdater extends OctagonUpdater {

    public DeferredOctagonUpdater() {
        super(Set.of(
            new OctagonNotEqualRule(),
            new OctagonInplaceAddExprRule(),
            new OctagonSumExprRule(),
            new OctagonDiffExprRule(),
            new OctagonNegativeDiffExprRule(),
            new OctagonIntervalGeUpdateRule(),
            new OctagonIntervalLeUpdateRule()));
    }
}
