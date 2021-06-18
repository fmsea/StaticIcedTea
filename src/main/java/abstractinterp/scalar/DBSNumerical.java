package abstractinterp.scalar;

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
import util.Variables;

import abstractinterp.scalar.state.DifferenceBoundedState;
import solver.SolverWrapper;
import solver.SolverWrapperZ3;

public class DBSNumerical {
    protected Body b;
    UnitGraph g;
    ForwardBranchedFlowDifferenceBoundedNumerical analysis;
    private SolverWrapper solver;

    public DBSNumerical(Body b, int iterations) {
        this(new SolverWrapperZ3(), b, iterations);
    }

    public DBSNumerical(SolverWrapper solver, Body b, int iterations) {
        this.b = b;
        this.g = new ExceptionalUnitGraph(b);
        this.solver = solver;

        List<Unit> order = new PseudoTopologicalOrderer<Unit>().newList(g, false);
        Map<Unit, DifferenceBoundedState> unitToBeforeFlow = new HashMap<>();
        Map<Unit, List<DifferenceBoundedState>> unitToAfterBranchFlow = new HashMap<>();
        Map<Unit, List<DifferenceBoundedState>> unitToAfterFallFlow = new HashMap<>();
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
        analysis =
            new ForwardBranchedFlowDifferenceBoundedNumerical(g,
                                                              order,
                                                              unitToBeforeFlow,
                                                              unitToAfterBranchFlow,
                                                              unitToAfterFallFlow,
                                                              wideningNode,
                                                              iterations,
                                                              locals);

        // setup the flows
        for (Unit node : order) {
            unitToBeforeFlow.put(node, analysis.newInitialFlow());
            List<DifferenceBoundedState> f = new ArrayList<>();
            unitToAfterFallFlow.put(node, f);
            if (node.fallsThrough()) {
                f.add(analysis.newInitialFlow());
            }
            f = new ArrayList<>();
            unitToAfterBranchFlow.put(node, f);
            if (node.branches()) {
                for (int i = 0; i < node.getUnitBoxes().size(); i++) {
                    DifferenceBoundedState v = analysis.newInitialFlow();
                    f.add(v);
                }
            }
        }
        // entry points
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
            DifferenceBoundedState state = analysis.getFallFlowAfter(u);
            sb.append(state.toSMTFormula(this.solver));
            List<DifferenceBoundedState> branches = analysis.getBranchFlowAfter(u);
            for (DifferenceBoundedState branch : branches) {
                sb.append(branch.toSMTFormula(this.solver));
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
                DifferenceBoundedState state = analysis.getFallFlowAfter(u);
                if (state.isFeasible()) {
                    for (Local l : locals) {
                        if (changedVariables.get(u).contains(l)) {
                            sb.append(l.toString());
                            sb.append("->");
                            sb.append(state.LocalToSMTFormula(this.solver, l));
                            sb.append('\n');
                        }
                    }
                    List<DifferenceBoundedState> branches = analysis.getBranchFlowAfter(u);
                    if (!branches.isEmpty()) {
                        for (DifferenceBoundedState branch : branches) {
                            if (branch.isFeasible()) {
                                for (Local l : locals) {
                                    if (changedVariables.get(u).contains(l)) {
                                        sb.append(l.toString());
                                        sb.append("f->");
                                        sb.append(branch.LocalToSMTFormula(this.solver, l));
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

    public void report() {
        System.out.print(generateReport());
    }
}
