package dev.fmsea.tadr.rewrite;

import java.util.Set;

import dev.fmsea.tadr.rewrite.rules.IntervalRewriteRule;

public class BoxRewriter extends Rewriter {

    public BoxRewriter() {
        super(Set.of(new IntervalRewriteRule()));
    }
}
