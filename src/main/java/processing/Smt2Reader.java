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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import processing.util.FlowSet;

public class Smt2Reader {

    private static final Logger LOGGER = LoggerFactory.getLogger(Smt2Reader.class);

    public static Set<String> getIdentifiers(String smt) {
        Set<String> identifiers = new HashSet<>();
        String[] tokens = smt.split("[ ()>=<+-]");
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].trim();
            if (token.isEmpty()) {
                continue;
            } else if (token.matches("^[$A-Za-z][A-Za-z0-9]+$") &&
                       !(token.equals("or") ||
                         token.equals("and") ||
                         token.equals("true") ||
                         token.equals("false"))) {
                identifiers.add(token);
            } else {
                LOGGER.debug("token was not identified: {}", token);
            }
        }
        return identifiers;
    }

    public static Optional<SmtIdentifierExpression> parseExpression(String smtWithIdentifier) {
        if (smtWithIdentifier.indexOf("->") < 0) {
            return Optional.empty();
        }
        String[] varFormula = smtWithIdentifier.split("->");
        assert varFormula.length == 2;
        String identifier = varFormula[0];
        String formula = varFormula[1];
        Set<String> identifiers = getIdentifiers(formula);
        return Optional.of(new SmtIdentifierExpression(identifier, identifiers, formula));
    }

    public static AnalysisFullSMTReport parseFullReport(Reader reader) {
        Set<String> statements = new HashSet<>();
        Set<String> variables = new HashSet<>();
        Map<String, String> fallThroughExprs = new HashMap<>();
        Map<String, String> branchOutExprs = new HashMap<>();
        BiConsumer<String, String> closeExpression = (statement, expression) -> {
            if (expression.startsWith("fall\t")) {
                // "fall\t" is 5 characters
                fallThroughExprs.put(statement, expression.substring(5).trim());
            } else if (expression.startsWith("branch\t")) {
                // "branch\t" is 7 characters
                branchOutExprs.put(statement, expression.substring(7).trim());
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
                if (line.matches("^[0-9]+.*")) {
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
                    expr.append(line);
                    expr.append("\n");
                } else if (line.isEmpty()) {
                } else {
                    expr.append(line);
                    expr.append("\n");
                }
            }
            closeExpression.accept(currentStatement, expr.toString());
        }
        return new AnalysisFullSMTReport(statements,
                                         variables,
                                         fallThroughExprs,
                                         branchOutExprs);
    }

    private static Set<String> parseVariables(String variablesLine) {
        Set<String> variables = new HashSet<>();
        Stream.of(variablesLine.trim().split("\t")).forEach(v -> variables.add(v));
        return variables;
    }

    public static Map<String, List<SmtIdentifierExpression>> parse(Reader reader) {
        try (Scanner scanner = new Scanner(reader)) {
            Map<String, List<SmtIdentifierExpression>> map = new HashMap<>();
            List<SmtIdentifierExpression> expressions = null;
            StringBuilder expr = new StringBuilder();
            while (scanner.hasNext()) {
                String line = scanner.nextLine().trim();
                LOGGER.debug(line);
                if (line.matches("^[0-9]+.*")) {
                    // close out current expression
                    if (expr.length() > 0) {
                        Optional<SmtIdentifierExpression> smtExpr = parseExpression(expr.toString());
                        if (smtExpr.isPresent()) {
                            expressions.add(smtExpr.get());
                        }
                        expr = new StringBuilder();
                    }
                    // reset for next series of statements
                    expressions = new ArrayList<>();
                    map.put(line, expressions);
                } else if (line.contains("->")) {
                    // clear any current expression
                    if (expr.length() > 0) {
                        Optional<SmtIdentifierExpression> smtExpr = parseExpression(expr.toString());
                        if (smtExpr.isPresent()) {
                            expressions.add(smtExpr.get());
                        }
                        expr = new StringBuilder();
                    }
                    expr.append(line);
                } else {
                    expr.append(" ");
                    expr.append(line);
                }
            }
            // close out last expression
            Optional<SmtIdentifierExpression> smtExpr = parseExpression(expr.toString());
            if (smtExpr.isPresent()) {
                expressions.add(smtExpr.get());
            }
            return map;
        }
    }

    public static Map<String, FlowSet<String>> parseExtraIdentifiers(Reader reader) {
        Map<String, FlowSet<String>> statements = new HashMap<>();
        try (Scanner scanner = new Scanner(reader)) {
            while (scanner.hasNext()) {
                String line = scanner.nextLine().trim();
                LOGGER.debug("line from extra identifiers file: {}", line);
                String[] elements = line.split("\t");
                FlowSet<String> identifiers = statements.getOrDefault(elements[0], new FlowSet<>());
                for (int i = 2; i < elements.length; i++) {
                    if ("fall".equals(elements[1])) {
                        identifiers.addFallThrough(elements[i]);
                    } else if ("branch".equals(elements[1])) {
                        identifiers.addBranchOut(elements[i]);
                    }
                }
                LOGGER.debug("statement and identifiers: {}->{}", elements[0], identifiers);
                statements.put(elements[0], identifiers);
            }
        }
        return statements;
    }

    public static Map<String, FlowSet<String>> getIdentifiersPerStatement(Reader reader) {
        Map<String, List<SmtIdentifierExpression>> smtExpressions = parse(reader);
        Map<String, FlowSet<String>> result = new HashMap<>();
        for (String statement : smtExpressions.keySet()) {
            FlowSet<String> identifiers = new FlowSet<>();
            result.put(statement, identifiers);
            for (SmtIdentifierExpression expr : smtExpressions.get(statement)) {
                if (expr.isBranchOut()) {
                    identifiers.addAllBranchOut(expr.identifiers);
                } else {
                    identifiers.addAllFallThrough(expr.identifiers);
                }
            }
        }
        return result;
    }
}
