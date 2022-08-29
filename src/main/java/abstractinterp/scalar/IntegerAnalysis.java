package abstractinterp.scalar;

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
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import soot.Body;
import soot.Local;
import soot.Trap;
import soot.Unit;
import soot.Value;
import soot.util.Chain;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.LoopNestTree;
import soot.toolkits.graph.PseudoTopologicalOrderer;
import soot.toolkits.graph.UnitGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import abstractinterp.scalar.state.State;
import abstractinterp.scalar.state.factory.StateFactory;
import solver.SolverWrapper;
import solver.SolverFactory;

public class IntegerAnalysis<S extends State> implements Analysis {
    private static final Logger LOGGER = LoggerFactory.getLogger(IntegerAnalysis.class);
    protected Body b;
    UnitGraph g;
    ForwardBranchedFlowNumerical<S> analysis;
    private SolverWrapper solver;
    private Set<Local> locals;

    public IntegerAnalysis(Body b, int iterations, StateFactory<S> stateFactory) {
        this(SolverFactory.getSolver(), b, iterations, stateFactory);
    }

    public IntegerAnalysis(SolverWrapper solver, Body b, int iterations, StateFactory<S> stateFactory) {
        this.solver = solver;
        this.b = b;
        this.g = new ExceptionalUnitGraph(b);

        List<Unit> order = new PseudoTopologicalOrderer<Unit>().newList(g, false);
        Map<Unit, S> unitToBeforeFlow = new HashMap<>();
        Map<Unit, List<S>> unitToAfterBranchFlow = new HashMap<>();
        Map<Unit, List<S>> unitToAfterFallFlow = new HashMap<>();
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

        analysis = new ForwardBranchedFlowNumerical<>(g,
                                                      order,
                                                      unitToBeforeFlow,
                                                      unitToAfterBranchFlow,
                                                      unitToAfterFallFlow,
                                                      wideningNode,
                                                      iterations,
                                                      locals,
                                                      stateFactory);

        // setup the flows
        for (Unit node : order) {
            unitToBeforeFlow.put(node, analysis.newInitialFlow());
            List<S> f = new ArrayList<>();
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

    public String generateReport() {
        StringBuilder sb = new StringBuilder();
        for (Unit u : this.g.getBody().getUnits()) {
            sb.append(u);
            sb.append(" ");
            sb.append(u.getClass());
            sb.append(" f->");
            sb.append(analysis.getFallFlowAfter(u));
            sb.append('\n');
            if (u.branches()) {
                sb.append(u);
                sb.append(" b->");
                sb.append(analysis.getBranchFlowAfter(u));
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    public String generateSMTReportFull() {
        return generateOutput((state, _locals) -> state.toSMT(this.solver));
    }

    public String generateSMTReport() {
        return generateOutput((state, locals) -> locals.map(ls -> state.toSMT(ls, this.solver)).orElse("true"));
    }

    public String generateReachableSMTReport() {
        return generateOutput((state, locals) -> locals.map(ls -> state.toReachableSMT(ls, this.solver)).orElse("true"));
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
            List<S> branches = analysis.getBranchFlowAfter(u);
            for (S branch : branches) {
                String branchOutput = Paths.get(output.toString(), String.format("/%d-branch.dot", stmtCount)).toString();
                branch.toGraph().toDot(branchOutput);
            }
        }
    }

    private String generateOutput(BiFunction<State, Optional<Set<Local>>, String> stateFormatter) {
        StringBuilder sb = new StringBuilder();
        Set<String> locals = new TreeSet<>();
        locals.addAll(this.locals.stream().map(l -> l.toString()).collect(Collectors.toSet()));
        for (String l : locals) {
            sb.append(l);
            sb.append("\t");
        }
        // remove last tab
        sb.deleteCharAt(sb.length() - 1);
        sb.append("\n");

        Set<Unit> outputStmt = this.analysis.getOutputStatements();
        Map<Unit, Set<Local>> variables = this.getChangedVariables();
        String methodSignature = this.b.getMethod().getSignature();
        int stmtCount = 0;
        for (Unit u : this.g.getBody().getUnits()) {
            stmtCount++;
            if (outputStmt.contains(u) && variables.get(u).size() > 0) {
                sb.append(stmtCount);
                sb.append(" ");
                sb.append(u);
                sb.append(":");
                sb.append(methodSignature);
                sb.append('\n');
                State state = analysis.getFallFlowAfter(u);
                if (state.isFeasible()) {
                    String fallSmtExpr = stateFormatter.apply(state, Optional.ofNullable(variables.get(u)));
                    if (!fallSmtExpr.isEmpty()) {
                        sb.append("fall\t");
                        sb.append(fallSmtExpr);
                        sb.append("\n");
                    }
                }
                List<S> branches = analysis.getBranchFlowAfter(u);
                for (S branch : branches) {
                    if (branch.isFeasible()) {
                        String branchSmtExpr = stateFormatter.apply(branch, Optional.ofNullable(variables.get(u)));
                        if (!branchSmtExpr.isEmpty()) {
                            sb.append("branch\t");
                            sb.append(branchSmtExpr);
                            sb.append("\n");
                        }
                    }
                }
            }
        }
        return sb.toString();
    }

    protected Map<Unit, Set<Local>> getChangedVariables() {
        return this.analysis.getChangedVariables();
    }

    public void writeSMTReport(Writer writer) throws IOException {
        writer.write(generateSMTReport());
    }

    public void report() {
        System.out.print(generateSMTReport());
    }
}
