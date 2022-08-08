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
        AnalysisSMTReport leftReport = Smt2Reader.parse(fullL);
        AnalysisSMTReport rightReport = Smt2Reader.parse(fullR);
        AnalysisSMTReport changedLeft = Smt2Reader.parse(changedL);
        AnalysisSMTReport changedRight = Smt2Reader.parse(changedR);
        Set<String> statements = new HashSet<>();
        statements.addAll(changedLeft.statements());
        statements.addAll(changedRight.statements());
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
            Set<Local> fallLocals = new HashSet<>();
            Set<Local> branchLocals = new HashSet<>();
            changedLeft.getFallVariables(statement).map(vars -> vars.stream().map(v -> Locals.get(v)).collect(Collectors.toSet()))
                .ifPresent(vars -> fallLocals.addAll(vars));
            changedRight.getFallVariables(statement).map(vars -> vars.stream().map(v -> Locals.get(v)).collect(Collectors.toSet()))
                .ifPresent(vars -> fallLocals.addAll(vars));
            changedLeft.getBranchVariables(statement) .map(vars -> vars.stream().map(v -> Locals.get(v)).collect(Collectors.toSet()))
                .ifPresent(vars -> branchLocals.addAll(vars));
            changedRight.getBranchVariables(statement).map(vars -> vars.stream().map(v -> Locals.get(v)).collect(Collectors.toSet()))
                .ifPresent(vars -> branchLocals.addAll(vars));
            if (leftFall.isPresent() || rightFall.isPresent()) {
                writer.write("(echo \"fall through\")\n");
                writer.write(formatSmtImplies(leftFall, rightFall, fallLocals));
            }

            if (leftBranch.isPresent() || rightBranch.isPresent()) {
                writer.write("(echo \"branch out\")\n");
                writer.write(formatSmtImplies(leftBranch, rightBranch, branchLocals));
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
