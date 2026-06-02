package dev.fmsea.inference.visitors;

import dev.fmsea.inference.DiffExpression;
import dev.fmsea.inference.EqInvariant;
import dev.fmsea.inference.GeqInvariant;
import dev.fmsea.inference.GtInvariant;
import dev.fmsea.inference.Identifier;
import dev.fmsea.inference.InvariantExpression;
import dev.fmsea.inference.LeqInvariant;
import dev.fmsea.inference.LtInvariant;
import dev.fmsea.inference.Numeral;
import dev.fmsea.inference.SumExpression;

public class ToStringVisitor implements InvariantExpression.Visitor<String> {

    public String visit(Identifier identifier) {
        return identifier.identifier.toString();
    }

    public String visit(Numeral numeral) {
        return Integer.valueOf(numeral.value).toString();
    }

    public String visit(SumExpression sum) {
        return String.format(
            "%s + %s",
            sum.left.accept(this),
            sum.right.accept(this));
    }

    public String visit(DiffExpression diff) {
        return String.format(
            "%s - %s",
            diff.left.accept(this),
            diff.right.accept(this));
    }

    public String visit(LtInvariant lt) {
        return String.format(
            "%s < %s",
            lt.left.accept(this),
            lt.right.accept(this));
    }

    public String visit(GtInvariant gt) {
        return String.format(
            "%s > %s",
            gt.left.accept(this),
            gt.right.accept(this));
    }

    public String visit(LeqInvariant leq) {
        return String.format(
            "%s <= %s",
            leq.left.accept(this),
            leq.right.accept(this));
    }

    public String visit(GeqInvariant geq) {
        return String.format(
            "%s >= %s",
            geq.left.accept(this),
            geq.right.accept(this));
    }

    public String visit(EqInvariant eq) {
        return String.format(
            "%s == %s",
            eq.left.accept(this),
            eq.right.accept(this));
    }
}
