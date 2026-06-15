package dev.fmsea.absint.scalar;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import dev.fmsea.absint.ConstraintType;
import dev.fmsea.absint.scalar.state.State;
import dev.fmsea.picotelem.engine.PicoTelemetryEngine;
import dev.fmsea.solver.SolverWrapper;
import dev.fmsea.util.IOThrowableConsumer;
import soot.Body;
import soot.Local;
import soot.Unit;
import soot.toolkits.graph.Orderer;

public class IntegerAnalysisWithInflow extends IntegerAnalysis {

    public IntegerAnalysisWithInflow(
        SolverWrapper solver,
        Body b,
        int iterations,
        Class<?> type,
        Set<Integer> widenSteps,
        Orderer<Unit> orderer,
        boolean reduceOutput,
        PicoTelemetryEngine telemetry) {

        super(solver, b, iterations, type, widenSteps, orderer, reduceOutput, telemetry);
    }

    public void reportConstraintTypes(Writer writer) throws IOException {
        IOThrowableConsumer<Map.Entry<ConstraintType, Set<Local>>> variableWriter = kv -> {
            kv.getValue().stream()
                .map(Local::toString)
                .sorted()
                .forEach(v -> {
                try {
                    writer.write(String.format("%s\t%s\n",
                        v,
                        kv.getKey().toString()));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        };
        int stmtCount = 0;
        for (Unit u : this.g.getBody().getUnits()) {
            stmtCount++;
            writeUnitSignature(writer, u, stmtCount);
            writer.write("before\n");
            State before = this.analysis.getFlowBefore(u);
            if (reduceOutput) {
                before.reduce();
            }
            before.queryConstraintTypes()
                .entrySet()
                .stream()
                .forEach(kv -> variableWriter.accept(kv));
            if (this.analysis.getFallMinChangedVariables(u).isPresent()) {
                writer.write("fall\n");
                State state = analysis.getFallFlowAfter(u);
                state.queryConstraintTypes()
                    .entrySet()
                    .stream()
                    .forEach(kv -> variableWriter.accept(kv));
            } else {
            }
            List<State> branches = analysis.getBranchFlowAfter(u);
            for (State branch : branches) {
                Set<Local> branchDeltaVars = this.analysis.getBranchMinChangedVariables(u).orElse(Set.of());
                if (branchDeltaVars.isEmpty()) {
                    continue;
                }
                writer.write("branch\n");
                branch.queryConstraintTypes()
                    .entrySet()
                    .stream()
                    .forEach(kv -> variableWriter.accept(kv));
            }
        }
    }

    public void writeReport(Writer writer) throws IOException {
        writer.write(this.locals.stream().map(l -> l.toString()).sorted().collect(Collectors.joining("\t")));
        writer.write("\n");
        Set<Unit> outputStmt = this.analysis.getOutputStatements();
        Map<Unit, Set<Local>> variables = this.getChangedVariables();
        int stmtCount = 0;
        for (Unit u : this.g.getBody().getUnits()) {
            stmtCount++;
            writeUnitSignature(writer, u, stmtCount);
            State before = analysis.getFlowBefore(u);
            if (reduceOutput) {
                before.reduce();
            }
            writer.write("before\t");
            writer.write(before.toSMT(this.solver));
            writer.write('\n');
            if (outputStmt.contains(u) && variables.get(u).size() > 0) {
                State state = analysis.getFallFlowAfter(u);
                writer.write("fall\t");
                writer.write(this.analysis.getFallMinChangedVariables(u)
                    .map(vars -> vars
                        .stream()
                        .map(v -> v.toString())
                        .sorted()
                        .collect(Collectors.joining("\t", "", "\t")))
                    .orElse(""));
                if (this.reduceOutput) {
                    state.reduce();
                }
                writer.write(state.toSMT(this.solver));
                writer.write("\n");
                List<State> branches = analysis.getBranchFlowAfter(u);
                for (State branch : branches) {
                    writer.write("branch\t");
                    writer.write(this.analysis.getBranchMinChangedVariables(u)
                        .map(vars -> vars
                            .stream()
                            .map(v -> v.toString())
                            .sorted()
                            .collect(Collectors.joining("\t", "", "\t")))
                        .orElse(""));
                    if (this.reduceOutput) {
                        branch.reduce();
                    }
                    writer.write(branch.toSMT(this.solver));
                    writer.write("\n");
                }
                writer.flush();
            }
        }
    }
}
