package dev.fmsea.inference.rewrite.rules.normalization;

import dev.fmsea.inference.DiffExpression;
import dev.fmsea.inference.GeqInvariant;
import dev.fmsea.inference.Identifier;
import dev.fmsea.inference.InvariantExpression;
import dev.fmsea.inference.Numeral;

public class GeqToLeqRewriteRule extends NormalizationRule {

    public InvariantExpression visit(GeqInvariant geq) {
        if (geq.left instanceof Identifier && geq.right instanceof Identifier) {
            return InvariantExpression.newLeqInv(
                geq.right.accept(this),
                geq.left.accept(this));
        } else if (geq.left instanceof DiffExpression && geq.right instanceof Numeral) {
            DiffExpression diff = (DiffExpression)geq.left;
            Numeral right = (Numeral)geq.right.accept(this);
            return InvariantExpression.newLeqInv(
                InvariantExpression.newDiff(diff.right.accept(this), diff.left.accept(this)),
                InvariantExpression.newNumeral(right.value * -1)
            );
        } else {
            return geq;
        }
    }

    public String toString() {
        return "≥ → ≤";
    }
}
