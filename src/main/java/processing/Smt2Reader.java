package processing;

import java.io.Reader;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.Local;

import processing.util.FlowSet;

public class Smt2Reader {

    private static final Logger LOGGER = LoggerFactory.getLogger(Smt2Reader.class);
    private static final Pattern IDENTIFIER = Pattern.compile("^[$A-Za-z][A-Za-z0-9]+$");
    private static final Pattern STATEMENT_LINE = Pattern.compile("^[0-9]+.*");
    private static final Pattern SMT_TOKENS = Pattern.compile("[ ()>=<+-]");
    private static final Pattern TAB = Pattern.compile("\t");

    public static Set<Local> getIdentifiers(String smt) {
        Set<Local> identifiers = new HashSet<>();
        String[] tokens = SMT_TOKENS.split(smt);
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].trim();
            if (token.isEmpty()) {
                continue;
            } else if (IDENTIFIER.matcher(token).matches() &&
                       !(token.equals("or") ||
                         token.equals("and") ||
                         token.equals("true") ||
                         token.equals("false"))) {
                identifiers.add(Locals.get(token));
            } else {
                LOGGER.trace("token was not identified: {}", token);
            }
        }
        return identifiers;
    }

    public static AnalysisSMTReport parse(Reader reader) {
        Set<String> statements = new HashSet<>();
        Set<Local> variables = new HashSet<>();
        Map<String, String> fallThroughExprs = new HashMap<>();
        Map<String, String> branchOutExprs = new HashMap<>();
        Map<String, Set<Local>> fallVariables = new HashMap<>();
        Map<String, Set<Local>> fallChangedVariables = new HashMap<>();
        Map<String, Set<Local>> branchVariables = new HashMap<>();
        Map<String, Set<Local>> branchChangedVariables = new HashMap<>();
        BiConsumer<String, String> closeExpression = (statement, expression) -> {
            if (expression.startsWith("fall\t")) {
                // "fall\t" is 5 characters
                String smtExpression = expression.substring(5).trim();
                Set<Local> vars = getIdentifiers(smtExpression);
                fallThroughExprs.put(statement, smtExpression);
                fallVariables.put(statement, vars);
            } else if (expression.startsWith("branch\t")) {
                // "branch\t" is 7 characters
                String smtExpression = expression.substring(7).trim();
                Set<Local> vars = getIdentifiers(smtExpression);
                branchOutExprs.put(statement, smtExpression);
                branchVariables.put(statement, vars);
            }
        };
        try (Scanner scanner = new Scanner(reader)) {
            if (scanner.hasNext()) {
                variables = parseVariables(scanner.nextLine().trim());
            }
            StringBuilder expr = new StringBuilder();
            String currentStatement = "";
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                LOGGER.debug("full parsing: {}", line);
                if (STATEMENT_LINE.matcher(line).matches()) {
                    // close out current expression
                    if (expr.length() > 0) {
                        closeExpression.accept(currentStatement, expr.toString());
                        expr = new StringBuilder();
                    }
                    currentStatement = line.trim();
                    // seed initial mapping
                    statements.add(currentStatement);
                    fallThroughExprs.put(currentStatement, null);
                    branchOutExprs.put(currentStatement, null);
                } else if (line.startsWith("branch\t") ||
                           line.startsWith("fall\t")) {
                    if (expr.length() > 0) {
                        closeExpression.accept(currentStatement, expr.toString());
                        expr = new StringBuilder();
                    }
                    boolean isFall = line.startsWith("fall");
                    int first = line.indexOf('\t');
                    int openExpr = Stream.of(line.indexOf('('),
                                             line.lastIndexOf('f'),
                                             line.lastIndexOf('t'))
                        .mapToInt(i -> (int)i).max().orElse(-1);
                    int last = line.lastIndexOf('\t');
                    if (last > openExpr) {
                        // there's a tab in the SMT expression, ignore.
                        last = first;
                    }
                    Set<Local> changedVariables = null;
                    if (first != last) {
                        changedVariables = parseVariables(line.substring(first, last));
                    }
                    if (isFall) {
                        fallChangedVariables.put(currentStatement, changedVariables);
                    } else {
                        branchChangedVariables.put(currentStatement, changedVariables);
                    }
                    expr.append(line.substring(0, first + 1));
                    expr.append(line.substring(last + 1));
                    expr.append("\n");
                } else if (line.isEmpty()) {
                } else {
                    expr.append(line);
                    expr.append("\n");
                }
            }
            closeExpression.accept(currentStatement, expr.toString());
        }
        return new AnalysisSMTReport(statements,
                                     variables,
                                     fallThroughExprs,
                                     fallVariables,
                                     fallChangedVariables,
                                     branchOutExprs,
                                     branchVariables,
                                     branchChangedVariables);
    }

    protected static Set<Local> parseVariables(String variablesLine) {
        return Stream.of(TAB.split(variablesLine.trim()))
            .map(v -> Locals.get(v))
            .collect(Collectors.toSet());
    }

    public static Map<String, FlowSet<Local>> parseExtraIdentifiers(Reader reader) {
        Map<String, FlowSet<Local>> statements = new HashMap<>();
        try (Scanner scanner = new Scanner(reader)) {
            while (scanner.hasNext()) {
                String line = scanner.nextLine().trim();
                LOGGER.debug("line from extra identifiers file: {}", line);
                String[] elements = TAB.split(line);
                FlowSet<Local> identifiers = statements.getOrDefault(elements[0], new FlowSet<>());
                for (int i = 2; i < elements.length; i++) {
                    if ("fall".equals(elements[1])) {
                        identifiers.addFallThrough(Locals.get(elements[i]));
                    } else if ("branch".equals(elements[1])) {
                        identifiers.addBranchOut(Locals.get(elements[i]));
                    }
                }
                LOGGER.debug("statement and identifiers: {}->{}", elements[0], identifiers);
                statements.put(elements[0], identifiers);
            }
        }
        return statements;
    }

    public static Map<String, FlowSet<Local>> getIdentifiersPerStatement(Reader reader) {
        AnalysisSMTReport report = parse(reader);
        Map<String, FlowSet<Local>> result = new HashMap<>();
        for (String statement : report.statements()) {
            FlowSet<Local> identifiers = new FlowSet<>();
            result.put(statement, identifiers);
            report.getFallVariables(statement).ifPresent(vars -> identifiers.addAllFallThrough(vars));
            report.getBranchVariables(statement).ifPresent(vars -> identifiers.addAllBranchOut(vars));
        }
        return result;
    }
}
