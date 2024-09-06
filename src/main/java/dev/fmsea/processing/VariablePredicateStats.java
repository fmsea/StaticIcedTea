package dev.fmsea.processing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.json.JSONObject;

import dev.fmsea.processing.smt.SmtExpression;
import dev.fmsea.processing.smt.SmtExpressionReader;

public class VariablePredicateStats {
    private enum Keys {
        VARIABLES,
        PREDICATES,
    }

    private Map<String, Map<Keys, Integer>> fallThroughCounts;
    private Map<String, Map<Keys, Integer>> branchOutCounts;

    public VariablePredicateStats() {
        this.fallThroughCounts = new HashMap<>();
        this.branchOutCounts = new HashMap<>();
    }

    public String toJSON() {
        Set<String> statements = new HashSet<>();
        Map<String, Map<String, Map<Keys, Integer>>> stats = new HashMap<>();
        statements.addAll(this.fallThroughCounts.keySet());
        statements.addAll(this.branchOutCounts.keySet());
        for (String statement : statements) {
            Map<Keys, Integer> fall = this.fallThroughCounts.get(statement);
            Map<Keys, Integer> branch = this.branchOutCounts.get(statement);
            Map<String, Map<Keys, Integer>> map = new HashMap<>();
            if (fall != null) {
                map.put("fall", fall);
            }
            if (branch != null) {
                map.put("branch", branch);
            }
            stats.put(statement, map);
        }
        JSONObject o = new JSONObject(stats);
        return o.toString();
    }

    public static VariablePredicateStats from(AnalysisSMTReport report) {
        VariablePredicateStats stats = new VariablePredicateStats();
        for (String statement : report.statements()) {
            Optional<SmtExpression> fallSmtExpression = report.getFallThrough(statement)
                .map(SmtExpressionReader::parse);
            Optional<SmtExpression> branchSmtExpression = report.getBranchOut(statement)
                .map(SmtExpressionReader::parse);
            fallSmtExpression.ifPresent(expr -> {
                    stats.fallThroughCounts.put(statement, Map.of(Keys.VARIABLES, expr.getLocals().size(),
                                                                  Keys.PREDICATES, expr.getPredicateCount()));
                });
            branchSmtExpression.ifPresent(expr -> {
                    stats.branchOutCounts.put(statement, Map.of(Keys.VARIABLES, expr.getLocals().size(),
                                                                Keys.PREDICATES, expr.getPredicateCount()));
                });
        }
        return stats;
    }
}
