package dev.fmsea.inference;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class ZonotopalInferenceParser {

    public static InvariantExpression parseExpr(String expr) {
        CharStream input = CharStreams.fromString(expr);
        InvariantExpressionLexer lexer = new InvariantExpressionLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        InvariantExpressionParser parser = new InvariantExpressionParser(tokens);
        ParseTree tree = parser.invariant();
        InvariantInstantiator instantiator = new InvariantInstantiator();
        return instantiator.visit(tree);
    }
}
