package dev.fmsea.processing.smt.visitors;

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

public class SmtConverter implements SmtExpression.Visitor<String> {
    public String visitFalse(FalseSmtExpression falsy) {
        return "false";
    }

    public String visitTrue(TrueSmtExpression truthy) {
        return "true";
    }

    public String visitAndExpr(AndSmtExpression expr) {
        if (expr.expressions.size() > 1) {
            return expr.expressions.stream()
                .map(xpr -> xpr.accept(this))
                .sorted()
                .collect(Collectors.joining(" ", "(and ", ")"));
        } else {
            return expr.expressions.stream()
                .map(xpr -> xpr.accept(this))
                .collect(Collectors.joining(""));
        }
    }

    public String visitOrExpr(OrSmtExpression expr) {
        if (expr.expressions.size() > 1) {
            return expr.expressions.stream()
                .map(xpr -> xpr.accept(this))
                .sorted()
                .collect(Collectors.joining(" ", "(or ", ")"));
        } else {
            return expr.expressions.stream()
                .map(xpr -> xpr.accept(this))
                .collect(Collectors.joining(""));
        }
    }

    public String visitAdditionExpr(AdditionSmtExpression expr) {
        return String.format("(+ %s %s)",
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public String visitSubtractionExpr(SubtractionSmtExpression expr) {
        return String.format("(- %s %s)",
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public String visitMultiplicationExpr(MultiplicationSmtExpression expr) {
        return String.format("(* %s %s)",
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public String visitDivisionExpr(DivisionSmtExpression expr) {
        return String.format("(div %s %s)",
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public String visitModulusExpr(ModulusSmtExpression expr) {
        return String.format("(mod %s %s)",
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public String visitEqExpr(EqSmtExpression expr) {
        return String.format("(= %s %s)",
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public String visitLeExpr(LeSmtExpression expr) {
        return String.format("(<= %s %s)",
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public String visitLtExpr(LtSmtExpression expr) {
        return String.format("(< %s %s)",
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public String visitGeExpr(GeSmtExpression expr) {
        return String.format("(>= %s %s)",
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public String visitGtExpr(GtSmtExpression expr) {
        return String.format("(> %s %s)",
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public String visitNegExpr(NegSmtExpression expr) {
        return String.format("(- %s)",
            expr.expr.accept(this));
    }

    public String visitNotExpr(NotSmtExpression expr) {
        return String.format("(not %s)",
            expr.expr.accept(this));
    }

    public String visitIdentifier(Identifier identifier) {
        return identifier.identifier.toString();
    }

    public String visitNumber(Number number) {
        if (number.value < 0) {
            return String.format("(- %d)", number.value * -1);
        } else {
            return String.format("%d", number.value);
        }
    }
}
