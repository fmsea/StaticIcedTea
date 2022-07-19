package processing.smt;

import java.util.List;
import java.util.Scanner;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import processing.smt.SmtExpression;

public class SmtExpressionReader {

    private final SmtExpression expr;

    public SmtExpressionReader(String smtExpression) {
        CharStream input = CharStreams.fromString(smtExpression);
        SmtExpressionLexer lexer = new SmtExpressionLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SmtExpressionParser parser = new SmtExpressionParser(tokens);
        ParseTree tree = parser.expr();
        SmtExpressionInstantiator instantiator = new SmtExpressionInstantiator();
        this.expr = instantiator.visit(tree);
    }

    public SmtExpression getSmtExpression() {
        return this.expr;
    }

    public static SmtExpression parse(String expression) {
        return new SmtExpressionReader(expression).getSmtExpression();
    }
}
