package dev.fmsea.inference;

import dev.fmsea.common.Locals;
import dev.fmsea.inference.visitors.ToStringVisitor;

public abstract class InvariantExpression {

    public interface Visitor<R> {
        R visit(NegIdentifier negIdentifier);
        R visit(Identifier identifier);
        R visit(Numeral numeral);
        R visit(SumExpression expr);
        R visit(DiffExpression expr);
        R visit(LtInvariant lt);
        R visit(GtInvariant gt);
        R visit(LeqInvariant leq);
        R visit(GeqInvariant geq);
        R visit(EqInvariant eq);
    }

    public abstract <R> R accept(Visitor<R> visitor);

    public String toString() {
        return this.accept(new ToStringVisitor());
    }

    public static Numeral newNumeral(int value) {
        return new Numeral(value);
    }

    public static Identifier newIdentifier(String identifier) {
        return new Identifier(Locals.get(identifier));
    }

    public static NegIdentifier newNegIdentifier(Identifier identifier) {
        return new NegIdentifier(identifier);
    }

    public static SumExpression newSum(InvariantExpression left, InvariantExpression right) {
        return new SumExpression(left, right);
    }

    public static DiffExpression newDiff(InvariantExpression left, InvariantExpression right) {
        return new DiffExpression(left, right);
    }

    public static LtInvariant newLtInv(InvariantExpression left, InvariantExpression right) {
        return new LtInvariant(left, right);
    }

    public static GtInvariant newGtInv(InvariantExpression left, InvariantExpression right) {
        return new GtInvariant(left, right);
    }

    public static LeqInvariant newLeqInv(InvariantExpression left, InvariantExpression right) {
        return new LeqInvariant(left, right);
    }

    public static GeqInvariant newGeqInv(InvariantExpression left, InvariantExpression right) {
        return new GeqInvariant(left, right);
    }

    public static EqInvariant newEqInv(InvariantExpression left, InvariantExpression right) {
        return new EqInvariant(left, right);
    }
}
