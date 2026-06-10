package dev.fmsea.inference.updates;

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
import dev.fmsea.tadr.TADR;

public class TadrRewriter implements InvariantExpression.Visitor<TADR> {

    public TADR visit(AndExpression and) {
        throw new RuntimeException("TADR does not directly support AND expressions");
    }

    public TADR visit(SumExpression sum) {
        TADR left = sum.left.accept(this);
        TADR right = sum.right.accept(this);
        return TADR.newAddExpr(left, right);
    }

    public TADR visit(DiffExpression diff) {
        TADR left = diff.left.accept(this);
        TADR right = diff.right.accept(this);
        return TADR.newSubExpr(left, right);
    }

    public TADR visit(LeqInvariant leq) {
        TADR left = leq.left.accept(this);
        TADR right = leq.right.accept(this);

        return TADR.newLeExpr(left, right);
    }

    public TADR visit(LtInvariant lt) {
        throw new RuntimeException("LT: You should have rewritten this...");
    }

    public TADR visit(GtInvariant gt) {
        throw new RuntimeException("GT: You should have rewritten this...");
    }

    public TADR visit(EqInvariant eq) {
        throw new RuntimeException("EQ: You should have rewritten this...");
    }

    public TADR visit(GeqInvariant geq) {
        TADR left = geq.left.accept(this);
        TADR right = geq.right.accept(this);

        return TADR.newGeExpr(left, right);
    }

    public TADR visit(Identifier identifier) {
        return TADR.newVariable(identifier.identifier);
    }

    public TADR visit(NegIdentifier negIdentifier) {
        throw new RuntimeException("NegIdent: You should have rewritten this...");
    }

    public TADR visit(Numeral numeral) {
        return TADR.newValue(numeral.value);
    }
}
