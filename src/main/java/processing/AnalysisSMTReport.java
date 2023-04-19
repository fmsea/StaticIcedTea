package processing;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import soot.Local;

public class AnalysisSMTReport {
    private Set<Local> variables;
    private Set<String> statements;
    private Map<String, String> fallThroughSmtExpressions;
    private Map<String, Set<Local>> fallVariables;
    private Map<String, Set<Local>> fallChangedVariables;
    private Map<String, String> branchOutSmtExpressions;
    private Map<String, Set<Local>> branchVariables;
    private Map<String, Set<Local>> branchChangedVariables;

    public AnalysisSMTReport(Set<String> statements,
                             Set<Local> variables,
                             Map<String, String> fallThroughExprs,
                             Map<String, Set<Local>> fallVariables,
                             Map<String, String> branchOutExprs,
                             Map<String, Set<Local>> branchVariables) {
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
                             Set<Local> variables,
                             Map<String, String> fallThroughExprs,
                             Map<String, Set<Local>> fallVariables,
                             Map<String, Set<Local>> fallChangedVariables,
                             Map<String, String> branchOutExprs,
                             Map<String, Set<Local>> branchVariables,
                             Map<String, Set<Local>> branchChangedVariables) {
        this.statements = statements;
        this.variables = variables;
        this.fallThroughSmtExpressions = fallThroughExprs;
        this.fallVariables = fallVariables;
        this.fallChangedVariables = fallChangedVariables;
        this.branchOutSmtExpressions = branchOutExprs;
        this.branchVariables = branchVariables;
        this.branchChangedVariables = branchChangedVariables;
    }

    public Set<Local> variables() {
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

    public Optional<Set<Local>> getFallVariables(String statement) {
        return Optional.ofNullable(this.fallVariables.get(statement));
    }

    public Optional<Set<Local>> getFallChangedVariables(String statement) {
        return Optional.ofNullable(this.fallChangedVariables.get(statement));
    }

    public Optional<Set<Local>> getBranchVariables(String statement) {
        return Optional.ofNullable(this.branchVariables.get(statement));
    }

    public Optional<Set<Local>> getBranchChangedVariables(String statement) {
        return Optional.ofNullable(this.branchChangedVariables.get(statement));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.variables.size() == 0) {
            return "";
        }
        sb.append(this.variables.stream().map(v -> v.toString()).sorted().collect(Collectors.joining("\t")));
        sb.append("\n");
        this.statements.stream().sorted().forEach(statement -> {
                sb.append(statement);
                sb.append("\n");
                Optional<String> fall = this.getFallThrough(statement);
                Optional<String> branch = this.getBranchOut(statement);
                String fallChangedVariables = this.getFallChangedVariables(statement)
                    .map(vars -> vars.stream().map(v -> v.toString()).sorted().collect(Collectors.joining("\t", "", "\t")))
                    .orElse("");
                String branchChangedVariables = this.getBranchChangedVariables(statement)
                    .map(vars -> vars.stream().map(v -> v.toString()).sorted().collect(Collectors.joining("\t", "", "\t")))
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
