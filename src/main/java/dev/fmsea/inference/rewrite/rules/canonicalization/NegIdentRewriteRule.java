package dev.fmsea.inference.rewrite.rules.canonicalization;

import dev.fmsea.inference.DiffExpression;
import dev.fmsea.inference.Identifier;
import dev.fmsea.inference.InvariantExpression;
import dev.fmsea.inference.LeqInvariant;
import dev.fmsea.inference.NegIdentifier;
import dev.fmsea.inference.Numeral;

public class NegIdentRewriteRule extends CanonicalizationRule {

    public InvariantExpression visit(LeqInvariant leq) {
        if (leq.left instanceof DiffExpression && leq.right instanceof Numeral) {
            DiffExpression left = (DiffExpression)leq.left;
            Numeral right = (Numeral)leq.right;
            if (left.left instanceof NegIdentifier && left.right instanceof Identifier) {
                Identifier x = ((NegIdentifier)left.left).identifier;
                Identifier y = (Identifier)left.right;
                return InvariantExpression.newGeqInv(
                    InvariantExpression.newSum(x, y),
                    InvariantExpression.newNumeral(right.value * -1)
                );
            }
        }
        return leq;
    }

    public String toString() {
        return "-x - y <= c → x + y >= -c";
    }
}
