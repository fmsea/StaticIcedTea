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
                       !(token.equals("or") || token.equals("and"))) {
                identifiers.add(token);
            } else {
                LOGGER.debug("token was not identified: {}", token);
            }
        }
        return identifiers;
    }

    public static Optional<SmtExpression> parseExpression(String smtWithIdentifier) {
        if (smtWithIdentifier.indexOf("->") < 0) {
            return Optional.empty();
        }
        String[] varFormula = smtWithIdentifier.split("->");
        assert varFormula.length == 2;
        String identifier = varFormula[0];
        String formula = varFormula[1];
        Set<String> identifiers = getIdentifiers(formula);
        return Optional.of(new SmtExpression(identifier, identifiers, formula));
    }

    public static Map<String, List<SmtExpression>> parse(Reader reader) {
        try (Scanner scanner = new Scanner(reader)) {
            Map<String, List<SmtExpression>> map = new HashMap<>();
            List<SmtExpression> expressions = null;
            StringBuilder expr = new StringBuilder();
            while (scanner.hasNext()) {
                String line = scanner.nextLine().trim();
                LOGGER.debug(line);
                if (line.matches("^[0-9]+.*")) {
                    // close out current expression
                    if (expr.length() > 0) {
                        Optional<SmtExpression> smtExpr = parseExpression(expr.toString());
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
                        Optional<SmtExpression> smtExpr = parseExpression(expr.toString());
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
            Optional<SmtExpression> smtExpr = parseExpression(expr.toString());
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
        Map<String, List<SmtExpression>> smtExpressions = parse(reader);
        Map<String, FlowSet<String>> result = new HashMap<>();
        for (String statement : smtExpressions.keySet()) {
            FlowSet<String> identifiers = new FlowSet<>();
            result.put(statement, identifiers);
            for (SmtExpression expr : smtExpressions.get(statement)) {
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
