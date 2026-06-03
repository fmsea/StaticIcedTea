package dev.fmsea.inference.rewrite.rules.canonicalization;

import dev.fmsea.inference.EqInvariant;
import dev.fmsea.inference.InvariantExpression;

public class SplitEqRewriteRule extends CanonicalizationRule {

    public InvariantExpression visit(EqInvariant eq) {
        InvariantExpression left = eq.left.accept(this);
        InvariantExpression right = eq.right.accept(this);
        return InvariantExpression.newAnd(
            InvariantExpression.newLeqInv(left, right),
            InvariantExpression.newGeqInv(left, right)
        );
    }

    public String toString() {
        return "Split Equal Rewrite Rule";
    }
}
