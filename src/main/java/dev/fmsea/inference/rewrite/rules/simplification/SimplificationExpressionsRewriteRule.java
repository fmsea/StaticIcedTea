package dev.fmsea.inference.rewrite.rules.simplification;

import dev.fmsea.inference.DiffExpression;
import dev.fmsea.inference.InvariantExpression;
import dev.fmsea.inference.Numeral;
import dev.fmsea.inference.SumExpression;

public class SimplificationExpressionsRewriteRule extends SimplificationRule {

    public InvariantExpression visit(DiffExpression diff) {
        if (diff.left instanceof Numeral && diff.right instanceof Numeral) {
            Numeral left = (Numeral)diff.left;
            Numeral right = (Numeral)diff.right;
            return InvariantExpression.newNumeral(left.value - right.value);
        } else {
            return diff;
        }
    }

    public InvariantExpression visit(SumExpression sum) {
        if (sum.left instanceof Numeral && sum.right instanceof Numeral) {
            Numeral left = (Numeral)sum.left;
            Numeral right = (Numeral)sum.right;
            return InvariantExpression.newNumeral(left.value + right.value);
        } else {
            return sum;
        }
    }

    public String toString() {
        return "simplification rule";
    }
}
