package dev.fmsea.inference.rewrite.rules.canonicalization;

import dev.fmsea.inference.DiffExpression;
import dev.fmsea.inference.GeqInvariant;
import dev.fmsea.inference.Identifier;
import dev.fmsea.inference.InvariantExpression;
import dev.fmsea.inference.LeqInvariant;
import dev.fmsea.inference.Numeral;
import dev.fmsea.inference.SumExpression;

public class MoveSumsLeftRewriteRule extends CanonicalizationRule {

    public InvariantExpression visit(GeqInvariant geq) {
        if (geq.left instanceof Identifier && geq.right instanceof SumExpression) {
            SumExpression sum = (SumExpression)geq.right;
            if (sum.left instanceof Identifier && sum.right instanceof Numeral) {
                return InvariantExpression.newGeqInv(
                    InvariantExpression.newDiff(geq.left.accept(this), sum.left.accept(this)),
                    sum.right.accept(this));
            } else if (sum.left instanceof Numeral && sum.right instanceof Identifier) {
                return InvariantExpression.newGeqInv(
                    InvariantExpression.newDiff(geq.left.accept(this), sum.right.accept(this)),
                    sum.left.accept(this));
            }
        } else if (geq.left instanceof Identifier && geq.right instanceof DiffExpression) {
            DiffExpression diff = (DiffExpression)geq.right;
            if (diff.left instanceof Identifier && diff.right instanceof Numeral) {
                Numeral right = (Numeral)diff.right.accept(this);
                return InvariantExpression.newGeqInv(
                    InvariantExpression.newDiff(geq.left.accept(this), diff.left.accept(this)),
                    InvariantExpression.newNumeral(right.value * -1));
            } else if (diff.left instanceof Numeral && diff.right instanceof Identifier) {
                return InvariantExpression.newGeqInv(
                    InvariantExpression.newSum(geq.left.accept(this), diff.right.accept(this)),
                    diff.left.accept(this));
            }
        }
        return geq;
    }

    public InvariantExpression visit(LeqInvariant leq) {
        if (leq.left instanceof Identifier && leq.right instanceof SumExpression) {
            SumExpression sum = (SumExpression)leq.right;
            if (sum.left instanceof Identifier && sum.right instanceof Numeral) {
                return InvariantExpression.newLeqInv(
                    InvariantExpression.newDiff(leq.left.accept(this), sum.left.accept(this)),
                    sum.right.accept(this)
                );
            } else if (sum.left instanceof Numeral && sum.right instanceof Identifier) {
                return InvariantExpression.newLeqInv(
                    InvariantExpression.newDiff(leq.left.accept(this), sum.right.accept(this)),
                    sum.left.accept(this)
                );
            }
        } else if (leq.left instanceof Identifier && leq.right instanceof DiffExpression) {
            DiffExpression diff = (DiffExpression)leq.right;
            if (diff.left instanceof Identifier && diff.right instanceof Numeral) {
                Numeral right = (Numeral)diff.right.accept(this);
                return InvariantExpression.newLeqInv(
                    InvariantExpression.newDiff(leq.left.accept(this), diff.left.accept(this)),
                    InvariantExpression.newNumeral(right.value * -1));
            } else if (diff.left instanceof Numeral && diff.right instanceof Identifier) {
                return InvariantExpression.newLeqInv(
                    InvariantExpression.newSum(leq.left.accept(this), diff.right.accept(this)),
                    diff.left.accept(this));
            }
        }
        return leq;
    }

    public String toString() {
        return "Move sums to the left";
    }
}
