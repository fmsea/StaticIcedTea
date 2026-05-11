package dev.fmsea.processing.smt.visitors;

import java.util.Optional;
import java.util.Set;
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

public class ProjectionSmtConverter implements SmtExpression.Visitor<Optional<String>> {

    private final Set<Local> variables;

    public ProjectionSmtConverter(Set<Local> variables) {
        this.variables = variables;
    }

    public Optional<String> visitFalse(FalseSmtExpression falsy) {
        return Optional.of("false");
    }

    public Optional<String> visitTrue(TrueSmtExpression truthy) {
        return Optional.of("true");
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
        Set<Local> locals = binop.getLocals();
        if (locals.size() == 1) {
            // interval valued expression, permit
            return Optional.of(binop.toSmt2());
        } else if (locals.size() == 2 && this.variables.containsAll(locals)) {
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
        return Optional.of(identifier.toSmt2());
    }

    public Optional<String> visitNumber(Number number) {
        return Optional.empty();
    }
}
