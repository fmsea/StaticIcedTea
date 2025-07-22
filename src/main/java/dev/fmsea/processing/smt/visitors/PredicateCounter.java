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

public class PredicateCounter implements SmtExpression.Visitor<Integer> {

    public Integer visitFalse(FalseSmtExpression falsy) {
        return 1;
    }

    public Integer visitTrue(TrueSmtExpression truthy) {
        return 1;
    }

    public Integer visitAndExpr(AndSmtExpression expr) {
        return expr.expressions.stream()
            .map(xpr -> xpr.accept(this))
            .reduce((a, b) -> a + b)
            .orElse(0);
    }

    public Integer visitOrExpr(OrSmtExpression expr) {
        return expr.expressions.stream()
            .map(xpr -> xpr.accept(this))
            .reduce((a, b) -> a + b)
            .orElse(0);
    }

    public Integer visitAdditionExpr(AdditionSmtExpression expr) {
        return expr.left.accept(this) + expr.right.accept(this);
    }

    public Integer visitSubtractionExpr(SubtractionSmtExpression expr) {
        return expr.left.accept(this) + expr.right.accept(this);
    }

    public Integer visitMultiplicationExpr(MultiplicationSmtExpression expr) {
        return expr.left.accept(this) + expr.right.accept(this);
    }

    public Integer visitDivisionExpr(DivisionSmtExpression expr) {
        return expr.left.accept(this) + expr.right.accept(this);
    }

    public Integer visitModulusExpr(ModulusSmtExpression expr) {
        return expr.left.accept(this) + expr.right.accept(this);
    }

    public Integer visitEqExpr(EqSmtExpression expr) {
        return 1;
    }

    public Integer visitLeExpr(LeSmtExpression expr) {
        return 1;
    }

    public Integer visitLtExpr(LtSmtExpression expr) {
        return 1;
    }

    public Integer visitGeExpr(GeSmtExpression expr) {
        return 1;
    }

    public Integer visitGtExpr(GtSmtExpression expr) {
        return 1;
    }

    public Integer visitNegExpr(NegSmtExpression expr) {
        return expr.expr.accept(this);
    }

    public Integer visitNotExpr(NotSmtExpression expr) {
        return expr.expr.accept(this);
    }

    public Integer visitIdentifier(Identifier identifier) {
        return 0;
    }

    public Integer visitNumber(Number number) {
        return 0;
    }
}
