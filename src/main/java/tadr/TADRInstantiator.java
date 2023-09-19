package tadr;

import abstractinterp.scalar.state.Interval32Box;
import common.Locals;
import soot.Local;

public class TADRInstantiator extends TADRBaseVisitor<TADR> {

    @Override
    public TADR visitNot(TADRParser.NotContext ctx) {
        TADR expr = visit(ctx.expr());
        return TADR.newNotExpr(expr);
    }

    @Override
    public TADR visitLessThan(TADRParser.LessThanContext ctx) {
        TADR left = visit(ctx.expr(0));
        TADR right = visit(ctx.expr(1));
        return TADR.newLtExpr(left, right);
    }

    @Override
    public TADR visitGreaterThan(TADRParser.GreaterThanContext ctx) {
        TADR left = visit(ctx.expr(0));
        TADR right = visit(ctx.expr(1));
        return TADR.newGtExpr(left, right);
    }

    @Override
    public TADR visitLessOrEqual(TADRParser.LessOrEqualContext ctx) {
        TADR left = visit(ctx.expr(0));
        TADR right = visit(ctx.expr(1));
        return TADR.newLeExpr(left, right);
    }

    @Override
    public TADR visitGreaterOrEqual(TADRParser.GreaterOrEqualContext ctx) {
        TADR left = visit(ctx.expr(0));
        TADR right = visit(ctx.expr(1));
        return TADR.newGeExpr(left, right);
    }

    @Override
    public TADR visitEqual(TADRParser.EqualContext ctx) {
        TADR left = visit(ctx.expr(0));
        TADR right = visit(ctx.expr(1));
        return TADR.newEqExpr(left, right);
    }

    @Override
    public TADR visitMultiplication(TADRParser.MultiplicationContext ctx) {
        TADR left = visit(ctx.expr(0));
        TADR right = visit(ctx.expr(1));
        return TADR.newMulExpr(left, right);
    }

    @Override
    public TADR visitAddition(TADRParser.AdditionContext ctx) {
        TADR left = visit(ctx.expr(0));
        TADR right = visit(ctx.expr(1));
        return TADR.newAddExpr(left, right);
    }

    @Override
    public TADR visitSubtraction(TADRParser.SubtractionContext ctx) {
        TADR left = visit(ctx.expr(0));
        TADR right = visit(ctx.expr(1));
        return TADR.newSubExpr(left, right);
    }

    @Override
    public TADR visitDivision(TADRParser.DivisionContext ctx) {
        TADR left = visit(ctx.expr(0));
        TADR right = visit(ctx.expr(1));
        return TADR.newDivExpr(left, right);
    }

    @Override
    public TADR visitNegation(TADRParser.NegationContext ctx) {
        TADR expr = visit(ctx.expr());
        return TADR.newNegExpr(expr);
    }

    @Override
    public TADR visitIdentifier(TADRParser.IdentifierContext ctx) {
        Local identifier = Locals.get(ctx.getChild(0).getText());
        return TADR.newVariable(identifier);
    }

    @Override
    public TADR visitValue(TADRParser.ValueContext ctx) {
        int value = Integer.parseInt(ctx.getChild(0).getText());
        return TADR.newValue(Interval32Box.of(value));
    }
}
