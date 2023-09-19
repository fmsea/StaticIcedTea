package tadr;

import java.util.List;
import java.util.Scanner;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import soot.Local;

public class TADRReader {

    private final TADR expr;

    public TADRReader(String smtExpression) {
        CharStream input = CharStreams.fromString(smtExpression);
        TADRLexer lexer = new TADRLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TADRParser parser = new TADRParser(tokens);
        ParseTree tree = parser.expr();
        TADRInstantiator instantiator = new TADRInstantiator();
        this.expr = instantiator.visit(tree);
    }

    public TADR getTADR() {
        return this.expr;
    }

    public static TADR parse(String expression) {
        return new TADRReader(expression).getTADR();
    }
}
