package dev.fmsea.processing.smt.visitors;

import dev.fmsea.processing.smt.AdditionSmtExpression;
import dev.fmsea.processing.smt.AndSmtExpression;
import dev.fmsea.processing.smt.DivisionSmtExpression;
import dev.fmsea.processing.smt.EqSmtExpression;
import dev.fmsea.processing.smt.FalseSmtExpression;
import dev.fmsea.processing.smt.GeSmtExpression;
import dev.fmsea.processing.smt.GtSmtExpression;
import dev.fmsea.processing.smt.Identifier;
import dev.fmsea.processing.smt.LeSmtExpression;
import dev.fmsea.processing.smt.LtSmtExpression;
import dev.fmsea.processing.smt.ModulusSmtExpression;
import dev.fmsea.processing.smt.MultiplicationSmtExpression;
import dev.fmsea.processing.smt.NegSmtExpression;
import dev.fmsea.processing.smt.NotSmtExpression;
import dev.fmsea.processing.smt.Number;
import dev.fmsea.processing.smt.OrSmtExpression;
import dev.fmsea.processing.smt.SmtExpression;
import dev.fmsea.processing.smt.SubtractionSmtExpression;
import dev.fmsea.processing.smt.TrueSmtExpression;
import soot.Local;

public class ContainsLocalVisitor implements SmtExpression.Visitor<Boolean> {

    private final Local target;

    public ContainsLocalVisitor(Local target) {
        this.target = target;
    }

    public Boolean visitFalse(FalseSmtExpression falsy) {
        return false;
    }

    public Boolean visitTrue(TrueSmtExpression truthy) {
        return false;
    }

    public Boolean visitAndExpr(AndSmtExpression expr) {
        return expr.expressions.stream()
            .map(xpr -> xpr.accept(this))
            .reduce((a, b) -> a || b).orElse(false);
    }

    public Boolean visitOrExpr(OrSmtExpression expr) {
        return expr.expressions.stream()
            .map(xpr -> xpr.accept(this))
            .reduce((a, b) -> a || b).orElse(false);
    }

    public Boolean visitAdditionExpr(AdditionSmtExpression expr) {
        return (expr.left.accept(this) ||
                expr.right.accept(this));
    }

    public Boolean visitSubtractionExpr(SubtractionSmtExpression expr) {
        return (expr.left.accept(this) ||
                expr.right.accept(this));
    }

    public Boolean visitMultiplicationExpr(MultiplicationSmtExpression expr) {
        return (expr.left.accept(this) ||
                expr.right.accept(this));
    }

    public Boolean visitDivisionExpr(DivisionSmtExpression expr) {
        return (expr.left.accept(this) ||
                expr.right.accept(this));
    }

    public Boolean visitModulusExpr(ModulusSmtExpression expr) {
        return (expr.left.accept(this) ||
                expr.right.accept(this));
    }

    public Boolean visitEqExpr(EqSmtExpression expr) {
        return (expr.left.accept(this) ||
                expr.right.accept(this));
    }

    public Boolean visitLeExpr(LeSmtExpression expr) {
        return (expr.left.accept(this) ||
                expr.right.accept(this));
    }

    public Boolean visitLtExpr(LtSmtExpression expr) {
        return (expr.left.accept(this) ||
                expr.right.accept(this));
    }

    public Boolean visitGeExpr(GeSmtExpression expr) {
        return (expr.left.accept(this) ||
                expr.right.accept(this));
    }

    public Boolean visitGtExpr(GtSmtExpression expr) {
        return (expr.left.accept(this) ||
                expr.right.accept(this));
    }

    public Boolean visitNegExpr(NegSmtExpression expr) {
        return expr.expr.accept(this);
    }

    public Boolean visitNotExpr(NotSmtExpression expr) {
        return expr.expr.accept(this);
    }

    public Boolean visitIdentifier(Identifier identifier) {
        return target.equals(identifier.identifier);
    }

    public Boolean visitNumber(Number number) {
        return false;
    }
}
