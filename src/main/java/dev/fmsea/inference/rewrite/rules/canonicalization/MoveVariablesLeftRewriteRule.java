package dev.fmsea.inference.rewrite.rules.canonicalization;

import dev.fmsea.inference.GeqInvariant;
import dev.fmsea.inference.Identifier;
import dev.fmsea.inference.InvariantExpression;
import dev.fmsea.inference.LeqInvariant;

public class MoveVariablesLeftRewriteRule extends CanonicalizationRule {

    public InvariantExpression visit(GeqInvariant geq) {
        if (geq.left instanceof Identifier && geq.right instanceof Identifier) {
            return InvariantExpression.newGeqInv(
                InvariantExpression.newDiff(geq.left.accept(this), geq.right.accept(this)),
                InvariantExpression.newNumeral(0)
            );
        }
        return geq;
    }

    public InvariantExpression visit(LeqInvariant leq) {
        if (leq.left instanceof Identifier && leq.right instanceof Identifier) {
            return InvariantExpression.newLeqInv(
                InvariantExpression.newDiff(leq.left.accept(this), leq.right.accept(this)),
                InvariantExpression.newNumeral(0)
            );
        }
        return leq;
    }

    public String toString() {
        return "Move sums to the left";
    }
}
