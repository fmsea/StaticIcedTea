package dev.fmsea.inference.rewrite.rules.normalization;

import dev.fmsea.inference.GeqInvariant;
import dev.fmsea.inference.InvariantExpression;
import dev.fmsea.inference.rewrite.rules.RewritePhase;
import dev.fmsea.inference.rewrite.rules.RewriteRule;

public abstract class NormalizationRule extends RewriteRule {

    public RewritePhase rewritePhase() {
        return RewritePhase.NORMALIZATION;
    }
}
