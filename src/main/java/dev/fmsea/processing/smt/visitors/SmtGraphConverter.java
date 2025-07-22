package dev.fmsea.processing.smt.visitors;

import java.util.Set;

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
import dev.fmsea.processing.smt.SmtGraph;
import dev.fmsea.processing.smt.SubtractionSmtExpression;
import dev.fmsea.processing.smt.TrueSmtExpression;

import soot.Local;

public class SmtGraphConverter implements SmtExpression.Visitor<SmtGraph> {
    public SmtGraph visitFalse(FalseSmtExpression falsy) {
        return SmtGraph.empty();
    }

    public SmtGraph visitTrue(TrueSmtExpression truthy) {
        return SmtGraph.empty();
    }

    public SmtGraph visitAndExpr(AndSmtExpression expr) {
        return expr.expressions.stream()
            .map(xpr -> xpr.accept(this))
            .reduce(SmtGraph::union).orElse(SmtGraph.empty())
            .computeClosure();
    }

    public SmtGraph visitOrExpr(OrSmtExpression expr) {
        return expr.expressions.stream()
            .map(xpr -> xpr.accept(this))
            .reduce(SmtGraph::union).orElse(SmtGraph.empty())
            .computeClosure();
    }

    public SmtGraph visitAdditionExpr(AdditionSmtExpression expr) {
        return SmtGraph.union(
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public SmtGraph visitSubtractionExpr(SubtractionSmtExpression expr) {
        return SmtGraph.union(
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public SmtGraph visitMultiplicationExpr(MultiplicationSmtExpression expr) {
        return SmtGraph.union(
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public SmtGraph visitDivisionExpr(DivisionSmtExpression expr) {
        return SmtGraph.union(
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public SmtGraph visitModulusExpr(ModulusSmtExpression expr) {
        return SmtGraph.union(
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public SmtGraph visitEqExpr(EqSmtExpression expr) {
        SmtGraph graph = SmtGraph.empty();
        Set<Local> leftLocals = expr.left.getLocals();
        Set<Local> rightLocals = expr.right.getLocals();
        leftLocals.forEach(l -> {
            graph.addEdge(l, l, expr);
            rightLocals.forEach(r -> graph.addEdge(l, r, expr));
        });
        rightLocals.forEach(r -> {
            graph.addEdge(r, r, expr);
            leftLocals.forEach(l -> graph.addEdge(r, l, expr));
        });
        return graph;
    }

    public SmtGraph visitLeExpr(LeSmtExpression expr) {
        SmtGraph graph = SmtGraph.empty();
        Set<Local> leftLocals = expr.left.getLocals();
        Set<Local> rightLocals = expr.right.getLocals();
        leftLocals.forEach(l -> {
                graph.addEdge(l, l, expr);
                rightLocals.forEach(r -> graph.addEdge(l, r, expr));
            });
        rightLocals.forEach(r -> graph.addEdge(r, r, expr));
        return graph;
    }

    public SmtGraph visitLtExpr(LtSmtExpression expr) {
        SmtGraph graph = SmtGraph.empty();
        Set<Local> leftLocals = expr.left.getLocals();
        Set<Local> rightLocals = expr.right.getLocals();
        leftLocals.forEach(l -> {
                graph.addEdge(l, l, expr);
                rightLocals.forEach(r -> graph.addEdge(l, r, expr));
            });
        rightLocals.forEach(r -> graph.addEdge(r, r, expr));
        return graph;
    }

    public SmtGraph visitGeExpr(GeSmtExpression expr) {
        SmtGraph graph = SmtGraph.empty();
        Set<Local> leftLocals = expr.left.getLocals();
        Set<Local> rightLocals = expr.right.getLocals();
        leftLocals.forEach(l -> graph.addEdge(l, l, expr));
        rightLocals.forEach(r -> {
                graph.addEdge(r, r, expr);
                leftLocals.forEach(l -> graph.addEdge(r, l, expr));
            });
        return graph;
    }

    public SmtGraph visitGtExpr(GtSmtExpression expr) {
        SmtGraph graph = SmtGraph.empty();
        Set<Local> leftLocals = expr.left.getLocals();
        Set<Local> rightLocals = expr.right.getLocals();
        leftLocals.forEach(l -> graph.addEdge(l, l, expr));
        rightLocals.forEach(r -> {
                graph.addEdge(r, r, expr);
                leftLocals.forEach(l -> graph.addEdge(r, l, expr));
            });
        return graph;
    }

    public SmtGraph visitNegExpr(NegSmtExpression expr) {
        return expr.expr.accept(this);
    }

    public SmtGraph visitNotExpr(NotSmtExpression expr) {
        return expr.expr.accept(this);
    }

    public SmtGraph visitIdentifier(Identifier identifier) {
        SmtGraph graph = SmtGraph.empty();
        graph.addEdge(identifier.identifier, identifier.identifier, identifier);
        return graph;
    }

    public SmtGraph visitNumber(Number number) {
        return SmtGraph.empty();
    }
}
