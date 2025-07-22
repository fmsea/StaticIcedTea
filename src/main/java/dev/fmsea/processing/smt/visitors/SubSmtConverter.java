package dev.fmsea.processing.smt.visitors;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import dev.fmsea.processing.smt.AdditionSmtExpression;
import dev.fmsea.processing.smt.AndSmtExpression;
import dev.fmsea.processing.smt.BinopSmtExpression;
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

public class SubSmtConverter implements SmtExpression.Visitor<Optional<String>> {

    private final Set<Local> variables;

    public SubSmtConverter(Set<Local> variables) {
        this.variables = variables;
    }

    public Optional<String> visitFalse(FalseSmtExpression falsy) {
        return Optional.empty();
    }

    public Optional<String> visitTrue(TrueSmtExpression truthy) {
        return Optional.empty();
    }

    public Optional<String> visitAndExpr(AndSmtExpression expr) {
        Set<String> exprs = expr.expressions.stream()
            .map(xpr -> xpr.accept(this))
            .filter(o -> o.isPresent())
            .map(o -> o.get())
            .collect(Collectors.toSet());
        if (exprs.size() > 1) {
            return exprs.stream()
                .sorted()
                .reduce((a, b) -> String.format("%s %s", a, b))
                .map(xpr -> String.format("(and %s)", xpr));
        } else {
            return exprs.stream().findFirst();
        }
    }

    public Optional<String> visitOrExpr(OrSmtExpression expr) {
        Set<String> exprs = expr.expressions.stream()
            .map(xpr -> xpr.accept(this))
            .filter(o -> o.isPresent())
            .map(o -> o.get())
            .collect(Collectors.toSet());
        if (exprs.size() > 1) {
            return exprs.stream()
                .sorted()
                .reduce((a, b) -> String.format("%s %s", a, b))
                .map(xpr -> String.format("(or %s)", xpr));
        } else {
            return exprs.stream().findFirst();
        }
    }

    private Optional<String> visitBinopExpression(BinopSmtExpression binop) {
        BiPredicate<Local, SmtExpression> contains = (v, expr) -> {
            Set<Local> locals = expr.getLocals();
            return locals.isEmpty() || locals.contains(v);
        };
        if (this.variables.stream().map(v -> contains.test(v, binop.left)).reduce((a, b) -> a || b).orElse(false) &&
            this.variables.stream().map(v -> contains.test(v, binop.right)).reduce((a, b) -> a || b).orElse(false)) {
            return Optional.of(binop.toSmt2());
        } else {
            return Optional.empty();
        }
    }

    public Optional<String> visitAdditionExpr(AdditionSmtExpression expr) {
        return this.visitBinopExpression(expr);
    }

    public Optional<String> visitSubtractionExpr(SubtractionSmtExpression expr) {
        return this.visitBinopExpression(expr);
    }

    public Optional<String> visitMultiplicationExpr(MultiplicationSmtExpression expr) {
        return this.visitBinopExpression(expr);
    }

    public Optional<String> visitDivisionExpr(DivisionSmtExpression expr) {
        return this.visitBinopExpression(expr);
    }

    public Optional<String> visitModulusExpr(ModulusSmtExpression expr) {
        return this.visitBinopExpression(expr);
    }

    public Optional<String> visitEqExpr(EqSmtExpression expr) {
        return this.visitBinopExpression(expr);
    }

    public Optional<String> visitLeExpr(LeSmtExpression expr) {
        return this.visitBinopExpression(expr);
    }

    public Optional<String> visitLtExpr(LtSmtExpression expr) {
        return this.visitBinopExpression(expr);
    }

    public Optional<String> visitGeExpr(GeSmtExpression expr) {
        return this.visitBinopExpression(expr);
    }

    public Optional<String> visitGtExpr(GtSmtExpression expr) {
        return this.visitBinopExpression(expr);
    }

    public Optional<String> visitNegExpr(NegSmtExpression expr) {
        return expr.expr.accept(this).map(xpr -> String.format("(- %s)", xpr));
    }

    public Optional<String> visitNotExpr(NotSmtExpression expr) {
        return expr.expr.accept(this).map(xpr -> String.format("(not %s)", xpr));
    }

    public Optional<String> visitIdentifier(Identifier identifier) {
        if (this.variables.contains(identifier.identifier)) {
            return Optional.of(identifier.toSmt2());
        } else {
            return Optional.empty();
        }
    }

    public Optional<String> visitNumber(Number number) {
        return Optional.empty();
    }
}
