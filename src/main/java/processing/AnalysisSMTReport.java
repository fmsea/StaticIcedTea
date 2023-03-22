package processing;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AnalysisSMTReport {
    private Set<String> variables;
    private Set<String> statements;
    private Map<String, String> fallThroughSmtExpressions;
    private Map<String, Set<String>> fallVariables;
    private Map<String, Set<String>> fallChangedVariables;
    private Map<String, String> branchOutSmtExpressions;
    private Map<String, Set<String>> branchVariables;
    private Map<String, Set<String>> branchChangedVariables;

    public AnalysisSMTReport(Set<String> statements,
                             Set<String> variables,
                             Map<String, String> fallThroughExprs,
                             Map<String, Set<String>> fallVariables,
                             Map<String, String> branchOutExprs,
                             Map<String, Set<String>> branchVariables) {
        this(statements,
             variables,
             fallThroughExprs,
             fallVariables,
             Map.of(),
             branchOutExprs,
             branchVariables,
             Map.of());
    }

    public AnalysisSMTReport(Set<String> statements,
                             Set<String> variables,
                             Map<String, String> fallThroughExprs,
                             Map<String, Set<String>> fallVariables,
                             Map<String, Set<String>> fallChangedVariables,
                             Map<String, String> branchOutExprs,
                             Map<String, Set<String>> branchVariables,
                             Map<String, Set<String>> branchChangedVariables) {
        this.statements = statements;
        this.variables = variables;
        this.fallThroughSmtExpressions = fallThroughExprs;
        this.fallVariables = fallVariables;
        this.fallChangedVariables = fallChangedVariables;
        this.branchOutSmtExpressions = branchOutExprs;
        this.branchVariables = branchVariables;
        this.branchChangedVariables = branchChangedVariables;
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

    public Optional<Set<String>> getFallVariables(String statement) {
        return Optional.ofNullable(this.fallVariables.get(statement));
    }

    public Optional<Set<String>> getFallChangedVariables(String statement) {
        return Optional.ofNullable(this.fallChangedVariables.get(statement));
    }

    public Optional<Set<String>> getBranchVariables(String statement) {
        return Optional.ofNullable(this.branchVariables.get(statement));
    }

    public Optional<Set<String>> getBranchChangedVariables(String statement) {
        return Optional.ofNullable(this.branchChangedVariables.get(statement));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.variables.size() == 0) {
            return "";
        }
        sb.append(this.variables.stream().collect(Collectors.joining("\t")));
        sb.append("\n");
        this.statements.stream().sorted().forEach(statement -> {
                sb.append(statement);
                sb.append("\n");
                Optional<String> fall = this.getFallThrough(statement);
                Optional<String> branch = this.getBranchOut(statement);
                String fallChangedVariables = this.getFallChangedVariables(statement)
                    .map(vars -> vars.stream().collect(Collectors.joining("\t", "", "\t")))
                    .orElse("");
                String branchChangedVariables = this.getBranchChangedVariables(statement)
                    .map(vars -> vars.stream().collect(Collectors.joining("\t", "", "\t")))
                    .orElse("");
                fall.ifPresent(expr -> {
                        sb.append("fall\t");
                        sb.append(fallChangedVariables);
                        sb.append(expr);
                        sb.append("\n");
                    });
                branch.ifPresent(expr -> {
                        sb.append("branch\t");
                        sb.append(branchChangedVariables);
                        sb.append(expr);
                        sb.append("\n");
                    });
            });
        return sb.toString();
    }
}
