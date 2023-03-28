package processing;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import soot.Local;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import processing.smt.SmtExpression;
import processing.smt.SmtExpressionReader;
import processing.util.FlowSet;
import solver.SolverWrapper;
import solver.SolverFactory;

public class Smt2FormatReachable extends Smt2Format {
    private static Logger LOGGER = LoggerFactory.getLogger(Smt2FormatReachable.class);
    private static SolverWrapper solver = SolverFactory.getSolver();

    public static void Smt2FormatReachable(Reader left,
                                           Reader right,
                                           Writer writer) throws IOException{
        Smt2FormatReachable(left, right, writer, Smt2FormatType.MIN);
    }

    public static void Smt2FormatReachable(Reader left,
                                           Reader right,
                                           Writer writer,
                                           Smt2FormatType type) throws IOException {
        AnalysisSMTReport leftReport = Smt2Reader.parse(left);
        AnalysisSMTReport rightReport = Smt2Reader.parse(right);
        LOGGER.debug("parsed left and right reports");
        Set<String> statements = Stream.concat(leftReport.statements().stream(),
                                               rightReport.statements().stream())
            .collect(Collectors.toSet());
        List<String> sortedStatements = sortStatements(statements);
        writer.write("(set-logic LIA)\n");
        for (String statement : sortedStatements) {
            writer.write("(echo \"");
            writer.write(statement.replaceAll("\"", "\"\""));
            writer.write("\")\n");
            LOGGER.debug("formatting \"{}\"", statement);
            Optional<SmtExpression> leftFall = leftReport.getFallThrough(statement)
                .map(SmtExpressionReader::parse);
            Optional<SmtExpression> rightFall = rightReport.getFallThrough(statement)
                .map(SmtExpressionReader::parse);
            Optional<SmtExpression> leftBranch = leftReport.getBranchOut(statement)
                .map(SmtExpressionReader::parse);
            Optional<SmtExpression> rightBranch = rightReport.getBranchOut(statement)
                .map(SmtExpressionReader::parse);
            LOGGER.trace("[leftFall = {}, rightFall = {}, leftBranch = {}, rightBranch={}]",
                         leftFall,
                         rightFall,
                         leftBranch,
                         rightBranch);
            Set<Local> changedVariablesFall;
            Set<Local> changedVariablesBranch;

            switch (type) {
            case MIN:
                changedVariablesFall = Stream.concat(leftReport.getFallChangedVariables(statement).orElse(Set.of()).stream(),
                                                     rightReport.getFallChangedVariables(statement).orElse(Set.of()).stream())
                    .map(vars -> Locals.get(vars))
                    .collect(Collectors.toSet());
                changedVariablesBranch = Stream.concat(leftReport.getBranchChangedVariables(statement).orElse(Set.of()).stream(),
                                                       rightReport.getBranchChangedVariables(statement).orElse(Set.of()).stream())
                    .map(vars -> Locals.get(vars))
                    .collect(Collectors.toSet());
                break;
            case FULL:
            default:
                changedVariablesFall = Set.of();
                changedVariablesBranch = Set.of();
            }

            if (leftFall.isPresent() || rightFall.isPresent()) {
                writer.write("(echo \"fall through\")\n");
                if (changedVariablesFall.size() > 0) {
                    writer.write(formatSmtImplies(leftFall.orElse(SmtExpression.FALSE()),
                                                  rightFall.orElse(SmtExpression.FALSE()),
                                                  changedVariablesFall));
                } else {
                    writer.write(formatSmtImpliesFull(leftFall.orElse(SmtExpression.FALSE()),
                                                      rightFall.orElse(SmtExpression.FALSE())));
                }
            }

            if (leftBranch.isPresent() || rightBranch.isPresent()) {
                writer.write("(echo \"branch out\")\n");
                if (changedVariablesBranch.size() > 0) {
                    writer.write(formatSmtImplies(leftBranch.orElse(SmtExpression.FALSE()),
                                                  rightBranch.orElse(SmtExpression.FALSE()),
                                                  changedVariablesFall));
                } else {
                    writer.write(formatSmtImpliesFull(leftBranch.orElse(SmtExpression.FALSE()),
                                                      rightBranch.orElse(SmtExpression.FALSE())));
                }
            }
        }
        writer.flush();
        writer.close();
    }

    private static String formatSmtImplies(SmtExpression left,
                                           SmtExpression right,
                                           Set<Local> changedVariables) {
        StringBuilder sb = new StringBuilder();
        Set<Local> variables = SmtExpression.reachableUnion(changedVariables, left, right);
        String leftSmt = left.toReachableSmt2(variables).orElse("true");
        String rightSmt = right.toReachableSmt2(variables).orElse("true");
        Set<String> variableStrs = variables.stream().map(l -> l.toString()).collect(Collectors.toSet());
        sb.append(formatImplies(variableStrs, leftSmt, rightSmt));
        sb.append(formatImplies(variableStrs, rightSmt, leftSmt));
        return sb.toString();
    }

    private static String formatSmtImpliesFull(SmtExpression left, SmtExpression right) {
        StringBuilder sb = new StringBuilder();
        Set<String> variables = Stream.concat(left.getLocals().stream(),
                                              right.getLocals().stream())
            .map(local -> local.toString())
            .collect(Collectors.toSet());
        String leftSmt = left.toSmt2();
        String rightSmt = right.toSmt2();
        sb.append(formatImplies(variables, leftSmt, rightSmt));
        sb.append(formatImplies(variables, rightSmt, leftSmt));
        return sb.toString();
    }
}
