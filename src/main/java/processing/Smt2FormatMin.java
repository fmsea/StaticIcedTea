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
import soot.Value;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import processing.smt.SmtExpression;
import processing.smt.SmtExpressionReader;
import processing.util.FlowSet;
import solver.SolverWrapper;
import solver.SolverFactory;

public class Smt2FormatMin extends Smt2Format {

    private static Logger LOGGER = LoggerFactory.getLogger(Smt2Format.class);
    private static SolverWrapper solver = SolverFactory.getSolver();

    public static void Smt2FormatMin(Reader fullL,
                                     Reader changedL,
                                     Reader fullR,
                                     Reader changedR,
                                     Writer writer) throws IOException {
        AnalysisFullSMTReport leftReport = Smt2Reader.parseFullReport(fullL);
        AnalysisFullSMTReport rightReport = Smt2Reader.parseFullReport(fullR);
        Map<String, List<SmtIdentifierExpression>> changedLeft = Smt2Reader.parse(changedL);
        Map<String, List<SmtIdentifierExpression>> changedRight = Smt2Reader.parse(changedR);
        Set<String> statements = new HashSet<>();
        statements.addAll(changedLeft.keySet());
        statements.addAll(changedRight.keySet());
        List<String> sortedStatements = sortStatements(statements);
        for (String statement : sortedStatements) {
            writer.write("(echo \"");
            writer.write(statement.replaceAll("\"", "\"\""));
            writer.write("\")\n");
            Optional<SmtExpression> leftFall = leftReport.getFallThrough(statement)
                .map(SmtExpressionReader::parse);
            Optional<SmtExpression> rightFall = rightReport.getFallThrough(statement)
                .map(SmtExpressionReader::parse);
            Optional<SmtExpression> leftBranch = leftReport.getBranchOut(statement)
                .map(SmtExpressionReader::parse);
            Optional<SmtExpression> rightBranch = rightReport.getBranchOut(statement)
                .map(SmtExpressionReader::parse);
            Map<String, Set<Local>> localsMap = new HashMap<>();
            List<SmtIdentifierExpression> leftExprs = changedLeft.getOrDefault(statement, List.of());
            List<SmtIdentifierExpression> rightExprs = changedRight.getOrDefault(statement, List.of());
            Stream.concat(leftExprs.stream(), rightExprs.stream()).forEach(expr -> {
                    Set<Local> exprLocals = expr.identifiers.stream().map(i -> Locals.get(i)).collect(Collectors.toSet());
                    if (localsMap.containsKey(expr.identifier)) {
                        Set<Local> locals = localsMap.get(expr.identifier);
                        locals.addAll(exprLocals);
                    } else {
                        localsMap.put(expr.identifier, exprLocals);
                    }
                });
            Set<SmtIdentifier> exprs = new TreeSet<>();
            exprs.addAll(leftExprs.stream().map(SmtIdentifier::from).collect(Collectors.toList()));
            exprs.addAll(rightExprs.stream().map(SmtIdentifier::from).collect(Collectors.toList()));
            for (SmtIdentifier expr : exprs.stream().sorted().collect(Collectors.toList())) {
                writer.write(String.format("(echo \"%s\")\n", expr.toString()));
                if (expr.branchOut) {
                    Set<Local> locals = localsMap.getOrDefault(expr.identifier + "f", Set.of());
                    writer.write(formatSmtImplies(leftBranch, rightBranch, locals));
                } else {
                    Set<Local> locals = localsMap.getOrDefault(expr.identifier.toString(), Set.of());
                    writer.write(formatSmtImplies(leftFall, rightFall, locals));
                }
            }
        }
        writer.flush();
        writer.close();
    }

    private static String formatSmtImplies(Optional<SmtExpression> leftExpr,
                                           Optional<SmtExpression> rightExpr,
                                           Set<Local> locals) {
        StringBuilder sb = new StringBuilder();
        String left = leftExpr.flatMap(expr -> expr.toSmt2(locals)).orElse("true");
        String right = rightExpr.flatMap(expr -> expr.toSmt2(locals)).orElse("true");
        Set<String> variables = locals.stream().map(l -> l.toString()).collect(Collectors.toSet());
        sb.append(formatImplies(variables, left, right));
        sb.append(formatImplies(variables, right, left));
        return sb.toString();
    }

    private static String formatSmtImplies(Optional<SmtExpression> leftExpr,
                                           Optional<SmtExpression> rightExpr,
                                           Local identifier) {
        StringBuilder sb = new StringBuilder();
        Set<Local> locals = new HashSet<>();
        locals.add(identifier);
        leftExpr.map(expr -> expr.getLocals(identifier)).ifPresent(ls -> locals.addAll(ls));
        rightExpr.map(expr -> expr.getLocals(identifier)).ifPresent(ls -> locals.addAll(ls));
        String left = leftExpr.flatMap(expr -> expr.toSmt2(locals)).orElse("true");
        String right = rightExpr.flatMap(expr -> expr.toSmt2(locals)).orElse("true");
        Set<String> variables = locals.stream().map(l -> l.toString()).collect(Collectors.toSet());
        sb.append(formatImplies(variables, left, right));
        sb.append(formatImplies(variables, right, left));
        return sb.toString();
    }
}
