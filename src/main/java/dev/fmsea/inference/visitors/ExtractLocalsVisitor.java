package dev.fmsea.inference.visitors;

import java.util.Set;

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
import dev.fmsea.util.Sets;
import soot.Local;

public class ExtractLocalsVisitor implements InvariantExpression.Visitor<Set<Local>> {

    public Set<Local> visit(AndExpression and) {
        return and.asConjuncts()
            .map(expr -> expr.accept(this))
            .reduce(Sets::union)
            .orElse(Set.of());
    }

    public Set<Local> visit(SumExpression sum) {
        Set<Local> left = sum.left.accept(this);
        Set<Local> right = sum.right.accept(this);
        return Sets.union(left, right);
    }

    public Set<Local> visit(DiffExpression diff) {
        Set<Local> left = diff.left.accept(this);
        Set<Local> right = diff.right.accept(this);
        return Sets.union(left, right);
    }

    public Set<Local> visit(LtInvariant lt) {
        Set<Local> left = lt.left.accept(this);
        Set<Local> right = lt.right.accept(this);
        return Sets.union(left, right);
    }

    public Set<Local> visit(GtInvariant gt) {
        Set<Local> left = gt.left.accept(this);
        Set<Local> right = gt.right.accept(this);
        return Sets.union(left, right);
    }

    public Set<Local> visit(LeqInvariant leq) {
        Set<Local> left = leq.left.accept(this);
        Set<Local> right = leq.right.accept(this);
        return Sets.union(left, right);
    }

    public Set<Local> visit(GeqInvariant geq) {
        Set<Local> left = geq.left.accept(this);
        Set<Local> right = geq.right.accept(this);
        return Sets.union(left, right);
    }

    public Set<Local> visit(EqInvariant eq) {
        Set<Local> left = eq.left.accept(this);
        Set<Local> right = eq.right.accept(this);
        return Sets.union(left, right);
    }

    public Set<Local> visit(NegIdentifier negIdentifier) {
        return Set.of(negIdentifier.identifier.identifier);
    }

    public Set<Local> visit(Identifier identifier) {
        return Set.of(identifier.identifier);
    }

    public Set<Local> visit(Numeral numeral) {
        return Set.of();
    }
}
