package dev.fmsea.processing.smt.visitors;

import java.util.Set;
import java.util.stream.Collectors;

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
import dev.fmsea.util.Sets;
import soot.Local;

public class LocalsVisitor implements SmtExpression.Visitor<Set<Local>> {
    public Set<Local> visitFalse(FalseSmtExpression falsy) {
        return Set.of();
    }

    public Set<Local> visitTrue(TrueSmtExpression truthy) {
        return Set.of();
    }

    public Set<Local> visitAndExpr(AndSmtExpression expr) {
        return Sets.union(expr.expressions.stream()
            .map(xpr -> xpr.accept(this))
            .collect(Collectors.toList()));
    }

    public Set<Local> visitOrExpr(OrSmtExpression expr) {
        return Sets.union(expr.expressions.stream()
            .map(xpr -> xpr.accept(this))
            .collect(Collectors.toList()));
    }

    public Set<Local> visitAdditionExpr(AdditionSmtExpression expr) {
        return Sets.union(
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public Set<Local> visitSubtractionExpr(SubtractionSmtExpression expr) {
        return Sets.union(
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public Set<Local> visitMultiplicationExpr(MultiplicationSmtExpression expr) {
        return Sets.union(
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public Set<Local> visitDivisionExpr(DivisionSmtExpression expr) {
        return Sets.union(
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public Set<Local> visitModulusExpr(ModulusSmtExpression expr) {
        return Sets.union(
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public Set<Local> visitEqExpr(EqSmtExpression expr) {
        return Sets.union(
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public Set<Local> visitLeExpr(LeSmtExpression expr) {
        return Sets.union(
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public Set<Local> visitLtExpr(LtSmtExpression expr) {
        return Sets.union(
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public Set<Local> visitGeExpr(GeSmtExpression expr) {
        return Sets.union(
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public Set<Local> visitGtExpr(GtSmtExpression expr) {
        return Sets.union(
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public Set<Local> visitNegExpr(NegSmtExpression expr) {
        return expr.expr.accept(this);
    }

    public Set<Local> visitNotExpr(NotSmtExpression expr) {
        return expr.expr.accept(this);
    }

    public Set<Local> visitIdentifier(Identifier identifier) {
        return Set.of(identifier.identifier);
    }

    public Set<Local> visitNumber(Number number) {
        return Set.of();
    }
}
