package dev.fmsea.inference;

public class InvariantInstantiator extends InvariantExpressionBaseVisitor<InvariantExpression> {

    public InvariantExpression visitRelationalConstraint(InvariantExpressionParser.RelationalConstraintContext ctx) {
        InvariantExpression left = visit(ctx.expr(0));
        InvariantExpression right = visit(ctx.expr(1));
        switch (ctx.RELOP().getText()) {
            case "==":
                return InvariantExpression.newEqInv(left, right);
            case ">=":
                return InvariantExpression.newGeqInv(left, right);
            case "<=":
                return InvariantExpression.newLeqInv(left, right);
            case ">":
                return InvariantExpression.newGtInv(left, right);
            case "<":
                return InvariantExpression.newLtInv(left, right);
            default:
                throw new RuntimeException("Nope nopity nope nope.");
        }
    }

    public InvariantExpression visitIdentifier(InvariantExpressionParser.IdentifierContext ctx) {
        String identifierText = ctx.Identifier().getText();
        if (identifierText.startsWith("-")) {
            return InvariantExpression.newNegIdentifier(
                InvariantExpression.newIdentifier(identifierText.substring(1)));
        } else {
            return InvariantExpression.newIdentifier(ctx.Identifier().getText());
        }
    }

    public InvariantExpression visitNumeral(InvariantExpressionParser.NumeralContext ctx) {
        return InvariantExpression.newNumeral(Integer.valueOf(ctx.Numeral().getText()));
    }

    public InvariantExpression visitBinarySum(InvariantExpressionParser.BinarySumContext ctx) {
        InvariantExpression left = visit(ctx.expr(0));
        InvariantExpression right = visit(ctx.expr(1));
        switch (ctx.SUMOP().getText()) {
            case "+":
                return InvariantExpression.newSum(left, right);
            case "-":
                return InvariantExpression.newDiff(left, right);
            default:
                throw new RuntimeException("Invalid sum operator");
        }
    }
}
