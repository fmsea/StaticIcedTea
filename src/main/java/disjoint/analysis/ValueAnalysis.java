package disjoint.analysis;

import java.io.IOException;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import soot.ByteType;
import soot.IntType;
import soot.Local;
import soot.LongType;
import soot.ShortType;
import soot.Body;
import soot.BooleanType;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.grimp.Grimp;
import soot.grimp.internal.GAndExpr;
import soot.grimp.internal.GEqExpr;
import soot.grimp.internal.GOrExpr;
import soot.jimple.AndExpr;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.ConditionExpr;
import soot.jimple.CmpExpr;
import soot.jimple.EqExpr;
import soot.jimple.Expr;
import soot.jimple.GeExpr;
import soot.jimple.GtExpr;
import soot.jimple.IfStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.IntConstant;
import soot.jimple.LongConstant;
import soot.jimple.LeExpr;
import soot.jimple.LtExpr;
import soot.jimple.NeExpr;
import soot.jimple.NumericConstant;
import soot.jimple.OrExpr;
import soot.jimple.ShlExpr;
import soot.jimple.ShrExpr;
import soot.jimple.Stmt;
import soot.jimple.XorExpr;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JGeExpr;
import soot.jimple.internal.JGtExpr;
import soot.jimple.internal.JLeExpr;
import soot.jimple.internal.JLtExpr;
import soot.jimple.internal.JNeExpr;
import soot.jimple.internal.JNegExpr;
import soot.jimple.internal.JUshrExpr;
import soot.jimple.internal.JimpleLocal;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardBranchedFlowAnalysis;
//import abstractinterp.scalar.ForwardBranchedFlowAnalysis;
import soot.util.Chain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import disjoint.domain.BaseElement;
import disjoint.domain.Domain;
import disjoint.driver.StartAnalysis;
import solver.SolverWrapper;
import solver.SolverFactory;
import disjoint.state.*;
import abstractinterp.scalar.state.DeferredCmpMap;
import abstractinterp.scalar.state.PredicateType;

/**
 * The core of the analysis and
 * transfer function algorithms
 * @author elenasherman
 *
 */
public class ValueAnalysis extends ForwardBranchedFlowAnalysis<AbstractState> {
//public class ValueAnalysis extends ForwardBranchedFlowAnalysis<Unit, AbstractState> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValueAnalysis.class);

    //only write to the file states of those statements
    protected Set<Unit> outputStmt;

    // map of deferred comparisons, used when long types are compared and need to be later refined.
    private DeferredCmpMap deferredComparisons;

    //in the current implementation would have
    //at most three states:
    //IntervalState, UnstructuredState and SymbolicState
    private List<State> states;

    /*
     * Keeps track which domain is used for
     * which state in the list of intervals states
     */
    private int disjointDomainIndex;

    /*
     * Maps a domain to its index in the list of bitvectors
     * in varToValue map in disjoint.state.ItervalState.java
     */
    private Map<Domain,Integer> disjointDomainToIndex;
    private Map<Integer, Domain> indexToDisjointDomain;

    /*
     * Keeps track which domain is used for
     * which state in the list of unstructured sates
     */
    private int unstructuredDomainIndex;
    /*
     * Maps an unstructured domain to its index in the
     * list of sets of bitvectors in disjoint.state.UnstructuredState.java
     */
    private Map<Domain,Integer> unstructuredDomainToIndex;
    private Map<Integer, Domain> indexToUnstructuredDomain;
    protected SolverWrapper solver;

    /*
     * use that to output values
     * only of those variables
     * that have been changed after the state
     */
    protected Map<Unit,Set<Local>> changedVariables;

    /*
     * analysis execution time
     */
    protected long time;

    /*
     * the body of the analyzed method
     */
    protected Body b;

    private boolean addIntervalDomain(Domain d){
        disjointDomainToIndex.put(d, disjointDomainIndex);
        indexToDisjointDomain.put(disjointDomainIndex, d);
        disjointDomainIndex++;
        return true;
    }

    private boolean addUnstructuredDomain(Domain d){
        unstructuredDomainToIndex.put(d, unstructuredDomainIndex);
        indexToUnstructuredDomain.put(unstructuredDomainIndex, d);
        unstructuredDomainIndex++;
        return true;
    }


    public ValueAnalysis(UnitGraph graph, List<Domain> setDomains, boolean symbolicOn) {
        super(graph);

        solver = SolverFactory.getSolver();
        //initialize the type of states that will be used in the analysis
        states = new ArrayList<State>();
        IntervalStates iState = null;
        UnstructuredStates uState = null;
        //instantiate the right state for each domain
        for(Domain domain : setDomains){
            if(domain.isDisjoint()){
                if(iState == null){
                    //need to clear it for different method analysis
                    //when multiple methods of the same class are analyzed
                    //in the same run -- in case code is modified to do that
                    IntervalStates.bitsInDomain.clear();
                    //instantiate one and initialize indices
                    iState = new IntervalStates();
                    states.add(iState);
                    disjointDomainToIndex = new HashMap<Domain, Integer>();
                    indexToDisjointDomain = new HashMap<Integer, Domain>();
                    disjointDomainIndex = 0;
                }
                //now add the domain itself
                addIntervalDomain(domain);
            } else {
                //domain is unstructured
                if(uState == null){
                    //instantiate the unstructured state
                    UnstructuredStates.bitsInDomain.clear();
                    unstructuredDomainToIndex = new HashMap<Domain, Integer>();
                    indexToUnstructuredDomain = new HashMap<Integer, Domain>();
                    unstructuredDomainIndex = 0;
                    uState = new UnstructuredStates();
                    states.add(uState);
                }
                //add the domain
                addUnstructuredDomain(domain);
            }
        }

        //update the sizes of bitvectors in each domain
        for(int i=0; i< disjointDomainIndex; i++){
            int size = indexToDisjointDomain.get(i).size();
            LOGGER.debug("Size {}", size);
            IntervalStates.bitsInDomain.add(size);
        }

        LOGGER.debug("index {}", unstructuredDomainIndex);
        for(int i = 0; i < unstructuredDomainIndex; i++){
            int size = indexToUnstructuredDomain.get(i).size();
            UnstructuredStates.bitsInDomain.add(size);
        }

        //----------- adding the one and only symbolic state
        b = graph.getBody();
        if(symbolicOn){
            SymbolicState.allStmt = new HashSet<Stmt>();
            Iterator<Unit> iterUnit = b.getUnits().iterator();
            while(iterUnit.hasNext()){
                Unit u = iterUnit.next();
                if(u instanceof Stmt){
                    SymbolicState.allStmt.add((Stmt)u);
                }
            }
            SymbolicState ss = new SymbolicState();
            states.add(ss);
        }
        //initial and entry flows set up
        Chain<Local> locals = b.getLocals();
        AbstractState.setLocals(locals);
        outputStmt = new HashSet<Unit>();
        changedVariables = new HashMap<>();
        deferredComparisons = new DeferredCmpMap();
    }


    public void start(){
        //start the main analysis and time it
        long start = System.currentTimeMillis();
        doAnalysis(); //call it explicitly after setting up the domains
        long end = System.currentTimeMillis();
        //done with the analysis
        time = end - start;
        //reporting part
        LOGGER.info("{}\t{}\t analyzed in {}ms",
                    b.getMethod().getDeclaringClass(),
                    b.getMethod().getSignature(),
                    time);
    }

    public void writeFullSMT(Writer writer) throws IOException {
        writer.write(b.getLocals().stream().map(l -> l.toString()).sorted().collect(Collectors.joining("\t")));
        writer.write("\n");

        String methodSignature = this.b.getMethod().getSignature();
        int stmtCount = 0;
        for (Unit unit : this.b.getUnits()) {
            stmtCount++;
            if (outputStmt.contains(unit)) {
                writer.write(String.valueOf(stmtCount));
                writer.write(" ");
                writer.write(unit.toString());
                writer.write(":");
                writer.write(methodSignature);
                writer.write("\n");
                AbstractState fall = getFallFlowAfter(unit);
                String changedVariablesForUnit = Optional.ofNullable(this.changedVariables.get(unit))
                    .map(vars -> vars
                         .stream()
                         .map(v -> v.toString())
                         .sorted()
                         .collect(Collectors.joining("\t", "", "\t")))
                    .orElse("");
                String fallExpr = formatState(fall);
                writer.write("fall\t");
                writer.write(changedVariablesForUnit);
                writer.write(fallExpr);
                writer.write("\n");
                List<AbstractState> branches = getBranchFlowAfter(unit);
                for (AbstractState branch : branches) {
                    String branchExpr = formatState(branch);
                    writer.write("branch\t");
                    writer.write(changedVariablesForUnit);
                    writer.write(branchExpr);
                    writer.write("\n");
                }
                writer.flush();
            }
        }
    }

    public void reportFullSMT() {
        try (PrintWriter writer = new PrintWriter(System.out);
             BufferedWriter buf = new BufferedWriter(writer)) {
            this.writeFullSMT(buf);
            buf.flush();
        } catch (IOException ex) {
            LOGGER.error("Unable to write full SMT report: {}", ex);
        }
    }

    public void writeSymbolicSMT(Writer writer) throws IOException {
        writer.write(b.getLocals().stream().map(l -> l.toString()).sorted().collect(Collectors.joining("\t")));
        writer.write("\n");
        String methodSignature = this.b.getMethod().getSignature();
        int stmtCount = 0;
        for (Unit unit : this.b.getUnits()) {
            stmtCount++;
            if (outputStmt.contains(unit)) {
                writer.write(String.valueOf(stmtCount));
                writer.write(" ");
                writer.write(unit.toString());
                writer.write(":");
                writer.write(methodSignature);
                writer.write("\n");
                AbstractState fall = getFallFlowAfter(unit);
                String changedVariables = Optional.ofNullable(this.changedVariables.get(unit))
                    .map(vars -> vars
                         .stream()
                         .map(v -> v.toString())
                         .sorted()
                         .collect(Collectors.joining("\t", "", "\t")))
                    .orElse("");
                if (!fall.getStates().isEmpty() && fall.isFeasible()) {
                    Optional<SymbolicState> symbState = fall.getStates().stream()
                        .filter(s -> s instanceof SymbolicState)
                        .map(s -> (SymbolicState)s)
                        .findFirst();
                    writer.write(symbState.map(state -> {
                                StringBuilder sb = new StringBuilder();
                                sb.append("fall\t");
                                sb.append(changedVariables);
                                sb.append(state.toSMT(this.solver));
                                sb.append("\n");
                                return sb.toString();
                            }).orElse("fall\ttrue\n"));
                } else {
                    writer.write("fall\t");
                    writer.write(changedVariables);
                    writer.write("false\n");
                }
                List<AbstractState> branches = getBranchFlowAfter(unit);
                for (AbstractState branch : branches) {
                    if (!branch.getStates().isEmpty() && branch.isFeasible()) {
                        Optional<SymbolicState> symbBranch = branch.getStates().stream()
                            .filter(s -> s instanceof SymbolicState)
                            .map(s -> (SymbolicState)s)
                            .findFirst();
                        if (symbBranch.isPresent()) {
                            writer.write(symbBranch.map(state -> {
                                        StringBuilder sb = new StringBuilder();
                                        sb.append("branch\t");
                                        sb.append(state.toSMT(this.solver));
                                        sb.append("\n");
                                        return sb.toString();
                                    }).get());
                        }
                    }  else {
                        writer.write("branch\t");
                        writer.write(changedVariables);
                        writer.write("false\n");
                    }
                }
                writer.flush();
            }
        }
    }

    public void reportSymbolicSMT() {
        try (PrintWriter writer = new PrintWriter(System.out);
             BufferedWriter buf = new BufferedWriter(writer)) {
            this.writeSymbolicSMT(buf);
            buf.flush();
        } catch (IOException ex) {
            LOGGER.error("Unable to output symbolic SMT report: {}", ex);
        }
    }

    private String formatState(AbstractState state) {
        Chain<Local> locals = b.getLocals();
        StringBuilder r = new StringBuilder();
        if (!state.getStates().isEmpty() && state.isFeasible()) {
            locals.stream().map(l -> evaluateStates(state, l).stream()
                                .reduce((a, b) -> Grimp.v().newAndExpr(a, b)))
                .filter(op -> op.isPresent())
                .map(o -> o.get())
                .reduce((a, b) -> Grimp.v().newAndExpr(a, b))
                .ifPresentOrElse(expr -> {
                        r.append(solver.smt2(expr));
                        r.append("\n");
                    }, () -> r.append("true\n"));
        } else if (!state.getStates().isEmpty() && !state.isFeasible()) {
            r.append("false\n");
        } else {
            r.append("true\n");
        }
        return r.toString();
    }

    public void writeReport(Writer writer) throws IOException {
        //printing the result
        writer.write(b.getLocals().stream().map(l -> l.toString()).sorted().collect(Collectors.joining("\t")));
        writer.write("\n");
        Iterator<Unit> iter = b.getUnits().iterator();
        int stmtCount = 0;
        while(iter.hasNext()){
            Unit u = iter.next();
            //check against statement after
            //which state has been changed
            stmtCount++;
            if(outputStmt.contains(u)){
                writer.write(String.valueOf(stmtCount));
                writer.write(" ");
                writer.write(u.toString());
                writer.write(":");
                writer.write(b.getMethod().getSignature());
                writer.write("\n");
                AbstractState fall = getFallFlowAfter(u);
                if(!fall.getStates().isEmpty() && fall.isFeasible()){
                    Optional<Set<Local>> vars = Optional.ofNullable(changedVariables.get(u));
                    writer.write("fall\t");
                    writer.write(vars.flatMap(vs -> vs.stream()
                                              .sorted((a, b) -> a.toString().compareTo(b.toString()))
                                              .flatMap(v -> evaluateStates(fall, v).stream())
                                              .reduce((a, b) -> Grimp.v().newAndExpr(a, b)))
                                 .map(expr -> solver.smt2(expr))
                                 .orElse("true"));
                    writer.write("\n");
                }
                //for other branch outcome if one exists
                List<AbstractState> branches = getBranchFlowAfter(u);
                if(!branches.isEmpty()){
                    for(AbstractState branch : branches){
                        if(!branch.getStates().isEmpty() && branch.isFeasible()){
                            Optional<Set<Local>> vars = Optional.ofNullable(changedVariables.get(u));
                            writer.write("branch\t");
                            writer.write(vars.flatMap(vs -> vs.stream()
                                                      .sorted((a, b) -> a.toString().compareTo(b.toString()))
                                                      .flatMap(v -> evaluateStates(branch, v).stream())
                                                      .reduce((a, b) -> Grimp.v().newAndExpr(a, b)))
                                         .map(expr -> solver.smt2(expr))
                                         .orElse("true"));
                            writer.write("\n");
                        }

                    }
                }
                writer.flush();
            }//end outputStmt check
        }
    }

    public void report() {
        try (PrintWriter writer = new PrintWriter(System.out);
             BufferedWriter buf = new BufferedWriter(writer)) {
            writeReport(buf);
            buf.flush();
        } catch (IOException ex) {
            LOGGER.error("Unable to output report due to exception: {}", ex);
        }
    }

    @Override
    protected void flowThrough(AbstractState in, Unit u, List<AbstractState> fallIn,
            List<AbstractState> branchOut) {
        Stmt s = (Stmt) u;
        LOGGER.debug("Unit {}", u);
        LOGGER.debug("In {}", in);
        AbstractState inState = in;
        AbstractState ifStmtTrue = inState.copy(); //instantiated in ifStmt only; outBranch
        AbstractState ifStmtFalse = inState.copy();//fallIn; out
        //only do the computation for a feasible inState
        //check only for integers?
        if(in.isFeasible()){
            if (s instanceof AssignStmt) {
                processAssignStmt((AssignStmt)s, inState, ifStmtFalse);

            } else if (s instanceof IfStmt) {
                processIfStmt((IfStmt) s, inState, ifStmtFalse, ifStmtTrue);
            }
            //other statement modify nothing
        }


        for (Iterator<AbstractState> it = fallIn.iterator(); it.hasNext();) {
            copy(ifStmtFalse, it.next());
        }

        for (Iterator<AbstractState> it = branchOut.iterator(); it.hasNext();) {
            copy(ifStmtTrue, it.next());
        }

        LOGGER.debug("outFall {}", ifStmtFalse);
        LOGGER.debug("outBranch {}", ifStmtTrue);

    }

    //coming from feasible to infeasible happens on conditional stmts only
    protected void processIfStmt(IfStmt s, AbstractState inState,
            AbstractState ifStmtFalse, AbstractState ifStmtTrue) {
        ConditionExpr condExpr = (ConditionExpr)s.getCondition();
        PredicateType type = PredicateType.fromJimple(condExpr);
        Value lhs = condExpr.getOp1();
        Value rhs = condExpr.getOp2();
        //make sure this is an integer conditional stmt
        if (isAnyIntType(lhs)) {
            if (lhs instanceof Local && this.deferredComparisons.contains((Local)lhs)) {
                this.deferredComparisons.get((Local) lhs).ifPresentOrElse(p -> {
                        Value exprLhs = p.fst();
                        Value exprRhs = p.snd();
                        processCondition(s,
                                         PredicateType.toJimple(type, exprLhs, exprRhs),
                                         inState,
                                         ifStmtFalse,
                                         ifStmtTrue,
                                         exprLhs,
                                         exprRhs);
                    }, () -> processCondition(s, condExpr, inState, ifStmtFalse, ifStmtTrue, lhs, rhs));
            } else {
                processCondition(s, condExpr, inState, ifStmtFalse, ifStmtTrue, lhs, rhs);
            }
        }

    }

    private void processCondition(IfStmt s,
                                  ConditionExpr condExpr,
                                  AbstractState inState,
                                  AbstractState ifStmtFalse,
                                  AbstractState ifStmtTrue,
                                  Value left,
                                  Value right) {
        //add it to the tracked states
            outputStmt.add(s);
            //create the set of variables to be tracked
            Set<Local> track = new HashSet<>();
            changedVariables.put(s, track);
            //precondition of the IfStmt
            Set<Expr> precond = new HashSet<Expr>();
            Set<Value> valuesToEval = new HashSet<Value>();//there should be one value only
            addNotNull(findLocal(left), valuesToEval);
            addNotNull(findLocal(right), valuesToEval);
            //need to make a special case when valuesToEval is empty
            //it means that both sides are concrete values
            //hence no need call for the solver
            for(Value v : valuesToEval){
                precond.addAll(evaluateStates(inState, v));
            }

            precond.addAll(evaluateStates(inState, null));//to evaluate symbolic state only
            //otherwise symbolic state can be evaluated twice -- equality of BinOp has not been implemented
            //it looks like in Jimple only statement of the same object are equal, but not
            //if they have the same semantics, assuming that locals are of the same object

            //do for true branch
            //add the current expression
            BinopExpr symbState = condExpr;
            BinopExpr symbNotState = negate(condExpr);
            for(Expr be : precond){
                symbState = new GAndExpr(symbState, be);
                symbNotState = new GAndExpr(symbNotState,be);
            }

            //at this point we have precondition set
            //make sure left is not a constant
            if(left instanceof JimpleLocal){
                //find new values for left
                updateStateCond(left, symbState, condExpr, ifStmtTrue, s);//s is only used for the symbolic state
                updateStateCond(left, symbNotState,negate(condExpr), ifStmtFalse, s);
                condExpr = null; //so no need to update the symbolic state twice
                track.add((Local)left);
            }
            //make sure right is not a constant
            if(right instanceof JimpleLocal){
                updateStateCond(right, symbState, condExpr, ifStmtTrue, s);
                updateStateCond(right, symbNotState, negate(condExpr), ifStmtFalse,s );
                track.add((Local)right);
            }
    }

    private BinopExpr negate(ConditionExpr condExpr) {
        BinopExpr ret = null;
        if(condExpr != null){
            Value lhs = condExpr.getOp1();
            Value rhs = condExpr.getOp2();
            if (condExpr instanceof EqExpr) {
                ret = new JNeExpr(lhs, rhs);
            } else if (condExpr instanceof NeExpr) {
                ret = new JEqExpr(lhs, rhs);
            } else if (condExpr instanceof LeExpr) {
                ret = new JGtExpr(lhs, rhs);
            } else if (condExpr instanceof GtExpr) {
                ret = new JLeExpr(lhs, rhs);
            } else if (condExpr instanceof GeExpr) {
                ret = new JLtExpr(lhs, rhs);
            } else if (condExpr instanceof LtExpr) {
                ret = new JGeExpr(lhs, rhs);
            }
        }
        return ret;
    }

    //the main difference between this method and updateStateAssign is the
    //set of base elements to choose the solution from
    //in updateStateCond is v's value in precondition
    //while in updateStateAssign is from all possible values
    private void updateStateCond(Value v, BinopExpr symbState, BinopExpr expr,
            AbstractState outState, IfStmt stmt){

        List<State> states = outState.getStates();
        for(State state : states){
            if(state instanceof IntervalStates){
                //this is the current value of v in each interval domain
                List<BitSet> intervalVal = ((IntervalStates)state).getState(v);
                //now iterate for each state/domain
                for(int i = 0; i < intervalVal.size(); i++){
                    BitSet value = intervalVal.get(i);
                    Domain d = indexToDisjointDomain.get(i);
                    //this is an optimization
                    //instead of iterating through all possible values
                    //we know that only subset of current values
                    //will be propagated to true/false branch.
                    Set<BaseElement> currentValSet = d.getElements(value);
                    Set<BaseElement> newSet = transferDisjoint(symbState, v, currentValSet);
                    //translate the set to bitvectors
                    BitSet newValue = d.getBitSet(newSet);
                    intervalVal.set(i, newValue);
                    if(newValue.isEmpty()){
                        //means the branch is infeasible
                        outState.setInfeasible();
                        //no need to finish the calculations for the rest of domains
                        break;
                    }
                }
            } else if (state instanceof UnstructuredStates){
                List<Set<BitSet>> unstructuredVal = ((UnstructuredStates) state).getState(v);
                for(int i=0; i < unstructuredVal.size(); i++){
                    //do the same optimization by setting
                    //top to those elements that can occur
                    Domain d = indexToUnstructuredDomain.get(i);
                    Set<Set<BaseElement>> newVals = transferBileteral(symbState, v, d);
                    Set<BitSet> retVals = new HashSet<BitSet>();
                    //translate each inner set into the bitvector
                    for(Set<BaseElement> newVal : newVals){
                        BitSet newBitVal = d.getBitSet(newVal);
                        retVals.add(newBitVal);
                    }
                    //update the state with the new value
                    unstructuredVal.set(i, retVals);
                    //detect infeasible state
                    if(newVals.isEmpty()){
                        outState.setInfeasible();
                        //no need to finish loop
                        //at least one domain
                        //detected that branch is infeasible
                        break;
                    }
                }

            }else if (state instanceof SymbolicState){
                //we need to add both the conditional statment itself
                //and the binop expr
                if(expr != null){
                    //stored as a grimp binop expression
                    ((SymbolicState) state).add(stmt, expr);
                }
            }
            if(!outState.isFeasible()){
                break;
            }
        }
    }

    protected void processAssignStmt(AssignStmt s, AbstractState inState,
            AbstractState outState) {
        Value lhs = s.getLeftOp();
        //only process if lhs of the desired type, otherwise states
        //will be unchanged.
        Value rhs = s.getRightOp();
        if(isAnyIntType(lhs)){
            //output state for this statement
            outputStmt.add(s);
            //create the set of variables to be tracked
            Set<Local> track = new HashSet<Local>();
            changedVariables.put(s, track);
            track.add((Local)lhs);
            //identify rhs
            Set<BinopExpr> precond = new HashSet<BinopExpr>();
            //create a temp local variable since in a loop
            //it is not SSA,e.g., i1 = i1+0
            JimpleLocal temp = new JimpleLocal("temp", lhs.getType());
            //change assignments into equality -- using grimp format to hold i0 = i + 1
            //because jimple does not allow rhs to be an expression
            EqExpr current = new GEqExpr(temp, rhs);
            Set<Value> valuesToEval = new HashSet<Value>();
            if (rhs instanceof JNegExpr) {
                addNotNull(findLocal(((JNegExpr) rhs).getOp()), valuesToEval);
            } else if (rhs instanceof BinopExpr && !(rhs instanceof AndExpr) &&
                       !(rhs instanceof XorExpr) && !(rhs instanceof OrExpr) &&
                       !(rhs instanceof JUshrExpr)) {

                // Interjection: If the BinopExpr operator is CMP, we are going
                // to defer the refinement.

                BinopExpr bexpr = (BinopExpr) rhs;
                Value exprLhs = bexpr.getOp1();
                Value exprRhs = bexpr.getOp2();
                if (rhs instanceof CmpExpr) {
                    // add lhs and rhs to deferred map.
                    this.deferredComparisons.put((Local)lhs, exprLhs, exprRhs);
                    return;
                } else {
                    //can only handle some non-linear operations
                    //might be different for a different solvers
                    //thus a good place for re-factoring the code
                    //but it would make it more slow since we need
                    //to go back and forth between encodings to
                    //realize that something in the solver is not
                    //supported.
                    //But there is a doubt that some solvers
                    //have such support
                    if(rhs instanceof ShlExpr || rhs instanceof ShrExpr){
                        //check weather esprRhs is not an constant
                        //Z3 cannot handle those operation
                        //perhaps should be outsourced to the solver
                        if(!(exprRhs instanceof IntConstant)){
                            //cannot handle it, update it to top
                            updateStateTop(lhs, outState);
                            return;
                        }
                    }
                    addNotNull(findLocal(exprRhs),valuesToEval);
                    addNotNull(findLocal(exprLhs),valuesToEval);
                }
            } else if (rhs instanceof JimpleLocal || //definitely can change it
                       rhs instanceof NumericConstant){
                addNotNull(findLocal(rhs),valuesToEval);
            } else {
                updateStateTop(lhs, outState);
                return;
            }


            //for each value get its representation in each state
            for(Value v : valuesToEval){
                precond.addAll(evaluateStates(inState, v));
            }

            precond.addAll(evaluateStates(inState, null));//to evaluate symbolic state only
            //otherwise symbolic state can be evaluated twice -- equality of BinOp has not been implemented
            //it looks like in Jimple only statement of the same object are equal, but not
            //if they have the same semantics, assuming that locals are of the same object

            //create one big formula
            BinopExpr symbState = current;
            for(BinopExpr pre : precond){
                symbState = new GAndExpr(pre,symbState);
            }

            //temp on what to do the calculations
            //lhs for what to do the updates
            updateStateAssign(temp,lhs, symbState, outState);

            //can update symbolic state right now
            for(State state : outState.getStates()){
                if(state instanceof SymbolicState){
                    //should figure out it internally what to add and how to deal with i0 = i0 + 1
                    ((SymbolicState) state).add(s);
                }
            }

        }

    }


    //sets lhs to top in all substates
    private void updateStateTop(Value lhs, AbstractState outState) {
        List<State> states = outState.getStates();
        for(State state : states){
            if(state instanceof SymbolicState){
                SymbolicState ss = (SymbolicState) state;
                //for symbolic state remove
                //all previous assignment to lhs
                //and stmt where lhs was used
                ss.removeLhsDepndencies(lhs);
            } else {
                state.initEntryVar(lhs);
            }
        }

    }

    private void updateStateAssign(Value temp, Value lhs, BinopExpr symbState,
            AbstractState outState) {
        //should be extracted into a sper
        List<State> states = outState.getStates();
        for(State state : states){
            if(state instanceof IntervalStates){
                List<BitSet> intervalVal = ((IntervalStates) state).getState(lhs);
                //translate bitset of each domain to its actual predicates
                for(int i = 0; i < intervalVal.size(); i++){
                    Domain d = indexToDisjointDomain.get(i);
                    Set<BaseElement> newSet = transferDisjoint(symbState, temp, d.getBaseElements());
                    BitSet newValue = d.getBitSet(newSet);
                    intervalVal.set(i, newValue);
                }
            } else if (state instanceof UnstructuredStates){
                List<Set<BitSet>> unstructVal = ((UnstructuredStates) state).getState(lhs);
                for(int i = 0; i < unstructVal.size(); i++){
                    Domain d = indexToUnstructuredDomain.get(i);
                    Set<Set<BaseElement>> newVals = transferBileteral(symbState, temp, d);
                    Set<BitSet> retVals = new HashSet<BitSet>();
                    //translate each inner set into the bitvector
                    for(Set<BaseElement> newVal : newVals){
                        BitSet newBitVal = d.getBitSet(newVal);
                        retVals.add(newBitVal);
                    }
                    //update the state with the new value
                    unstructVal.set(i, retVals);
                }
            }
        }
    }

    /*
     * Bilateral algorithm of Reps at al.
     */
    private Set<Set<BaseElement>> transferBileteral(BinopExpr symbState, Value lhs,
            Domain d){
        Set<Set<BaseElement>> lowerDNF = new HashSet<Set<BaseElement>>();//dnf
        Set<Set<BaseElement>> lowerCNF = new HashSet<Set<BaseElement>>();//cnf

        Set<Set<BaseElement>> upper = d.topCNF();//cnf

        //the first iteration just query symbSate and get the solutions and
        //update both lower sets
        List<Long> sol = solver.evaluateSol(symbState, null, lhs);
        if (sol == null){
            //return top, i.e., over approximation in dnf
            return d.topDNF();
        } else if (sol.isEmpty()){
            //return bot since no solution exists
            return lowerDNF;
        } else {
            //there is a solution
            //find a set of predicates satisfying it
            Set<BaseElement> satSet = d.getSatPredicates(sol, new Value[]{lhs});
            //add both of them to the lower
            //cnf each in its separate set
            for(BaseElement be : satSet){
                Set<BaseElement> set = new HashSet<BaseElement>();
                set.add(be);
                lowerCNF.add(set);
            }
            //dnf as a single set
            lowerDNF.add(satSet);

            //the main loop
            boolean equals = false;
            while(!equals){
                Set<BaseElement> current = d.abstractConsequence(lowerCNF, upper);
                //convert current into BoolExpr
                BinopExpr currentExpr = d.instantiate(current, lhs);
                //find the solution
                sol = solver.evaluateSol(symbState, currentExpr, lhs);
                if(sol == null){
                    //return top in dnf
                    LOGGER.warn("Timing out, returning upper in DNF");
                    lowerDNF = d.fromCNFtoDNF(upper);
                    //remove unsat predicates?
                    Set<Set<BaseElement>> toRemove = new HashSet<Set<BaseElement>>();
                    for(Set<BaseElement> set : lowerDNF){
                        BinopExpr evalSet = d.instantiateCNFSingle(set, lhs);
                        if(!solver.evaluate(evalSet)){
                            //unsat conjunction
                            toRemove.add(set);
                        }
                    }
                    //remove from the DNF
                    lowerDNF.removeAll(toRemove);
                    return lowerDNF;
                } else if (sol.isEmpty()){
                    // update upper -- glb with current
                    upper.add(current);
                } else {
                    //there is a solution
                    //find a set of predicates satisfying it
                    satSet = d.getSatPredicates(sol, new Value[]{lhs});//why it was null before
                    if(satSet.isEmpty()){
                        //something wrong, perhaps incomplete domain
                        LOGGER.warn("For non-empty solution found no predicates!");
                    }
                    LOGGER.debug("satSet {}", satSet);
                    //add both of them to the lower
                    //cnf double loop
                    //Set<Set<BaseElement>> toRemove = new HashSet<Set<BaseElement>>();

                    Set<Set<BaseElement>> lowerCNFNew = new HashSet<Set<BaseElement>>();
                    //the algorithm for converting two disjunctions of conjuncts
                    //into CNF, i.e., adding to each old disjunction a new
                    //predicate from satSet for each each satSet predicate
                    //if lowerCNF = P1 and P2, and satSet = {p3,p4},
                    // where P1, P2 are disjuncts then
                    // lowerCNF or (p3 and p4) becomes
                    //(P1 or p3) and (P1 or p4) and (P2 or p3) and (P2 or p4)
                    for(Set<BaseElement> cnfSet : lowerCNF){
                        for(BaseElement be : satSet){
                            Set<BaseElement> set = new HashSet<BaseElement>();
                            set.add(be);
                            set.addAll(cnfSet);
                            lowerCNFNew.add(set);
                        }
                    }
                    lowerCNF.clear();
                    lowerCNF.addAll(lowerCNFNew);


                    //dnf as a single set
                    lowerDNF.add(satSet);
                }
                //check if upper and lowerCNF are equivalent using
                //the sat solver? or actual set comparison?
                //upper has the dafault top value, not
                //sure how to determine the equivalence,
                //so use the solver for now;
                BinopExpr lowerCNFbinop = d.instantiateCNF(lowerCNF, lhs);
                BinopExpr upperBinop = d.instantiateCNF(upper, lhs);
                equals = solver.equals(lowerCNFbinop, upperBinop);
            }
        }
        return lowerDNF;
    }

    //yep just couple lines of code comparing Bilateral :)
    private Set<BaseElement> transferDisjoint(BinopExpr symbState,
            Value lhs, Set<BaseElement> values) {
        Set<BaseElement> ret = new HashSet<BaseElement>();
        for(BaseElement be : values){
            //evaluate the whole formula
            BinopExpr newSymbState = new GAndExpr(symbState, be.instantiate(lhs));
            boolean sat = solver.evaluate(newSymbState);
            if(sat){
                ret.add(be);
            }
        }
        return ret;
    }

    private void addNotNull(Value v, Set<Value> values){
        if(v != null){//null in case it is a constant
            values.add(v);
        }
    }

    private Value findLocal(Value expr){
        Value ret = null;
        if(expr instanceof JimpleLocal){
            ret = expr;
        }
        return ret;
    }

    protected Set<BinopExpr> evaluateStates(AbstractState inState, Value v){
        Set<BinopExpr> ret = new HashSet<BinopExpr>();
        //should be extracted into a sper
        List<State> states = inState.getStates();
        for (State state : states) {
            if (state instanceof IntervalStates && v != null) {
                List<BitSet> intervalVal = ((IntervalStates) state).getState(v);
                if (intervalVal == null) {
                    continue;
                }
                //translate bitset of each domain to its actual predicates
                for (int i = 0; i < intervalVal.size(); i++) {
                    BitSet value = intervalVal.get(i);
                    Domain d = indexToDisjointDomain.get(i);
                    BinopExpr dExpr = d.instantiate(value, v);
                    ret.add(dExpr);
                }
            } else if (state instanceof UnstructuredStates & v != null) {
                List<Set<BitSet>> unstructuredVal = ((UnstructuredStates) state).getState(v);
                if (unstructuredVal == null) {
                    continue;
                }
                //for each domain
                for (int i = 0; i < unstructuredVal.size(); i++) {
                    Set<BitSet> value = unstructuredVal.get(i);
                    BinopExpr dExpr = null;
                    //for each set of bitvectors -- a conjunction
                    for (BitSet bs : value) {
                        Domain d = indexToUnstructuredDomain.get(i);
                        if(dExpr != null){
                            //second iteration
                            dExpr = new GOrExpr(dExpr, d.instantiate(bs, v));//disjunction of conjunction
                        } else { //first iteration
                            dExpr = d.instantiate(bs, v);//must return conjunction
                        }
                    }
                    if (dExpr == null) {
                        UnstructuredStates ustate = (UnstructuredStates) state;
                        for (Entry<Value, List<Set<BitSet>>> es : ustate.varToValue.entrySet()) {
                            JimpleLocal jl = (JimpleLocal)es.getKey();
                        }
                        //something is wrong
                        LOGGER.error("Domain value is null");
                        System.exit(2);
                    }
                    ret.add(dExpr);
                }
            } else if (state instanceof SymbolicState & v == null){
                SymbolicState symbState = (SymbolicState) state;
                //iterate over stmt
                //if it cond stmt, get its binopExpr
                //otherwise convert assign stmt to the equality binopExpr
                for (Stmt stmt : symbState.getStaments()) {
                    //TODO: slice on variable v?
                    if (stmt instanceof IfStmt) {
                        ret.add(symbState.getBinop(stmt));
                    } else {
                        AssignStmt assignStmt = (AssignStmt)stmt; //it's either an assignment or conditional stmt
                        BinopExpr newExpr = new GEqExpr(assignStmt.getLeftOp(),
                                                        assignStmt.getRightOp());
                        ret.add(newExpr);
                    }
                }
            }
        }
        return ret;
    }

    @Override
    protected void merge(AbstractState in1, AbstractState in2, AbstractState out) {
        LOGGER.debug("Merging {} {} with {} {}",
                     in1, in1.isFeasible(),
                     in2, in2.isFeasible());
        if(in1.isFeasible() && in2.isFeasible()){
            out.copy(in1.merge(in2)); //regular
        } else if (!in2.isFeasible()){
            copy(in1,out); //if in2 is infeasible then copy in1 to out, does not matter if in1 is feasible or not
        } else if (!in1.isFeasible()){
            copy(in2,out); //if in2 is feasible but in1 is infeasible then copy in2
        }
    }

    @Override
    protected void copy(AbstractState source, AbstractState dest) {
        dest.copy(source);
    }

    @Override
    protected AbstractState newInitialFlow() {
        AbstractState as = new AbstractState(states);
        as.setInitialFlow();
        return as;
    }

    @Override
    protected AbstractState entryInitialFlow() {
        AbstractState as = new AbstractState(states);
        as.setEntryFlow();
        return as;
    }

    public static boolean isAnyIntType(Value val){
        Type t = val.getType();
        return (!(val instanceof ArrayRef) &&
                !(val instanceof InstanceFieldRef) &&
                (t instanceof IntType ||
                 t instanceof LongType ||
                 t instanceof ByteType ||
                 t instanceof ShortType ||
                 t instanceof BooleanType));
    }

    public long getRunTime(){
        return time;
    }

}
