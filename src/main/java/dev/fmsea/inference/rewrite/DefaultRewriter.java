package dev.fmsea.inference.rewrite;

import java.util.Set;

import dev.fmsea.inference.rewrite.rules.canonicalization.GtToGeqRewriteRule;
import dev.fmsea.inference.rewrite.rules.canonicalization.LtToLeqRewriteRule;
import dev.fmsea.inference.rewrite.rules.canonicalization.MoveSumsLeftRewriteRule;
import dev.fmsea.inference.rewrite.rules.canonicalization.MoveVariablesLeftRewriteRule;
import dev.fmsea.inference.rewrite.rules.canonicalization.NegIdentRewriteRule;
import dev.fmsea.inference.rewrite.rules.canonicalization.SplitEqRewriteRule;
import dev.fmsea.inference.rewrite.rules.normalization.GeqToLeqRewriteRule;
import dev.fmsea.inference.rewrite.rules.simplification.SimplificationExpressionsRewriteRule;

public class DefaultRewriter extends Rewriter {

    public DefaultRewriter() {
        super(Set.of(
            new GtToGeqRewriteRule(),
            new LtToLeqRewriteRule(),
            new MoveSumsLeftRewriteRule(),
            new MoveVariablesLeftRewriteRule(),
            new NegIdentRewriteRule(),
            new SplitEqRewriteRule(),
            new GeqToLeqRewriteRule(),
            new SimplificationExpressionsRewriteRule()
        ));
    }
}
