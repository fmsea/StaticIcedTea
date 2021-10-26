package processing;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AnalysisFullSMTReport {
    private Set<String> variables;
    private Set<String> statements;
    private Map<String, String> fallThroughSmtExpressions;
    private Map<String, String> branchOutSmtExpressions;

    public AnalysisFullSMTReport(Set<String> statements,
                                 Set<String> variables,
                                 Map<String, String> fallThroughExprs,
                                 Map<String, String> branchOutExprs) {
        this.statements = statements;
        this.variables = variables;
        this.fallThroughSmtExpressions = fallThroughExprs;
        this.branchOutSmtExpressions = branchOutExprs;
    }

    public Set<String> variables() {
        return Collections.unmodifiableSet(this.variables);
    }

    public Set<String> statements() {
        return Collections.unmodifiableSet(this.statements);
    }

    public Optional<String> getFallThrough(String statement) {
        return Optional.ofNullable(this.fallThroughSmtExpressions.get(statement));
    }

    public Optional<String> getBranchOut(String statement) {
        return Optional.ofNullable(this.branchOutSmtExpressions.get(statement));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.variables.size() == 0) {
            return "";
        }
        this.variables.forEach(v -> {
                sb.append(v);
                sb.append("\t");
            });
        // delete last tab
        sb.deleteCharAt(sb.length() - 1);
        sb.append("\n");
        this.statements.stream().sorted().forEach(statement -> {
                sb.append(statement);
                sb.append("\n");
                Optional<String> fall = this.getFallThrough(statement);
                Optional<String> branch = this.getBranchOut(statement);
                fall.ifPresent(expr -> {
                        sb.append("fall\t");
                        sb.append(expr);
                        sb.append("\n");
                    });
                branch.ifPresent(expr -> {
                        sb.append("branch\t");
                        sb.append(expr);
                        sb.append("\n");
                    });
            });
        return sb.toString();
    }
}
