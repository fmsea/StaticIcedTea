package dev.fmsea.processing;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import dev.fmsea.processing.smt.SmtExpression;
import dev.fmsea.processing.smt.SmtExpressionReader;
import soot.Local;

public class SmtReport {
    private Set<Local> variables;
    private Set<String> statements;
    private Map<String, SmtExpression> fallThroughSmtExpressions;
    private Map<String, Set<Local>> fallVariables;
    private Map<String, Set<Local>> fallChangedVariables;
    private Map<String, SmtExpression> branchOutSmtExpressions;
    private Map<String, Set<Local>> branchVariables;
    private Map<String, Set<Local>> branchChangedVariables;

    public static class SmtReportBuilder {
        private Set<Local> variables;
        private Set<String> statements;
        private Map<String, SmtExpression> fallThroughSmtExpressions;
        private Map<String, Set<Local>> fallVariables;
        private Map<String, Set<Local>> fallChangedVariables;
        private Map<String, SmtExpression> branchOutSmtExpressions;
        private Map<String, Set<Local>> branchVariables;
        private Map<String, Set<Local>> branchChangedVariables;

        public SmtReportBuilder withVariables(Set<Local> variables) {
            this.variables = variables;
            return this;
        }

        public SmtReportBuilder withStatements(Set<String> statements) {
            this.statements = statements;
            return this;
        }

        public SmtReportBuilder withStringyFallExpressions(Map<String, String> stringyExpressions) {
            Map<String, SmtExpression> expressions = new HashMap<>();
            for (Map.Entry<String, String> kv : stringyExpressions.entrySet()) {
                String statement = kv.getKey();
                SmtExpression expression = null;
                if (kv.getValue() != null) {
                    expression = SmtExpressionReader.parse(kv.getValue());
                }
                expressions.put(statement, expression);
            }
            return this.withFallExpressions(expressions);
        }

        public SmtReportBuilder withFallExpressions(Map<String, SmtExpression> expressions) {
            this.fallThroughSmtExpressions = expressions;
            return this;
        }

        public SmtReportBuilder withFallVariables(Map<String, Set<Local>> variables) {
            this.fallVariables = variables;
            return this;
        }

        public SmtReportBuilder withFallChangedVariables(Map<String, Set<Local>> variables) {
            this.fallChangedVariables = variables;
            return this;
        }

        public SmtReportBuilder withStringyBranchExpressions(Map<String, String> stringyExpressions) {
            Map<String, SmtExpression> expressions = new HashMap<>();
            for (Map.Entry<String, String> kv : stringyExpressions.entrySet()) {
                String statement = kv.getKey();
                SmtExpression expression = null;
                if (kv.getValue() != null) {
                    expression = SmtExpressionReader.parse(kv.getValue());
                }
                expressions.put(statement, expression);
            }
            return this.withBranchExpressions(expressions);
        }

        public SmtReportBuilder withBranchExpressions(Map<String, SmtExpression> expressions) {
            this.branchOutSmtExpressions = expressions;
            return this;
        }

        public SmtReportBuilder withBranchVariables(Map<String, Set<Local>> variables) {
            this.branchVariables = variables;
            return this;
        }

        public SmtReportBuilder withBranchChangedVariables(Map<String, Set<Local>> variables) {
            this.branchChangedVariables = variables;
            return this;
        }

        public SmtReport build() {
            return new SmtReport(
                this.statements,
                this.variables,
                this.fallThroughSmtExpressions,
                this.fallVariables,
                this.fallChangedVariables,
                this.branchOutSmtExpressions,
                this.branchVariables,
                this.branchChangedVariables);
        }

    }

    public SmtReport(Set<String> statements,
                     Set<Local> variables,
                     Map<String, SmtExpression> fallThroughExprs,
                     Map<String, Set<Local>> fallVariables,
                     Map<String, SmtExpression> branchOutExprs,
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

    public SmtReport(Set<String> statements,
                     Set<Local> variables,
                     Map<String, SmtExpression> fallThroughExprs,
                     Map<String, Set<Local>> fallVariables,
                     Map<String, Set<Local>> fallChangedVariables,
                     Map<String, SmtExpression> branchOutExprs,
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

    public Optional<SmtExpression> getFallThrough(String statement) {
        return Optional.ofNullable(this.fallThroughSmtExpressions.get(statement));
    }

    public Optional<SmtExpression> getBranchOut(String statement) {
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
        return this.format(expr -> expr.toString());
    }

    public String format(Function<SmtExpression, String> repr) {
        StringBuilder sb = new StringBuilder();
        if (this.variables.size() == 0) {
            return "";
        }
        sb.append(this.variables.stream().map(v -> v.toString()).sorted().collect(Collectors.joining("\t")));
        sb.append("\n");
        this.statements.stream()
            .sorted(AnalysisSMTReport::compareStatements)
            .forEach(statement -> {
                sb.append(statement);
                sb.append("\n");
                Optional<SmtExpression> fall = this.getFallThrough(statement);
                Optional<SmtExpression> branch = this.getBranchOut(statement);
                String fallChangedVariables = this.getFallChangedVariables(statement)
                    .map(vars -> vars.stream().map(v -> v.toString()).sorted().collect(Collectors.joining("\t", "", "\t")))
                    .orElse("");
                String branchChangedVariables = this.getBranchChangedVariables(statement)
                    .map(vars -> vars.stream().map(v -> v.toString()).sorted().collect(Collectors.joining("\t", "", "\t")))
                    .orElse("");
                fall.ifPresent(expr -> {
                        sb.append("fall\t");
                        sb.append(fallChangedVariables);
                        sb.append(repr.apply(expr));
                        sb.append("\n");
                    });
                branch.ifPresent(expr -> {
                        sb.append("branch\t");
                        sb.append(branchChangedVariables);
                        sb.append(repr.apply(expr));
                        sb.append("\n");
                    });
            });
        return sb.toString();
    }
}
