package dev.fmsea.inference.rewrite.rules.simplification;

import dev.fmsea.inference.rewrite.rules.RewritePhase;
import dev.fmsea.inference.rewrite.rules.RewriteRule;

public abstract class SimplificationRule extends RewriteRule {

    public RewritePhase rewritePhase() {
        return RewritePhase.SIMPLIFICATION;
    }
}
