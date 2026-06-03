package dev.fmsea.inference.rewrite.rules.canonicalization;

import dev.fmsea.inference.InvariantExpression;
import dev.fmsea.inference.GtInvariant;

public class GtToGeqRewriteRule extends CanonicalizationRule {

    public InvariantExpression visit(GtInvariant gt) {
        InvariantExpression left = gt.left.accept(this);
        InvariantExpression right = gt.right.accept(this);
        return InvariantExpression.newGeqInv(
            left,
            InvariantExpression.newSum(right, InvariantExpression.newNumeral(1))
        );
    }

    public String toString() {
        return "< → ≤";
    }
}
