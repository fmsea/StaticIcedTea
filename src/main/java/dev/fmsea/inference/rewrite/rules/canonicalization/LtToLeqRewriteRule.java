package dev.fmsea.inference.rewrite.rules.canonicalization;

import dev.fmsea.inference.InvariantExpression;
import dev.fmsea.inference.LtInvariant;

public class LtToLeqRewriteRule extends CanonicalizationRule {

    public InvariantExpression visit(LtInvariant lt) {
        InvariantExpression left = lt.left.accept(this);
        InvariantExpression right = lt.right.accept(this);
        return InvariantExpression.newLeqInv(
            left,
            InvariantExpression.newDiff(right, InvariantExpression.newNumeral(1))
        );
    }

    public String toString() {
        return "< → ≤";
    }
}
