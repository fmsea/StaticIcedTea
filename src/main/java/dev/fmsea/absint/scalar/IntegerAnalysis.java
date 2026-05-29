package dev.fmsea.absint.scalar;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import dev.fmsea.absint.scalar.state.State;
import dev.fmsea.absint.scalar.state.factory.StateFactory;
import dev.fmsea.solver.SolverWrapper;
import soot.Body;
import soot.Local;
import soot.Trap;
import soot.Unit;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.LoopNestTree;
import soot.toolkits.graph.Orderer;
import soot.toolkits.graph.UnitGraph;

public class IntegerAnalysis implements Analysis {

    protected Body b;
    UnitGraph g;
    ForwardBranchedFlowNumerical<State> analysis;
    private SolverWrapper solver;
    private Set<Local> locals;
    private final boolean reduceOutput;

    public IntegerAnalysis(
        SolverWrapper solver,
        Body b,
        int iterations,
        Class<?> type,
        Set<Integer> widenSteps,
        Orderer<Unit> orderer,
        boolean reduceOutput) {
        this.solver = solver;
        this.b = b;
        this.g = new ExceptionalUnitGraph(b);
        this.reduceOutput = reduceOutput;

        List<Unit> order = orderer.newList(g, false);
        Map<Unit, State> unitToBeforeFlow = new HashMap<>();
        Map<Unit, List<State>> unitToAfterBranchFlow = new HashMap<>();
        Map<Unit, List<State>> unitToAfterFallFlow = new HashMap<>();
        Set<Unit> wideningNode = new HashSet<>();
        this.locals = new HashSet<>();
        for (Local l : b.getLocals()) {
            locals.add(l);
        }
        // find the head of the loops
        LoopNestTree loopTree = new LoopNestTree(b);
        // can be also used to do the order
        Iterator<Loop> loopIterator = loopTree.descendingIterator();
        while (loopIterator.hasNext()) {
            wideningNode.add(loopIterator.next().getHead());
        }

        analysis = new ForwardBranchedFlowNumerical<State>(
            g,
            order,
            unitToBeforeFlow,
            unitToAfterBranchFlow,
            unitToAfterFallFlow,
            wideningNode,
            iterations,
            locals,
            widenSteps,
            StateFactory.getFactory(type));

        // setup the flows
        for (Unit node : order) {
            unitToBeforeFlow.put(node, analysis.newInitialFlow());
            List<State> f = new ArrayList<>();
            unitToAfterFallFlow.put(node, f);
            if (node.fallsThrough()) {
                f.add(analysis.newInitialFlow());
            }
            f = new ArrayList<>();
            unitToAfterBranchFlow.put(node, f);
            if (node.branches()) {
                for (int i = 0; i < node.getUnitBoxes().size(); i++) {
                    f.add(analysis.newInitialFlow());
                }
            }
        }

        // Entry points
        for (Unit head : g.getHeads()) {
            unitToBeforeFlow.put(head, analysis.entryInitialFlow());
        }
        // traps are treated as entry points
        if (analysis.treatTrapHandlersAsEntries()) {
            for (Trap trap : ((UnitGraph) g).getBody().getTraps()) {
                Unit handler = trap.getHandlerUnit();
                unitToBeforeFlow.put(handler, analysis.entryInitialFlow());
            }
        }
    }

    public void runAnalysis() {
        analysis.doAnalysis();
    }

    public void generateGraphOutputs(Path output) {
        File outputDir = output.toFile();
        outputDir.mkdirs();
        int stmtCount = 0;
        for (Unit u : this.g.getBody().getUnits()) {
            stmtCount++;
            State state = analysis.getFallFlowAfter(u);
            String fallOutput = Paths.get(output.toString(), String.format("/%d-fall.dot", stmtCount)).toString();
            state.toGraph().toDot(fallOutput);
            List<State> branches = analysis.getBranchFlowAfter(u);
            for (State branch : branches) {
                String branchOutput = Paths.get(output.toString(), String.format("/%d-branch.dot", stmtCount)).toString();
                branch.toGraph().toDot(branchOutput);
            }
        }
    }

    protected Map<Unit, Set<Local>> getChangedVariables() {
        return this.analysis.getChangedVariables();
    }

    public void writeReport(Writer writer) throws IOException {
        writer.write(this.locals.stream().map(l -> l.toString()).sorted().collect(Collectors.joining("\t")));
        writer.write("\n");
        Set<Unit> outputStmt = this.analysis.getOutputStatements();
        Map<Unit, Set<Local>> variables = this.getChangedVariables();
        String methodSignature = this.b.getMethod().getSignature();
        int stmtCount = 0;
        for (Unit u : this.g.getBody().getUnits()) {
            stmtCount++;
            if (outputStmt.contains(u) && variables.get(u).size() > 0) {
                writer.write(String.valueOf(stmtCount));
                writer.write(" ");
                writer.write(u.toString());
                writer.write(":");
                writer.write(methodSignature);
                writer.write('\n');
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
