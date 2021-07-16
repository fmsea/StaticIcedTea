package abstractinterp.scalar;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import solver.SolverWrapperZ3;

public class IntegerAnalysis<S extends State> {
    private static final Logger LOGGER = LoggerFactory.getLogger(IntegerAnalysis.class);
    protected Body b;
    UnitGraph g;
    ForwardBranchedFlowNumerical<S> analysis;
    private SolverWrapper solver;

    public IntegerAnalysis(Body b, int iterations, StateFactory<S> stateFactory) {
        this(new SolverWrapperZ3(), b, iterations, stateFactory);
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
        Set<Local> locals = new HashSet<>();
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
        // for (Unit head : g.getHeads()) {
        //     unitToBeforeFlow.put(head, analysis.entryInitialFlow());
        // }
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
        StringBuilder sb = new StringBuilder();
        String methodSignature = this.b.getMethod().getSignature();
        int stmtCount = 0;
        for (Unit u : this.g.getBody().getUnits()) {
            stmtCount++;
            sb.append(stmtCount);
            sb.append(" ");
            sb.append(u);
            sb.append(":");
            sb.append(methodSignature);
            sb.append('\n');
            State state = analysis.getFallFlowAfter(u);
            sb.append(state.toSMT(this.solver));
            List<S> branches = analysis.getBranchFlowAfter(u);
            for (S branch : branches) {
                sb.append(branch.toSMT(this.solver));
            }
        }
        return sb.toString();
    }

    public String generateSMTReport() {
        StringBuilder sb = new StringBuilder();
        String methodSignature = this.b.getMethod().getSignature();
        Set<Unit> outputStmt = this.analysis.getOutputStatements();
        Map<Unit, Set<Value>> changedVariables = this.analysis.getChangedVariables();
        Chain<Local> locals = this.b.getLocals();
        int stmtCount = 0;
        for (Unit u : this.g.getBody().getUnits()) {
            stmtCount++;
            if (outputStmt.contains(u)) {
                sb.append(stmtCount);
                sb.append(" ");
                sb.append(u);
                sb.append(":");
                sb.append(methodSignature);
                sb.append('\n');
                S state = analysis.getFallFlowAfter(u);
                if (state.isFeasible()) {
                    for (Local l : locals) {
                        if (changedVariables.get(u).contains(l)) {
                            sb.append(l.toString());
                            sb.append("->");
                            sb.append(state.toSMT(l, this.solver));
                            sb.append('\n');
                        }
                    }
                    List<S> branches = analysis.getBranchFlowAfter(u);
                    if (!branches.isEmpty()) {
                        for (S branch : branches) {
                            if (branch.isFeasible()) {
                                for (Local l : locals) {
                                    if (changedVariables.get(u).contains(l)) {
                                        sb.append(l.toString());
                                        sb.append("f->");
                                        sb.append(branch.toSMT(l, this.solver));
                                        sb.append('\n');
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return sb.toString();
    }

    public void writeSMTReport(Writer writer) throws IOException {
        writer.write(generateSMTReport());
    }

    public void report() {
        System.out.print(generateReport());
    }
}
