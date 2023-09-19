package tadr.rewrite;

import java.util.Set;

import tadr.rewrite.rules.IntervalRewriteRule;

public class BoxRewriter extends Rewriter {

    public BoxRewriter() {
        super(Set.of(new IntervalRewriteRule()));
    }
}
