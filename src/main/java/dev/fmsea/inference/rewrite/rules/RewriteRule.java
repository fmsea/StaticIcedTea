package dev.fmsea.inference.rewrite.rules;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.fmsea.inference.AndExpression;
import dev.fmsea.inference.DiffExpression;
import dev.fmsea.inference.EqInvariant;
import dev.fmsea.inference.GeqInvariant;
import dev.fmsea.inference.GtInvariant;
import dev.fmsea.inference.Identifier;
import dev.fmsea.inference.InvariantExpression;
import dev.fmsea.inference.LeqInvariant;
import dev.fmsea.inference.LtInvariant;
import dev.fmsea.inference.NegIdentifier;
import dev.fmsea.inference.Numeral;
import dev.fmsea.inference.SumExpression;

public abstract class RewriteRule implements InvariantExpression.Visitor<InvariantExpression> {

    protected static Logger LOG = LoggerFactory.getLogger(RewriteRule.class);

    public abstract RewritePhase rewritePhase();

    public int rewritePriority() {
        return 10;
    }

    public InvariantExpression visit(AndExpression and) {
        return InvariantExpression.newAnd(and.asConjuncts()
            .map(expr -> expr.accept(this))
            .collect(Collectors.toList()));
    }

    public InvariantExpression visit(Identifier identifier) {
        return identifier;
    }

    public InvariantExpression visit(NegIdentifier negIdentifier) {
        InvariantExpression ident = negIdentifier.identifier.accept(this);
        if (ident == negIdentifier.identifier) {
            return negIdentifier;
        }

        return InvariantExpression.newNegIdentifier((Identifier)ident);
    }

    public InvariantExpression visit(Numeral numeral) {
        return numeral;
    }

    public InvariantExpression visit(SumExpression sum) {
        InvariantExpression left = sum.left.accept(this);
        InvariantExpression right = sum.right.accept(this);

        if (left == sum.left && right == sum.right) {
            return sum;
        }

        return InvariantExpression.newSum(left, right);
    }

    public InvariantExpression visit(DiffExpression diff) {
        InvariantExpression left = diff.left.accept(this);
        InvariantExpression right = diff.right.accept(this);

        if (left == diff.left && right == diff.right) {
            return diff;
        }

        return InvariantExpression.newDiff(left, right);
    }

    public InvariantExpression visit(LtInvariant lt) {
        InvariantExpression left = lt.left.accept(this);
        InvariantExpression right = lt.right.accept(this);

        if (left == lt.left && right == lt.right) {
            return lt;
        }

        return InvariantExpression.newLtInv(left, right);
    }

    public InvariantExpression visit(GtInvariant gt) {
        InvariantExpression left = gt.left.accept(this);
        InvariantExpression right = gt.right.accept(this);

        if (left == gt.left && right == gt.right) {
            return gt;
        }

        return InvariantExpression.newGtInv(left, right);
    }

    public InvariantExpression visit(LeqInvariant leq) {
        InvariantExpression left = leq.left.accept(this);
        InvariantExpression right = leq.right.accept(this);

        if (left == leq.left && right == leq.right) {
            return leq;
        }

        return InvariantExpression.newLeqInv(left, right);
    }

    public InvariantExpression visit(GeqInvariant geq) {
        InvariantExpression left = geq.left.accept(this);
        InvariantExpression right = geq.right.accept(this);

        if (left == geq.left && right == geq.right) {
            return geq;
        }

        return InvariantExpression.newGeqInv(left, right);
    }

    public InvariantExpression visit(EqInvariant eq) {
        InvariantExpression left = eq.left.accept(this);
        InvariantExpression right = eq.right.accept(this);

        if (left == eq.left && right == eq.right) {
            return eq;
        }

        return InvariantExpression.newEqInv(left, right);
    }
}
