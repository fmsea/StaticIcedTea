package dev.fmsea.inference.rewrite.rules.canonicalization;

import dev.fmsea.inference.rewrite.rules.RewritePhase;
import dev.fmsea.inference.rewrite.rules.RewriteRule;

public abstract class CanonicalizationRule extends RewriteRule {
    public RewritePhase rewritePhase() {
        return RewritePhase.CANONICALIZE;
    }
}
