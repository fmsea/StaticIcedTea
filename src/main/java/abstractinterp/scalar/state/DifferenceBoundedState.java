package abstractinterp.scalar.state;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.BFSShortestPath;
import org.jgrapht.alg.shortestpath.BellmanFordShortestPath;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;
import org.jgrapht.alg.shortestpath.NegativeCycleDetectedException;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.IntType;
import soot.Local;
import soot.Value;
import soot.grimp.Grimp;
import soot.jimple.BinopExpr;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.internal.JNegExpr;

import solver.SolverWrapper;

public class DifferenceBoundedState implements State {

    private Graph<Local, Constraint> graph;
    private boolean feasible = true;
    private Logger LOGGER;
    public final Local ZERO;

    public DifferenceBoundedState(Set<Local> locals, boolean top) {
        this(locals, top, true);
    }

    public DifferenceBoundedState(DifferenceBoundedState state) {
        this(state.graph.vertexSet(), false, state.feasible);
        copyGraph(state.graph, this.graph);
    }

    public DifferenceBoundedState(Set<Local> locals, boolean top, boolean feasible) {
        this.LOGGER = LoggerFactory.getLogger(DifferenceBoundedState.class);
        this.feasible = feasible;
        this.graph = new DefaultDirectedGraph<>(Constraint.class);
        this.ZERO = Variable.ZERO;
        this.graph.addVertex(this.ZERO);
        for (Local l : locals) {
            this.graph.addVertex(l);
            // if (top) {
            //     this.graph.addEdge(l, this.ZERO, Constraint.TOP());
            // } else {
            //     // this probably will need to change to Max or something different...
            //     this.graph.addEdge(l, this.ZERO, Constraint.BOT());
            // }
        }
    }

    /** Return the set of known locals of the graph.
     */
    public Set<Local> getLocals() {
        return this.graph.vertexSet();
    }

    public State copy() {
        return new DifferenceBoundedState(this);
    }

    public void copyTo(State destination) {
        if (destination instanceof DifferenceBoundedState) {
            this.copyTo((DifferenceBoundedState) destination);
        } else {
            throw new RuntimeException("invalid type for copyTo");
        }
    }

    /** Add a constraint through the ZERO element.
     *
     * We remove any existing edge before adding the new edge/constraint.
     */
    public void add(Local l, Constraint constraint) {
        this.add(l, this.ZERO, constraint);
    }

    /** Add a constraint through the two provided locals.
     *
     * The ordering implies the relationship between variables:
     * l - r < c
     *
     * Remove the edge before adding the new edge of the constraint.
     */
    public void add(Local l, Local r, Constraint constraint) {
        this.graph.removeEdge(l, r);
        this.graph.addEdge(l, r, constraint);
    }

    public boolean intersection(DifferenceBoundedState inState) {
        for (Constraint c : inState.graph.edgeSet()) {
            Local s = inState.graph.getEdgeSource(c);
            Local t = inState.graph.getEdgeTarget(c);
            if (this.getValue(s, t).compareTo(c) == 1) {
                this.add(s, t, c);
            }
        }
        return this.isFeasible();
    }


    /** Get the constraint through the ZERO element.
     *
     * If no edge exists, we return TOP automatically.
     *
     * This method shall never return null.
     */
    public Constraint getValue(Local l) {
        return this.getValue(l, this.ZERO);
    }

    /** Get the constraint connecting the two locals.
     *
     * If no edge directly connects the two locals, we return TOP.
     *
     * This method shall never return null.
     */
    public Constraint getValue(Local l, Local r) {
        Constraint c = this.eval(l, r);
        if (c == null) {
            c = Constraint.TOP();
        }
        return c;
    }

    private Constraint eval(Value v) {
        Constraint c;
        if (v instanceof IntConstant) {
            c = this.eval((IntConstant) v);
        } else if (v instanceof Local) {
            c = this.eval((Local) v);
        } else {
            c = Constraint.TOP();
        }
        return c;
    }

    private Constraint eval(IntConstant constant) {
        return new Constraint(constant.value, PredicateType.Eq);
    }

    private Constraint eval(Local l) {
        return this.eval(l, this.ZERO);
    }

    private Constraint eval(Local l, Local r) {
        return this.graph.getEdge(l, r);
    }

    private boolean computeClosure() {
        // early exit, no point if graph contains bottoms
        if (this.anyBottoms()) {
            return false;
        }
        Set<Local> vertices = this.graph.vertexSet();
        int dim = vertices.size();
        Map<Local, Integer> indices = new HashMap<>();
        Map<Integer, Local> indicesToVertices = new HashMap<>();
        {
            int i = 0;
            for (Local l : vertices) {
                indices.put(l, i);
                indicesToVertices.put(i, l);
                i++;
            }
        }
        Constraint[][] dbm = new Constraint[dim][dim];
        for (int i = 0; i < dim; i++) {
            Arrays.fill(dbm[i], Constraint.TOP());
            dbm[i][i] = new Constraint(0, PredicateType.Le);
        }

        for (Constraint c : this.graph.edgeSet()) {
            Local s = this.graph.getEdgeSource(c);
            Local t = this.graph.getEdgeTarget(c);
            int si = indices.get(s);
            int ti = indices.get(t);
            dbm[si][ti] = c;
        }

        // Floyd-Warshall Shortest Paths
        for (int k = 0; k < dim; k++) {
            for (int i = 0; i < dim; i++) {
                for (int j = 0; j < dim; j++) {
                    Constraint c = Constraint.add(dbm[i][k], dbm[k][j]);
                    Constraint m = Constraint.min(dbm[i][j], c);
                    dbm[i][j] = m;
                }
            }
        }

        // Update Graph with computed Constraints
        for (Local v1 : vertices) {
            for (Local v2 : vertices) {
                int v1i = indices.get(v1);
                int v2i = indices.get(v2);
                // only copy non-top edges to graph
                if (v1i == v2i && dbm[v1i][v2i].bound() == 0) {
                    continue;
                } else if (dbm[v1i][v2i].isTop()) {
                    // if the computed edge is top, remove the edge
                    this.graph.removeEdge(v1, v2);
                } else {
                    this.add(v1, v2, dbm[v1i][v2i]);
                }
            }
        }

        this.feasible = true;
        for (int i = 0; i < dim; i++) {
            if (dbm[i][i].bound() < 0) {
                this.feasible = false;
            }
        }
        return this.feasible;
    }

    private boolean computeClosureBF() {
        Graph<Local, DefaultEdge> g = new DefaultDirectedWeightedGraph<>(DefaultEdge.class);
        this.graph.vertexSet().forEach(v -> g.addVertex(v));
        this.graph.vertexSet().forEach(v1 -> {
                this.graph.vertexSet().forEach(v2 -> {
                        DefaultEdge e;
                        if (v1.equals(v2)) {
                            e = g.addEdge(v1, v2);
                            g.setEdgeWeight(e, 0.0);
                        } else {
                            e = g.addEdge(v1, v2);
                            g.setEdgeWeight(e, Double.POSITIVE_INFINITY);
                        }
                    });
            });
        this.graph.edgeSet().forEach(c -> {
                Local s = this.graph.getEdgeSource(c);
                Local t = this.graph.getEdgeTarget(c);
                DefaultEdge e = g.getEdge(s, t);
                g.setEdgeWeight(e, c.bound());
            });
        BellmanFordShortestPath<Local, DefaultEdge> sps = new BellmanFordShortestPath<>(g);
        for (Local v : g.vertexSet()) {
            try {
                sps.getPaths(v);
            } catch (NegativeCycleDetectedException ex) {
                return false;
            }
        }
        return true;
    }

    private boolean anyBottoms() {
        for (Constraint b : this.graph.edgeSet()) {
            if (b.isBottom()) {
                return true;
            }
        }
        return false;
    }

    public boolean isFeasible() {
        DifferenceBoundedState state = new DifferenceBoundedState(this);
        this.feasible = state.computeClosure() && !state.anyBottoms();
        LOGGER.trace("this graph: {}", this.graph);
        LOGGER.trace("closed graph: {}", state.graph);
        return this.feasible;
    }


    public void widenWith(State inState) {
        if (inState instanceof DifferenceBoundedState) {
            widenWith((DifferenceBoundedState) inState);
        } else {
            throw new RuntimeException("invlaid types for widen");
        }
    }

    public void widenWith(DifferenceBoundedState inState) {
        LOGGER.debug("widening this {} with {}", this.graph, inState.graph);
        Graph<Local, Constraint> copy = new DefaultDirectedGraph<>(Constraint.class);
        copyGraph(this.graph, copy);

        // Close inState
        inState.computeClosure();

        for (Constraint c1 : copy.edgeSet()) {
            Local s = copy.getEdgeSource(c1);
            Local t = copy.getEdgeTarget(c1);
            Constraint c2 = inState.getValue(s, t);
            LOGGER.trace("Widening edge: {} < {} ? {}",
                         c1, c2, c1.compareTo(c2));
            if (!(c1.isBottom() || c2.isBottom()) && c1.compareTo(c2) == -1) {
                this.add(s, t, Constraint.TOP());
            }
        }
    }

    public void mergeWith(State inState) {
        if (inState instanceof DifferenceBoundedState) {
            mergeWith((DifferenceBoundedState) inState);
        } else {
            throw new RuntimeException("invalid types for merge");
        }
    }

    public void mergeWith(DifferenceBoundedState inState) {
        // To ensure the best results, both graphs need to be "strongly closed"...
        LOGGER.debug("Merging Paths...");
        LOGGER.trace("Merge {} with {}", this.graph, inState.graph);
        this.computeClosure();
        inState.computeClosure();
        LOGGER.debug("isFeasible ? {} and {}", this.feasible, inState.feasible);
        Graph<Local, Constraint> copy = new DefaultDirectedGraph<>(Constraint.class);
        copyGraph(this.graph, copy);
        Map<Local, Local> visited = new HashMap<>();
        for (Constraint c1 : copy.edgeSet()) {
            Local s = copy.getEdgeSource(c1);
            Local t = copy.getEdgeTarget(c1);
            Constraint c2 = inState.graph.getEdge(s, t);
            visited.put(s, t);
            if (c2 != null) {
                LOGGER.trace("Join: max({}, {}) = {}", c1, c2, Constraint.max(c1, c2));
                this.add(s, t, Constraint.max(c1, c2));
            } else {
                LOGGER.trace("Join: max({}, ∅) = {}", c1, c1);
                this.add(s, t, c1);
            }
        }
        for (Constraint c1 : inState.graph.edgeSet()) {
            Local s = inState.graph.getEdgeSource(c1);
            Local t = inState.graph.getEdgeTarget(c1);
            Local v;
            if ((v = visited.get(s)) != null && v.equals(t)) {
                continue;
            } else {
                Constraint c2 = copy.getEdge(s, t);
                if (c2 != null) {
                    LOGGER.trace("Join: max({}, {}) = {}", c2, c1, Constraint.max(c2, c1));
                    this.add(s, t, Constraint.max(c2, c1));
                } else {
                    LOGGER.trace("Join: max(∅, {}) = {}", c1, c1);
                    this.add(s, t, c1);
                }
            }
        }
    }

    private static void copyGraph(Graph<Local, Constraint> in,
                                  Graph<Local, Constraint> out) {
        in.vertexSet().forEach(v -> out.addVertex(v));
        in.edgeSet().forEach(e -> {
                Local s = in.getEdgeSource(e);
                Local t = in.getEdgeTarget(e);
                out.removeEdge(s, t);
                out.addEdge(s, t, e.copy());
            });
    }

    public static State initialFlow(Set<Local> locals, boolean top) {
        return new DifferenceBoundedState(locals, top);
    }

    public void copyTo(DifferenceBoundedState out) {
        if (out != null) {
            out.feasible = this.feasible;
            copyGraph(this.graph, out.graph);
        }
    }

    public static Constraint negate(Constraint constraint) {
        Constraint c = constraint.copy();
        c.negate();
        return c;
    }

    public void updateState(Local lVar, State inState, Value v) {
        if (inState instanceof DifferenceBoundedState) {
            updateState(lVar, (DifferenceBoundedState) inState, v);
        } else {
            throw new RuntimeException("invalid type for updateState");
        }
    }

    /** Assign lVar to the unary expression of v
     *
     */
    public void updateState(Local lVar, DifferenceBoundedState inState, Value v) {
        // we are assigning a value to lVar, no existing relationships hold.
        this.forgetConstraints(lVar);

        if (v instanceof JNegExpr) {
            v = ((JNegExpr) v).getOp();
            Constraint c = eval(inState, v);
            this.add(this.ZERO, lVar, c);
        } else if (v instanceof IntConstant) {
            IntConstant ic = (IntConstant) v;
            Constraint c = new Constraint(ic.value, PredicateType.Eq);
            this.add(lVar, this.ZERO, c);
        } else if (v instanceof Local) {
            this.add(lVar, (Local) v, new Constraint(0, PredicateType.Eq));
        } else {
            this.add(lVar, this.ZERO, Constraint.TOP());
        }
    }

    public void updateState(Local lVar,
                            State inState,
                            Value left,
                            Value right,
                            BinaryOperator operator) {
        if (inState instanceof DifferenceBoundedState) {
            updateState(lVar, (DifferenceBoundedState) inState, left, right, operator);
        } else {
            throw new RuntimeException("invalid type for updateState");
        }
    }

    /** Assign lVar to the result of the provided binary expression
     *
     * This method does the type checking to determine which overloaded
     * updateState should be called.  See the overloaded methods for more
     * details about the implementation.
     */
    public void updateState(Local lVar,
                            DifferenceBoundedState inState,
                            Value left,
                            Value right,
                            BinaryOperator type) {
        LOGGER.debug("{} = {} ({}) {}", lVar, left, type, right);
        LOGGER.trace("inState: {}", inState);
        if (left instanceof Local && right instanceof IntConstant) {
            this.updateState(lVar, inState, (Local) left, (IntConstant) right, type);
        } else if (left instanceof IntConstant &&
                   right instanceof Local) {
            this.updateState(lVar, inState, (IntConstant) left, (Local) right, type);
        } else if (left instanceof Local && right instanceof Local) {
            this.updateState(lVar, inState, (Local) left, (Local) right, type);
        } else {
            this.add(lVar, this.ZERO, Constraint.TOP());
        }
    }

    /** Assign lVar value of constant arithmetic
     *
     * It's unlikely this method is necessary since Soot compiles out
     * (optimizes) constant arithmetic.  We leave it here for testing and
     * completeness.  However, since we are currently modeling 32-bit Integers,
     * the Constraint arithmetic goes to TOP on overflow and underflow
     * conditions.
     */
    public void updateState(Local lVar,
                            DifferenceBoundedState inState,
                            IntConstant left,
                            IntConstant right,
                            BinaryOperator op) {
        Constraint c = Constraint.transferBinary(left, right, op);
        this.add(lVar, this.ZERO, c);
    }

    /** Assign lVar constraint with respect to a single local.
     *
     * We distinguish between whether the local on the right hand side *is*
     * lVar or a different local.
     *
     * In the case that, lVar == left, we update the edges of lVar according to
     * the constant `right`.  If lVar != left, we remove all edges containing
     * lVar and add the assignment of the current expression.  However, if the
     * expression involves multiplication or division, we project to the
     * interval domain and compute the result, otherwise, we set the relation
     * between lVar and left to TOP.
     */
    public void updateState(Local lVar,
                            DifferenceBoundedState inState,
                            Local left,
                            IntConstant right,
                            BinaryOperator op) {
        Constraint leftConstraint;
        Constraint rightConstraint = this.eval(right);
        switch (op) {
        case ADDITION:
            if (lVar.equals(left)) {
                inState.graph.outgoingEdgesOf(lVar).forEach(e -> {
                        Constraint x = e.copy();
                        Constraint c = new Constraint(right.value);
                        x.add(c);
                        Local s = inState.graph.getEdgeSource(e);
                        Local t = inState.graph.getEdgeTarget(e);
                        this.add(s, t, x);
                    });
                inState.graph.incomingEdgesOf(lVar).forEach(e -> {
                        Constraint x = e.copy();
                        Constraint c = new Constraint(right.value);
                        x.subtract(c);
                        Local s = inState.graph.getEdgeSource(e);
                        Local t = inState.graph.getEdgeTarget(e);
                        this.add(s, t, x);
                    });
            } else {
                forgetConstraints(lVar);
                this.add(lVar, left, new Constraint(right.value));
            }
            break;
        case SUBTRACTION:
            if (lVar.equals(left)) {
                inState.graph.outgoingEdgesOf(lVar).forEach(e -> {
                        Constraint x = e.copy();
                        Constraint c = new Constraint(right.value, PredicateType.Eq);
                        x.subtract(c);
                        Local s = inState.graph.getEdgeSource(e);
                        Local t = inState.graph.getEdgeTarget(e);
                        this.add(s, t, x);
                    });
                inState.graph.incomingEdgesOf(lVar).forEach(e -> {
                        Constraint x = e.copy();
                        Constraint c = new Constraint(right.value, PredicateType.Eq);
                        x.add(c);
                        Local s = inState.graph.getEdgeSource(e);
                        Local t = inState.graph.getEdgeTarget(e);
                        this.add(s, t, x);
                    });
            } else {
                forgetConstraints(lVar);
                this.add(lVar, left, new Constraint(right.value * -1));
            }
            break;
        case MULTIPLICATION:
            this.projectInterval(left, inState);
            forgetConstraints(lVar);
            if ((leftConstraint = this.eval(left)) != null) {
                Constraint c = leftConstraint.copy();
                c.multiply(rightConstraint);
                this.add(lVar, c);
                break;
            }
        case DIVISION:
            this.projectInterval(left, inState);
            forgetConstraints(lVar);
            if ((leftConstraint = this.eval(left)) != null) {
                Constraint c = leftConstraint.copy();
                c.divide(rightConstraint);
                this.add(lVar, c);
                break;
            }
        case MODULUS:
        case BAND:
        case BOR:
        case BSHL:
        case BSHR:
        case BUSHR:
        case XOR:
        case INVALID:
        default:
            // update edges containing lVar to ⟙
            inState.graph.edgesOf(lVar).forEach(e -> {
                    Local s = inState.graph.getEdgeSource(e);
                    Local t = inState.graph.getEdgeTarget(e);
                    this.add(s, t, Constraint.TOP());
                });
        }
        this.projectInterval(lVar, this);
    }

    /** Assign lVar constraint with respect to single local
     *
     * Similar to updateState where the local appears on the left hand side,
     * however, we unable to fully encode subtraction cases.  Like before, we
     * project and compute the interval values if we cannot compute the
     * relational domain values.
     */
    public void updateState(Local lVar,
                            DifferenceBoundedState inState,
                            IntConstant left,
                            Local right,
                            BinaryOperator op) {
        Constraint leftConstraint = this.eval(left);
        Constraint rightConstraint;
        switch (op) {
        case ADDITION:
            if (lVar.equals(right)) {
                inState.graph.outgoingEdgesOf(lVar).forEach(e -> {
                        Constraint x = e.copy();
                        Constraint c = new Constraint(left.value, PredicateType.Eq);
                        x.add(c);
                        Local s = inState.graph.getEdgeSource(e);
                        Local t = inState.graph.getEdgeTarget(e);
                        this.add(s, t, x);
                    });
                inState.graph.incomingEdgesOf(lVar).forEach(e -> {
                        Constraint x = e.copy();
                        Constraint c = new Constraint(left.value, PredicateType.Eq);
                        x.subtract(c);
                        Local s = inState.graph.getEdgeSource(e);
                        Local t = inState.graph.getEdgeTarget(e);
                        this.add(s, t, x);
                    });
            } else {
                this.forgetConstraints(lVar);
                this.add(lVar, right, new Constraint(left.value));
            }
            break;
        case SUBTRACTION:
            this.projectInterval(right, inState);
            this.forgetConstraints(lVar);
            if ((rightConstraint = this.eval(right)) != null) {
                this.add(lVar, leftConstraint.subtract(rightConstraint));
                break;
            }
        case MULTIPLICATION:
            this.projectInterval(right, inState);
            this.forgetConstraints(lVar);
            if ((rightConstraint = this.eval(right)) != null) {
                this.add(lVar, leftConstraint.multiply(rightConstraint));
                break;
            }
        case DIVISION:
            this.projectInterval(right, inState);
            this.forgetConstraints(lVar);
            if ((rightConstraint = this.eval(right)) != null) {
                this.add(lVar, leftConstraint.divide(rightConstraint));
                break;
            }
        case MODULUS:
        case BAND:
        case BOR:
        case BSHL:
        case BSHR:
        case BUSHR:
        case XOR:
        case INVALID:
        default:
            // update edges containing lVar to ⟙
            inState.graph.edgesOf(lVar).forEach(e -> {
                    Local s = inState.graph.getEdgeSource(e);
                    Local t = inState.graph.getEdgeTarget(e);
                    this.add(s, t, Constraint.TOP());
                });
        }
        this.projectInterval(lVar, this);
    }

    /** Assign lVar with respect to two locals
     *
     * We do not have decision procedures that can handle the transfer of a
     * local with respect to two locals, we project to the interval domain, if
     * available.
     */
    public void updateState(Local lVar,
                            DifferenceBoundedState inState,
                            Local left,
                            Local right,
                            BinaryOperator op) {
        Constraint c;
        Constraint d;
        switch (op) {
        case ADDITION:
            if ((c = inState.graph.getEdge(left, right)) != null) {
                this.add(lVar, this.ZERO, c.copy());
            } else if ((c =  inState.graph.getEdge(right, left)) != null) {
                this.add(lVar, this.ZERO, c.copy());
            } else if ((c = inState.graph.getEdge(left, this.ZERO)) != null &&
                       (d = inState.graph.getEdge(right, this.ZERO)) != null) {
                this.add(lVar, this.ZERO, c.copy().add(d));
            } else {
                this.graph.edgesOf(lVar).forEach(e -> {
                        Local s = this.graph.getEdgeSource(e);
                        Local t = this.graph.getEdgeTarget(e);
                        this.add(s, t, Constraint.TOP());
                    });
            }
            break;
        case SUBTRACTION:
            if ((c = inState.graph.getEdge(left, right)) != null) {
                this.add(lVar, this.ZERO, c.copy());
            } else if ((c = inState.graph.getEdge(right, left)) != null) {
                this.add(lVar, this.ZERO, new Constraint(c.bound() * -1, c.predicate()));
            } else if ((c = inState.graph.getEdge(left, this.ZERO)) != null &&
                         (d = inState.graph.getEdge(right, this.ZERO)) != null) {
                this.add(lVar, this.ZERO, c.copy().subtract(d));
            } else {
                this.graph.edgesOf(lVar).forEach(e -> {
                        Local s = this.graph.getEdgeSource(e);
                        Local t = this.graph.getEdgeTarget(e);
                        this.add(s, t, Constraint.TOP());
                    });
            }
            break;
        case MULTIPLICATION:
            if ((c = inState.graph.getEdge(left, this.ZERO)) != null &&
                (d = inState.graph.getEdge(right, this.ZERO)) != null) {
                this.add(lVar, this.ZERO, c.copy().multiply(d));
                break;
            }
        case DIVISION:
            if ((c = inState.graph.getEdge(left, this.ZERO)) != null &&
                (d = inState.graph.getEdge(right, this.ZERO)) != null) {
                this.add(lVar, this.ZERO, c.copy().divide(d));
                break;
            }
        case MODULUS:
        case BAND:
        case BOR:
        case BSHL:
        case BSHR:
        case BUSHR:
        case XOR:
        case INVALID:
        default:
            inState.graph.edgesOf(lVar).forEach(e -> {
                    Local s = inState.graph.getEdgeSource(e);
                    Local t = inState.graph.getEdgeTarget(e);
                    this.add(s, t, Constraint.TOP());
                });
        }
        this.projectInterval(lVar, this);
    }

    /** Remove all relations containing the provided Local
     *
     * First computes the closure of the graph then removes any constraint
     * connecting through <i>local</i>
     *
     * @param local Remove connecting edges passing through this variable
     */
    public void forget(Local local) {
        this.computeClosure();
        this.forgetConstraints(local);
    }

    /** Remove all relations containing the provided Local
     *
     * @param l Local to remove connecting edges
     */
    public void forgetConstraints(Local l) {
        this.graph.edgesOf(l).forEach(e -> {
                this.graph.removeEdge(e);
            });
    }

    private static Constraint eval(DifferenceBoundedState inState, Value v) {
        Constraint c;
        if (v instanceof IntConstant) {
            int value = ((IntConstant) v).value;
            c = new Constraint(value, PredicateType.Eq);
        } else if (v instanceof Local) {
            Local l = (Local) v;
            c = inState.getValue(l, Variable.ZERO);
        } else {
            c = Constraint.TOP();
        }
        return c;
    }

    /** Project Interval Bounds for all locals
     *
     */
    public void projectIntervals(DifferenceBoundedState inState) {
        this.graph.vertexSet().forEach(v -> {
                this.projectInterval(v, inState);
            });
    }

    /** Project Interval Bounds for local l
     *
     * @param l Local to Project
     */
    public void projectInterval(Local l, DifferenceBoundedState inState) {
        LOGGER.debug("Projecting Interval for {}", l);
        LOGGER.trace("Before Projection: {}", inState.graph);
        BFSShortestPath<Local, Constraint> bfs = new BFSShortestPath<>(inState.graph);
        GraphPath<Local, Constraint> path = bfs.getPath(l, this.ZERO);
        if (path != null) {
            Constraint newEdge = new Constraint(0, PredicateType.Eq);
            for (Constraint c : path.getEdgeList()) {
                newEdge.add(c);
            }
            this.add(l, this.ZERO, newEdge);
        }
        LOGGER.debug("new projected graph: {}", this.graph);
    }

    @Override
    public String toString() {
        return this.graph.toString();
    }

    public String toSMT(SolverWrapper solver) {
        StringBuilder sb = new StringBuilder();
        this.graph.edgeSet().forEach(c -> {
                Local s = this.graph.getEdgeSource(c);
                Local t = this.graph.getEdgeTarget(c);
                sb.append(s.toString());
                sb.append("->");
                sb.append(solver.smt2(this.toGrimpExpr(s, t, c)));
                sb.append("\n");
            });
        return sb.toString();
    }

    public String toSMT(Local l, SolverWrapper solver) {
        StringBuilder sb = new StringBuilder();
        Set<Constraint> edges = this.graph.edgesOf(l);
        LOGGER.debug("edges: {}", edges);
        if (edges.size() == 0) {
            sb.append(solver.smt2(this.toGrimpExpr(l, this.ZERO, Constraint.TOP())));
        } else if (edges.size() == 1) {
            Constraint c = edges.iterator().next();
            Local s = this.graph.getEdgeSource(c);
            Local t = this.graph.getEdgeTarget(c);
            sb.append(solver.smt2(this.toGrimpExpr(s, t, c)));
        } else {
            sb.append("(and");
            for (Constraint c : edges) {
                Local s = this.graph.getEdgeSource(c);
                Local t = this.graph.getEdgeTarget(c);
                sb.append(" ");
                sb.append(solver.smt2(this.toGrimpExpr(s, t, c)));
            }
            sb.append(")");
        }
        return sb.toString();
    }

    private BinopExpr toGrimpExpr(Local s, Local t, Constraint c) {
        LOGGER.debug("translating {} - {} ? {}", s, t, c);
        BinopExpr r = null;
        Function<Local, BinopExpr> top = l -> {
            return Grimp.v().newOrExpr(Grimp.v().newLeExpr(l, IntConstant.v(0)),
                                       Grimp.v().newGtExpr(l, IntConstant.v(0)));
        };
        Function<PredicateType, BiFunction<Value, Value, BinopExpr>> predExpr = p -> {
            switch (p) {
            case Le:
            return Grimp.v()::newLeExpr;
            case Eq:
            return Grimp.v()::newEqExpr;
            case Lt:
            return Grimp.v()::newLtExpr;
            case Ge:
            return Grimp.v()::newGeExpr;
            case Gt:
            return Grimp.v()::newGtExpr;
            default:
            LOGGER.error("I do not know how to translate {} to grimp", p);
            return null;
            }
        };
        if (c.isBottom()) {
            BinopExpr tBottom = Grimp.v().newAndExpr(Grimp.v().newLeExpr(t, IntConstant.v(0)),
                                                     Grimp.v().newGtExpr(t, IntConstant.v(0)));
            BinopExpr sBottom = Grimp.v().newAndExpr(Grimp.v().newLeExpr(s, IntConstant.v(0)),
                                                     Grimp.v().newGtExpr(s, IntConstant.v(0)));
            if (s.equals(this.ZERO)) {
                r = tBottom;
            } else if (t.equals(this.ZERO)) {
                r = sBottom;
            } else {
                r = Grimp.v().newAndExpr(sBottom, tBottom);
            }
        } else if (!s.equals(ZERO) && t.equals(ZERO) && c.isTop()) {
            r = top.apply(s);
        } else if (!t.equals(ZERO) && s.equals(ZERO) && c.isTop()) {
            r = top.apply(t);
        } else if (t.equals(ZERO)) {
            IntConstant k = IntConstant.v(c.bound());
            r = predExpr.apply(c.predicate()).apply(s, k);
        } else if (s.equals(this.ZERO)) {
            Constraint negated = c.copy().negate();
            IntConstant k = IntConstant.v(negated.bound());
            r = predExpr.apply(negated.predicate()).apply(t, k);
        } else {
            IntConstant k = IntConstant.v(c.bound());
            r = predExpr.apply(c.predicate()).apply(s, Grimp.v().newAddExpr(k, t));
        }
        return r;
    }

    @Override
    public boolean equals(Object o) {
        boolean equal = false;
        if (o != null && o instanceof DifferenceBoundedState) {
            DifferenceBoundedState state = (DifferenceBoundedState)o;
            equal = (this.feasible == state.feasible &&
                     equivalentGraphs(this.graph, state.graph));
        }
        return equal;
    }

    private static boolean equivalentGraphs(Graph<Local, Constraint> g1,
                                            Graph<Local, Constraint> g2) {
        boolean equivalent = true;
        equivalent = equivalent && g1.vertexSet().size() == g2.vertexSet().size();
        // equivalent = equivalent && g1.edgeSet().size() == g2.edgeSet().size();
        for (Constraint c : g1.edgeSet()) {
            Local s = g1.getEdgeSource(c);
            Local t = g1.getEdgeTarget(c);
            Constraint c2 = g2.getEdge(s, t);
            equivalent = equivalent && c.equals(c2);
        }
        return equivalent;
    }

    public void updateTop(Local l) {
        this.graph.outgoingEdgesOf(l).forEach(e -> {
                Local s = this.graph.getEdgeSource(e); // === l;
                Local t = this.graph.getEdgeTarget(e);
                this.add(s, t, Constraint.TOP());
            });
    }

    public void updateTop(Local l, Local r) {
        this.add(l, r, Constraint.TOP());
    }

    /** Make all edges infeasible
     *
     * Deletes all existing edges and replaces them with bottom constraints
     */
    public void makeInfeasible() {
        this.graph.edgeSet().forEach(e -> {
                Local s = this.graph.getEdgeSource(e);
                Local t = this.graph.getEdgeTarget(e);
                this.add(s, t, Constraint.BOT());
            });
    }

    public void updateCond(State inState, Value left, Value right, PredicateType type) {
        if (inState instanceof DifferenceBoundedState) {
            updateCond((DifferenceBoundedState) inState, left, right, type);
        } else {
            throw new RuntimeException("invalid state for update condition");
        }
    }

    public void updateCond(DifferenceBoundedState inState,
                           Value left,
                           Value right,
                           PredicateType type) {
        LOGGER.trace("transfer condition: {} : {} {} {}", inState, left, type, right);
        if (left instanceof Local && right instanceof Local) {
            this.updateCond(inState, (Local) left, (Local) right, type);
        } else if (left instanceof Local && right instanceof IntConstant) {
            this.updateCond(inState, (Local) left, (IntConstant) right, type);
        } else if (left instanceof IntConstant && right instanceof Local) {
            this.updateCond(inState, (IntConstant) left, (Local) right, type);
        } else {
            LOGGER.error("missing handler for x-condition: {} {} {}", left, type, right);
        }
    }

    private boolean transferConditional(Local s, Constraint constraint, DifferenceBoundedState inState) {
        return this.transferConditional(s, this.ZERO, constraint, inState);
    }

    private boolean transferConditional(Local s, Local t, Constraint constraint, DifferenceBoundedState inState) {
        DifferenceBoundedState state = new DifferenceBoundedState(inState);
        if (state.computeClosure()) {
            // only add if constraint is more restrictive
            Constraint c1 = state.getValue(s, t);
            Constraint left = state.getValue(s).copy();
            Constraint right = state.getValue(t).copy();
            left.subtract(right);
            LOGGER.trace("{} - {} -> {} conflicts with {} ? {}",
                         s, t, c1, constraint, c1.conflicts(constraint));
            if (!c1.conflicts(constraint) && !left.conflicts(constraint)) {
                this.add(s, t, constraint);
                this.feasible = true;
            } else {
                this.add(s, t, Constraint.BOT());
                this.feasible = false;
            }
        } else {
            this.add(s, t, Constraint.BOT());
            this.feasible = false;
        }
        LOGGER.debug("{} - {} -> {} path is feasible? {}",
                     s, t, constraint, this.feasible);
        return this.feasible;
    }

    public void updateCond(DifferenceBoundedState inState,
                           Local left,
                           IntConstant right,
                           PredicateType type) {
        switch (type) {
        case Le:
            this.transferConditional(left,
                                     new Constraint(right.value, PredicateType.Le),
                                     inState);
            break;
        case Lt:
            this.transferConditional(left,
                                     new Constraint(right.value - 1, PredicateType.Le),
                                     inState);
            break;
        case Eq:
            this.transferConditional(left,
                                     new Constraint(right.value),
                                     inState);
            break;
        case Ge:
            this.transferConditional(this.ZERO,
                                     left,
                                     new Constraint(right.value * -1, PredicateType.Le),
                                     inState);
            break;
        case Gt:
            this.transferConditional(this.ZERO,
                                     left,
                                     new Constraint((right.value * -1) - 1, PredicateType.Le),
                                     inState);
            break;
        case Ne:
            Constraint c = inState.eval(left);
            if (c != null && c.bound() == right.value) {
                this.feasible = false;
                this.add(left, Constraint.BOT());
                break;
            }
        case Invalid:
        default:
            // We cannot represent the condition, do nothing.
        }
    }

    public void updateCond(DifferenceBoundedState inState,
                           IntConstant left,
                           Local right,
                           PredicateType type) {
        switch (type) {
        case Le:
            this.transferConditional(this.ZERO,
                                     right,
                                     new Constraint(left.value * -1, PredicateType.Le),
                                     inState);
            break;
        case Lt:
            this.transferConditional(this.ZERO,
                                     right,
                                     new Constraint((left.value * -1) - 1, PredicateType.Le),
                                     inState);
            break;
        case Eq:
            this.transferConditional(right,
                                     new Constraint(left.value),
                                     inState);
            break;
        case Ge:
            this.transferConditional(right,
                                     new Constraint(left.value, PredicateType.Le),
                                     inState);
            break;
        case Gt:
            this.transferConditional(right,
                                     new Constraint(left.value + 1, PredicateType.Le),
                                     inState);
            break;
        case Ne:
            Constraint c = inState.eval(right);
            if (c != null && c.bound() == left.value) {
                this.feasible = false;
                this.add(right, Constraint.BOT());
                break;
            }
        case Invalid:
        default:
            // We cannot represent the condition, do nothing.
        }
    }

    public void updateCond(DifferenceBoundedState inState,
                           Local left,
                           Local right,
                           PredicateType type) {
        switch (type) {
        case Le:
            this.transferConditional(left,
                                     right,
                                     new Constraint(0, PredicateType.Le),
                                     inState);
            break;
        case Lt:
            this.transferConditional(left,
                                     right,
                                     new Constraint(-1, PredicateType.Le),
                                     inState);
            break;
        case Eq:
            this.transferConditional(left,
                                     right,
                                     new Constraint(0),
                                     inState);
            break;
        case Ge:
            this.transferConditional(right,
                                     left,
                                     new Constraint(0, PredicateType.Le),
                                     inState);
            break;
        case Gt:
            this.transferConditional(right,
                                     left,
                                     new Constraint(-1, PredicateType.Le),
                                     inState);
            break;
        case Ne:
            Constraint c1 = inState.eval(left);
            Constraint c2 = inState.eval(right);
            if (c1 != null && c2 != null && c1.bound() == c2.bound()) {
                this.feasible = false;
                this.add(left, Constraint.BOT());
                this.add(right, Constraint.BOT());
                break;
            }
        case Invalid:
        default:
            // There's nothing we can do here...
        }
    }
}
