package processing.smt;

import soot.Value;
import soot.Local;
import soot.jimple.Jimple;
import soot.IntType;
import soot.jimple.BinopExpr;
import soot.jimple.IntConstant;

import processing.Locals;

public class SmtExpressionInstantiator extends SmtExpressionBaseVisitor<SmtExpression> {

    @Override
    public SmtExpression visitAnd(SmtExpressionParser.AndContext ctx) {
        AndSmtExpression andExpr = new AndSmtExpression();
        for (SmtExpressionParser.ExprContext exprCtx : ctx.expr()) {
            andExpr.addExpression(visit(exprCtx));
        }
        return andExpr;
    }

    @Override
    public SmtExpression visitOr(SmtExpressionParser.OrContext ctx) {
        OrSmtExpression orExpr = new OrSmtExpression();
        for (SmtExpressionParser.ExprContext exprCtx : ctx.expr()) {
            orExpr.addExpression(visit(exprCtx));
        }
        return orExpr;
    }

    @Override
    public SmtExpression visitNot(SmtExpressionParser.NotContext ctx) {
        SmtExpression expr = visit(ctx.expr());
        return new NotSmtExpression(expr);
    }

    @Override
    public SmtExpression visitLessThan(SmtExpressionParser.LessThanContext ctx) {
        SmtExpression left = visit(ctx.expr(0));
        SmtExpression right = visit(ctx.expr(1));
        return new LtSmtExpression(left, right);
    }

    @Override
    public SmtExpression visitGreaterThan(SmtExpressionParser.GreaterThanContext ctx) {
        SmtExpression left = visit(ctx.expr(0));
        SmtExpression right = visit(ctx.expr(1));
        return new GtSmtExpression(left, right);
    }

    @Override
    public SmtExpression visitLessOrEqual(SmtExpressionParser.LessOrEqualContext ctx) {
        SmtExpression left = visit(ctx.expr(0));
        SmtExpression right = visit(ctx.expr(1));
        return new LeSmtExpression(left, right);
    }

    @Override
    public SmtExpression visitGreaterOrEqual(SmtExpressionParser.GreaterOrEqualContext ctx) {
        SmtExpression left = visit(ctx.expr(0));
        SmtExpression right = visit(ctx.expr(1));
        return new GeSmtExpression(left, right);
    }

    @Override
    public SmtExpression visitEqual(SmtExpressionParser.EqualContext ctx) {
        SmtExpression left = visit(ctx.expr(0));
        SmtExpression right = visit(ctx.expr(1));
        return new EqSmtExpression(left, right);
    }

    @Override
    public SmtExpression visitMultiplication(SmtExpressionParser.MultiplicationContext ctx) {
        SmtExpression left = visit(ctx.expr(0));
        SmtExpression right = visit(ctx.expr(1));
        return new MultiplicationSmtExpression(left, right);
    }

    @Override
    public SmtExpression visitAddition(SmtExpressionParser.AdditionContext ctx) {
        SmtExpression left = visit(ctx.expr(0));
        SmtExpression right = visit(ctx.expr(1));
        return new AdditionSmtExpression(left, right);
    }

    @Override
    public SmtExpression visitSubtraction(SmtExpressionParser.SubtractionContext ctx) {
        SmtExpression left = visit(ctx.expr(0));
        SmtExpression right = visit(ctx.expr(1));
        return new SubtractionSmtExpression(left, right);
    }

    @Override
    public SmtExpression visitDivision(SmtExpressionParser.DivisionContext ctx) {
        SmtExpression left = visit(ctx.expr(0));
        SmtExpression right = visit(ctx.expr(1));
        return new DivisionSmtExpression(left, right);
    }

    @Override
    public SmtExpression visitModulus(SmtExpressionParser.ModulusContext ctx) {
        SmtExpression left = visit(ctx.expr(0));
        SmtExpression right = visit(ctx.expr(1));
        return new ModulusSmtExpression(left, right);
    }

    @Override
    public SmtExpression visitNegation(SmtExpressionParser.NegationContext ctx) {
        SmtExpression expr = visit(ctx.expr());
        return new NegSmtExpression(expr);
    }

    @Override
    public SmtExpression visitTrue(SmtExpressionParser.TrueContext ctx) {
        return new TrueSmtExpression();
    }

    @Override
    public SmtExpression visitFalse(SmtExpressionParser.FalseContext ctx) {
        return new FalseSmtExpression();
    }

    @Override
    public SmtExpression visitIdentifier(SmtExpressionParser.IdentifierContext ctx) {
        Local identifier = Locals.get(ctx.getChild(0).getText());
        return new Identifier(identifier);
    }

    @Override
    public SmtExpression visitNumber(SmtExpressionParser.NumberContext ctx) {
        int value = Integer.parseInt(ctx.getChild(0).getText());
        return new Number(value);
    }
}
