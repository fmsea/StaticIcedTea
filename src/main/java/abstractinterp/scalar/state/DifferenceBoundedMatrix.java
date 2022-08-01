package abstractinterp.scalar.state;

import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.Local;
import soot.Value;
import soot.grimp.Grimp;
import soot.jimple.BinopExpr;
import soot.jimple.IntConstant;
import soot.jimple.internal.JNegExpr;

import abstractinterp.scalar.state.util.GraphProjection;
import solver.SolverWrapper;
import util.Configuration;

public class DifferenceBoundedMatrix {

    private static Logger LOGGER = LoggerFactory.getLogger(DifferenceBoundedMatrix.class);
    private final int N;
    private ZoneConstraint[][] matrix;
    private Set<Local> locals;
    protected Set<Local> constants;
    private Map<Local, Integer> localToIndices;
    private Map<Integer, Local> indicesToLocals;
    private boolean isClosed = false;

    public DifferenceBoundedMatrix(Set<Local> locals, boolean top) {
        this.N = locals.size();
        this.locals = new HashSet<>(N * 2);
        this.constants = new HashSet<>();
        this.locals.addAll(locals);
        this.localToIndices = new HashMap<>(N * 2 + 1, 0.7f);
        this.indicesToLocals = new HashMap<>(N * 2 + 1, 0.7f);
        this.matrix = new ZoneConstraint[N][N];
        {
            int i = 0;
            Collection<Local> ls = locals.stream()
                .sorted((a, b) -> a.toString().compareTo(b.toString()))
                .collect(Collectors.toList());
            for (Local l : ls) {
                this.localToIndices.put(l, i);
                this.indicesToLocals.put(i, l);
                i++;
            }
        }
        iterateMatrix((i, j) -> {
                if (top && i == j) {
                    this.matrix[i][j] = ZoneConstraint.of(0);
                } else {
                    this.matrix[i][j] = top ? ZoneConstraint.TOP() : ZoneConstraint.BOT();
                }
            });
    }

    public DifferenceBoundedMatrix(DifferenceBoundedMatrix copy) {
        this(copy.locals, false);
        this.isClosed = copy.isClosed;
        this.constants.addAll(copy.constants);
        iterateMatrix((i, j) -> {
                this.matrix[i][j] = copy.matrix[i][j].copy();
            });
    }

    public void copyTo(DifferenceBoundedMatrix destination) {
        iterateMatrix((i, j) -> {
                destination.matrix[i][j] = this.matrix[i][j].copy();
            });
        destination.constants.addAll(this.constants);
        destination.isClosed = this.isClosed;
    }

    public Set<Local> getLocals() {
        return this.locals;
    }

    public Set<Local> getConstants() {
        return Collections.unmodifiableSet(this.constants);
    }

    public void checkConstants() {
        Set<Local> constants = Collections.unmodifiableSet(this.constants);
        for (Local constant : constants) {
            if (!isConstant(constant, Variable.ZERO)) {
                this.constants.remove(constant);
            }
        }
    }

    public void setConstraint(Local source, Local target, ZoneConstraint constraint) {
        int i = this.localToIndices.get(source);
        int j = this.localToIndices.get(target);
        this.setConstraint(i, j, constraint);
    }

    private void setConstraint(int i, int j, ZoneConstraint c) {
        Local source = this.indicesToLocals.get(i);
        Local target = this.indicesToLocals.get(j);
        this.isClosed = false;
        if (i == j && c.bound().map(b -> b < 0).orElse(false)) {
            this.matrix[i][j] = ZoneConstraint.BOT();
        } else if (i == j && (!c.isBottom() || c.bound().map(b -> b > 0).orElse(false))) {
            this.matrix[i][j] = ZoneConstraint.of(0);
        } else {
            this.matrix[i][j] = c;
            if (this.isConstant(i, j)) {
                if (source.equals(Variable.ZERO)) {
                    this.constants.add(target);
                    LOGGER.debug("Established constant [source={}, target={}]", source, target);
                } else if (target.equals(Variable.ZERO)) {
                    this.constants.add(source);
                    LOGGER.debug("Established constant [source={}, target={}]", source, target);
                }
            } else {
                if (source.equals(Variable.ZERO)) {
                    this.constants.remove(target);
                    LOGGER.debug("Removed constant [source={}, target={}]", source, target);
                } else if (target.equals(Variable.ZERO)) {
                    this.constants.remove(source);
                    LOGGER.debug("Removed constant [source={}, target={}]", source, target);
                }
            }
        }
    }

    public boolean putConstraint(Local source, Local target, ZoneConstraint constraint) {
        return this.putConstraint(source, target, constraint, this);
    }

    public boolean putConstraint(Local source,
                                     Local target,
                                     ZoneConstraint constraint,
                                     DifferenceBoundedMatrix matrix) {
        int i = this.localToIndices.get(source);
        int j = this.localToIndices.get(target);
        return this.putConstraint(i, j, constraint, matrix);
    }

    private boolean putConstraint(int i, int j, ZoneConstraint c) {
        return this.putConstraint(i, j, c, this);
    }

    private boolean putConstraint(int i, int j, ZoneConstraint c, DifferenceBoundedMatrix in) {
        boolean added = false;
        LOGGER.trace("Compare to existing constraint: {} ≤ {}", c, in.matrix[i][j]);
        if (ZoneConstraint.compare(c, in.matrix[i][j]) < 0) {
            this.setConstraint(i, j, c);
            added = true;
        }
        return added;
    }

    public boolean putIncremental(Local source, Local target, ZoneConstraint constraint) {
        return this.putIncremental(source, target, constraint, this);
    }

    public boolean putIncremental(Local source,
                                  Local target,
                                  ZoneConstraint constraint,
                                  DifferenceBoundedMatrix in) {
        boolean feasible = false;
        boolean edgeAdded = putConstraint(source, target, constraint, in);
        if (!edgeAdded) {
            feasible = this.isFeasible();
        } else {
            feasible = this.incrementalClosure(source, target);
        }
        return feasible;
    }

    public ZoneConstraint getConstraint(Local source, Local target) {
        int i = this.localToIndices.get(source);
        int j = this.localToIndices.get(target);
        return this.matrix[i][j];
    }

    /** Compute the least upper bound between two matrices
     */
    public void union(DifferenceBoundedMatrix other) {
        assert this.locals.size() == other.locals.size();
        LOGGER.debug("computing least upper bound");
        LOGGER.trace("{} ⊔ {}", this, other);
        this.iterateMatrix((i, j) -> {
                this.setConstraint(i, j, ZoneConstraint.max(this.matrix[i][j], other.matrix[i][j]));
            });
        LOGGER.debug("finished least upper bound");
        LOGGER.trace("⊔ result: {}", this);
    }

    /** Compute "intersection" between two matrices
     **/
    public void intersection(DifferenceBoundedMatrix other) {
        assert this.locals.size() == other.locals.size();
        LOGGER.debug("computing intersection");
        LOGGER.trace("{} ⊓ {}", this, other);
        this.iterateMatrix((i, j) -> {
                this.setConstraint(i, j, ZoneConstraint.min(this.matrix[i][j], other.matrix[i][j]));
            });
        LOGGER.debug("finished computing intersection");
        LOGGER.trace("⊓ result:", this);
    }

    public static DifferenceBoundedMatrix intersect(DifferenceBoundedMatrix m1,
                                                    DifferenceBoundedMatrix m2) {
        m1.intersection(m2);
        return m1;
    }

    /** Compute if matrix represents feasible bounding region
     *
     * @return region represents feasible region
     */
    public boolean isFeasible() {
        boolean feasible = true;
        for (int i = 0; i < N; i++) {
            if (this.matrix[i][i].bound().map(b -> b < 0).orElse(false)) {
                feasible = false;
                break;
            }
        }
        feasible = feasible && !this.anyBottoms();
        return feasible;
    }

    /** compute preorder/subset relation between two matrices
     *
     */
    public boolean isSubset(DifferenceBoundedMatrix other) {
        return reduceMatrixToBool((i, j) -> {
                return ZoneConstraint.compare(this.matrix[i][j],
                                              other.matrix[i][j]) <= 0;
            });
    }

    public void closeConstants(Set<Local> sources) {
        LOGGER.debug("These are no longer constants: {}", sources);
        DifferenceBoundedMatrix newMatrix = constants.stream().flatMap(c -> {
                return sources.stream().map(s -> {
                        DifferenceBoundedMatrix m = new DifferenceBoundedMatrix(this);
                        m.computeProjectedClosure(s, c);
                        return m;
                    });
            }).reduce(DifferenceBoundedMatrix::intersect).get();
        this.intersection(newMatrix);
    }

    private boolean incrementalClosure(Local source, Local target) {
        int si = this.localToIndices.get(source);
        int ti = this.localToIndices.get(target);
        Set<Local> worklist = new HashSet<>();
        // seed worklist with a few more necessary targets
        worklist.add(target);
        // foreach child of target (ti), put a constraint
        // we do not skip self references to ensure we capture a negative cycle
        for (int i = 0; i < N; i++) {
            if (this.putConstraint(si, i, ZoneConstraint.add(this.matrix[si][ti],
                                                                 this.matrix[ti][i]))) {
                worklist.add(this.indicesToLocals.get(i));
            }
        }

        for (Local l : worklist) {
            int c = this.localToIndices.get(l);
            for (int i = 0; i < N; i++) {
                this.putConstraint(i, c, ZoneConstraint.add(this.matrix[i][si],
                                                                this.matrix[si][c]));
            }
        }

        this.isClosed = true;
        return this.isFeasible();
    }

    /** Compute transitive closure of matrix
     *
     * Closure is computed using the Floyd-Warshall Algorithm
     *
     * @return boolean does the matrix represent a feasible region
     */
    public boolean computeClosure() {
        // skip closure if matrix contains bottoms
        if (this.anyBottoms()) {
            return false;
        }

        if (!this.isClosed) {
            // ensure diagonal is zeroed.
            for (int i = 0; i < N; i++) {
                this.matrix[i][i] = ZoneConstraint.of(0);
            }

            for (int k = 0; k < N; k++) {
                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < N; j++) {
                        ZoneConstraint candidate = ZoneConstraint.add(this.matrix[i][k],
                                                                      this.matrix[k][j]);
                        ZoneConstraint newZoneConstraint = ZoneConstraint.min(this.matrix[i][j],
                                                                              candidate);
                        this.setConstraint(i, j, newZoneConstraint);
                    }
                }
            }
        }

        boolean feasible = true;
        for (int i = 0; i < N; i++) {
            if (this.matrix[i][i].isBottom() || this.matrix[i][i].bound().orElse(0) < 0) {
                feasible = false;
                break;
            }
        }
        this.isClosed = true;
        return feasible;
    }

    public boolean computeProjectedClosure(Local source, Local target) {
        if (this.anyBottoms()) {
            return false;
        }

        int s = this.localToIndices.get(source);
        int t = this.localToIndices.get(target);
        int k = this.localToIndices.get(Variable.ZERO); // should be 0

        this.putIncremental(source,
                            target,
                            ZoneConstraint.min(this.matrix[s][t],
                                               ZoneConstraint.add(this.matrix[s][k],
                                                                  this.matrix[k][t])));
        this.putIncremental(target,
                            source,
                            ZoneConstraint.min(this.matrix[t][s],
                                               ZoneConstraint.add(this.matrix[t][k],
                                                                  this.matrix[k][s])));

        boolean feasible = true;
        for (int i = 0; i < N; i++) {
            if (this.matrix[i][i].isBottom() || this.matrix[i][i].bound().orElse(0) < 0) {
                feasible = false;
                break;
            }
        }
        return feasible;
    }

    /** Compute the transitive closure reduction of the matrix
     *
     * This looks a lot like Floyd-Warshall in reverse, except we only consider
     * paths of length 2.
     *
     * If the current matrix does not represent a feasible half-space, skip,
     * the reduction is not computed.
     *
     * https://doi.org/10.1109/REAL.1997.641265
     */
    public boolean computeReducedClosure() {
        if (!this.computeClosure()) {
            return false;
        }

        // we only consider paths of length 2
        for (int k = 0; k < N; k++) {
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (i == j || i == k || j == k) {
                        continue;
                    }
                    ZoneConstraint transitivePath = ZoneConstraint.add(this.matrix[i][k],
                                                                       this.matrix[k][j]);
                    if (ZoneConstraint.compare(this.matrix[i][j], transitivePath) >= 0) {
                        this.setConstraint(i, j, ZoneConstraint.TOP());
                    }
                }
            }
        }
        return true;
    }

    /** Compute transitive closure W0Z reduced
     *
     * Compute the full closure of the matrix, then remove any direct edges if
     * the transitive edge of length two which passes through z0 is equal or
     * greater than the direct edge.  Direct edges which are less than the
     * transitive edge are more restrictive and therefore should be removed.
     */
    public boolean w0zReduction() {
        if (!this.computeClosure()) {
            return false;
        }
        for (int i = 1; i < N; i++) {
            for (int j = 1; j < N; j++) {
                if (i == j) {
                    continue;
                }
                ZoneConstraint transitivePath = ZoneConstraint.add(this.matrix[i][0],
                                                                   this.matrix[0][j]);
                if (ZoneConstraint.compare(this.matrix[i][j], transitivePath) >= 0) {
                    this.setConstraint(i, j, ZoneConstraint.TOP());
                }
            }
        }
        return true;
    }

    public static DifferenceBoundedMatrix widen(DifferenceBoundedMatrix m,
                                                DifferenceBoundedMatrix n) {
        DifferenceBoundedMatrix res = new DifferenceBoundedMatrix(m);
        res.widenWith(n);
        return res;
    }

    public void widenWith(DifferenceBoundedMatrix n) {
        iterateMatrix((i, j) -> {
                ZoneConstraint c1 = this.matrix[i][j];
                ZoneConstraint c2 = n.matrix[i][j];
                if (!(c1.isBottom() || c2.isBottom()) && c2.compareTo(c1) == 1) {
                    this.matrix[i][j] = ZoneConstraint.TOP();
                }
            });
    }

    public void forgetConstraintsSimple(Local local) {
        int k = this.localToIndices.get(local);
        for (int i = 0; i < N; i++) {
            if (i == k) {
                continue;
            } else {
                this.setConstraint(k, i, ZoneConstraint.TOP());
                this.setConstraint(i, k, ZoneConstraint.TOP());
            }
        }
    }

    public void forgetConstraints(Local local) {
        if (!this.isClosed) {
            int k = this.localToIndices.get(local);
            iterateMatrix((i, j) -> {
                    if (i == j && j == k) {
                        this.setConstraint(i, j, ZoneConstraint.of(0));
                    } else if (i != k && j != k) {
                        ZoneConstraint c = ZoneConstraint.min(this.matrix[i][j],
                                                              ZoneConstraint.add(this.matrix[i][k],
                                                                                 this.matrix[k][j]));
                        this.setConstraint(i, j, c);
                    }
                });
        }
        this.forgetConstraintsSimple(local);
    }

    public void makeInfeasible() {
        iterateMatrix((i, j) -> this.matrix[i][j] = ZoneConstraint.BOT());
    }

    public Interval32Box projectToInterval(Local source) {
        return project(source, Variable.ZERO);
    }

    public Interval32Box project(Local source, Local target) {
        int i = this.localToIndices.get(source);
        int j = this.localToIndices.get(target);
        Optional<Integer> lower = this.matrix[j][i].bound().map(b -> b * -1);
        Optional<Integer> upper = this.matrix[i][j].bound();
        return Interval32Box.of(lower, upper);
    }

    public void addIncoming(Local target, ZoneConstraint add) {
        this.addIncoming(target, add, this);
    }

    public void addOutgoing(Local source, ZoneConstraint add) {
        this.addOutgoing(source, add, this);
    }

    public void subIncoming(Local target, ZoneConstraint sub) {
        this.subIncoming(target, sub, this);
    }

    public void subOutgoing(Local source, ZoneConstraint sub) {
        this.subOutgoing(source, sub, this);
    }

    public void addIncoming(Local target, ZoneConstraint add, DifferenceBoundedMatrix from) {
        int k = this.localToIndices.get(target);
        for (int i = 0; i < N; i++) {
            if (i == k) {
                continue;
            }
            this.setConstraint(i, k, ZoneConstraint.add(from.matrix[i][k], add));
        }
    }

    public void addOutgoing(Local source, ZoneConstraint add, DifferenceBoundedMatrix from) {
        int k = this.localToIndices.get(source);
        for (int i = 0; i < N; i++) {
            if (i == k) {
                continue;
            }
            this.setConstraint(k, i, ZoneConstraint.add(from.matrix[k][i], add));
        }
    }

    public void subIncoming(Local target, ZoneConstraint sub, DifferenceBoundedMatrix from) {
        int k = this.localToIndices.get(target);
        for (int i = 0; i < N; i++) {
            if (i == k) {
                continue;
            }
            this.setConstraint(i, k, ZoneConstraint.subtract(from.matrix[i][k], sub));
        }
    }

    public void subOutgoing(Local source, ZoneConstraint sub, DifferenceBoundedMatrix from) {
        int k = this.localToIndices.get(source);
        for (int i = 0; i < N; i++) {
            if (i == k) {
                continue;
            }
            this.setConstraint(k, i, ZoneConstraint.subtract(from.matrix[k][i], sub));
        }
    }

    @Override
    public boolean equals(Object o) {
        boolean equal = false;
        if (o != null && o instanceof DifferenceBoundedMatrix) {
            equal = this.equals((DifferenceBoundedMatrix) o);
        }
        return equal;
    }

    public boolean equals(DifferenceBoundedMatrix other) {
        boolean equal = true;
        if (this.locals.size() != other.locals.size()) {
            equal = false;
        } else {
            equal = reduceMatrixToBool((i, j) -> this.matrix[i][j].equals(other.matrix[i][j]));
        }
        return equal;
    }

    private boolean isConstant(Local source, Local target) {
        int i = this.localToIndices.get(source);
        int j = this.localToIndices.get(target);
        return this.isConstant(i, j);
    }

    private boolean isConstant(int i, int j) {
        return ((i == 0 || j == 0) &&
                this.matrix[i][j].bound()
                .flatMap(a -> this.matrix[j][i].bound().map(b -> Math.abs(a) == Math.abs(b)))
                .orElse(false));
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + N;
        result = prime * result + this.matrix.hashCode();
        result = prime * result + this.localToIndices.hashCode();
        result = prime * result + this.indicesToLocals.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append(Arrays.deepToString(this.matrix).replace("], ", "],\n "));
        return sb.toString();
    }

    public Optional<BinopExpr> toBinop() {
        Configuration.getBoolean("OutputMinimizedZoneStates").ifPresentOrElse((b) -> {
                if (b) {
                    this.computeReducedClosure();
                }
            }, this::computeReducedClosure);
        Grimp g = Grimp.v();
        if (this.isFeasible()) {
            List<BinopExpr> exprs = new ArrayList<>(N * 2);
            for (int i = 0; i < N; i++) {
                for (int j = i; j < N; j++) {
                    Local s = this.indicesToLocals.get(i);
                    Local t = this.indicesToLocals.get(j);
                    if (i == j ||
                        (s.equals(Variable.ZERO) && t.equals(Variable.ZERO)) ||
                        (this.matrix[i][j].isTop() && this.matrix[j][i].isTop())) {
                        continue;
                    } else if (!this.matrix[i][j].isTop() &&
                               !this.matrix[j][i].isTop() &&
                               this.matrix[i][j].bound().equals(this.matrix[j][i].bound().map(b -> b * -1))) {
                        if (s.equals(Variable.ZERO)) {
                            exprs.add(g.newEqExpr(t, IntConstant.v(this.matrix[i][j].bound().map(b -> b * -1).get())));
                        } else if (t.equals(Variable.ZERO)) {
                            exprs.add(g.newEqExpr(s, IntConstant.v(this.matrix[i][j].bound().get())));
                        } else {
                            exprs.add(g.newEqExpr(s, g.newAddExpr(t, IntConstant.v((this.matrix[i][j].bound().get())))));
                        }
                    } else {
                        if (!this.matrix[i][j].isTop()) {
                            exprs.add(this.toGrimpExpr(s, t, this.matrix[i][j]));
                        }
                        if (!this.matrix[j][i].isTop()) {
                            exprs.add(this.toGrimpExpr(t, s, this.matrix[j][i]));
                        }
                    }
                }
            }
            return exprs.stream().reduce((a, b) -> g.newAndExpr(a, b));
        } else {
            // False
            return Optional.of(g.newEqExpr(IntConstant.v(0), IntConstant.v(1)));
        }
    }

    public String toSMT(SolverWrapper solver) {
        this.computeReducedClosure();
        StringBuilder sb = new StringBuilder();
        Grimp g = Grimp.v();
        if (this.isFeasible()) {
            this.toBinop().ifPresentOrElse((expr) -> {
                    sb.append(solver.smt2(expr));
                    sb.append("\n");
                }, () -> sb.append("true\n"));
        } else {
            sb.append("false\n");
        }
        return sb.toString();
    }

    public String toSMT(Local source, SolverWrapper solver) {
        if (!this.computeReducedClosure()) {
            return "false";
        } else {
            return this.toBinop(this.getConnectedVariablesOf(source)).stream()
                .sorted((a, b) -> a.toString().compareTo(b.toString()))
                .reduce((a, b) -> Grimp.v().newAndExpr(a, b))
                .map(expr -> solver.smt2(expr))
                .orElse("true");
        }
    }

    private Set<BinopExpr> toBinop(Set<Local> sources) {
        Set<BinopExpr> exprs = Set.of();
        for (Local source : sources) {
            exprs = this.toBinop(source, exprs);
        }
        return exprs;
    }

    private Set<BinopExpr> toBinop(Local source) {
        return this.toBinop(source, Set.of());
    }

    private Set<BinopExpr> toBinop(Local source, Set<BinopExpr> acc) {
        Set<BinopExpr> exprs = new HashSet<>();
        exprs.addAll(acc);
        Predicate isMember = expr -> exprs.stream()
            .map(e -> e.toString())
            .collect(Collectors.toSet())
            .contains(expr.toString());
        int k = this.localToIndices.get(source);
        for (int i = 0; i < N; i++) {
            if (i == k) {
                continue;
            }
            Local t = this.indicesToLocals.get(i);
            if (!this.matrix[i][k].isTop()) {
                BinopExpr expr = this.toGrimpExpr(t, source, this.matrix[i][k]);
                if (!isMember.test(expr)) {
                    exprs.add(expr);
                }
            }

            if (!this.matrix[k][i].isTop()) {
                BinopExpr expr = this.toGrimpExpr(source, t, this.matrix[k][i]);
                if (!isMember.test(expr)) {
                    exprs.add(expr);
                }
            }
        }
        return exprs;
    }

    private Optional<BinopExpr> toBinop(Local source, Local target) {
        int i = this.localToIndices.get(source);
        int j = this.localToIndices.get(target);
        if (this.matrix[i][j].isTop()) {
            return Optional.empty();
        } else {
            return Optional.of(this.toGrimpExpr(source, target, this.matrix[i][j]));
        }
    }

    public String toSMT(Local source, Local target, SolverWrapper solver) {
        int i = this.localToIndices.get(source);
        int j = this.localToIndices.get(target);
        return solver.smt2(this.toGrimpExpr(source, target, this.matrix[i][j]));
    }

    public GraphProjection toGraph() {
        GraphProjection graph = new GraphProjection(this.locals);
        if (this.isFeasible()) {
            iterateMatrix((i, j) -> {
                    Local s = this.indicesToLocals.get(i);
                    Local t = this.indicesToLocals.get(j);
                    if (this.matrix[i][j].isTop() || i == j) {
                    } else {
                        graph.setConstraint(s, t, this.matrix[i][j].copy());
                    }
                });
        } else {
            iterateMatrix((i, j) -> {
                    if (i == j) {
                        Local s = this.indicesToLocals.get(i);
                        graph.setConstraint(s, s, ZoneConstraint.BOT());
                    }
                });
        }
        return graph;
    }

    public static DifferenceBoundedGraph to(DifferenceBoundedMatrix m) {
        DifferenceBoundedGraph g;
        if (m.isFeasible()) {
            g = new DifferenceBoundedGraph(m.locals, true);
            m.iterateMatrix((i, j) -> {
                    Local s = m.indicesToLocals.get(i);
                    Local t = m.indicesToLocals.get(j);
                    if (m.matrix[i][j].isTop() || i == j) {
                    } else {
                        g.setConstraint(s, t, m.matrix[i][j].copy());
                    }
                });
        } else {
            g = new DifferenceBoundedGraph(m.locals, false);
        }
        return g;
    }

    public static DifferenceBoundedMatrix from(DifferenceBoundedGraph graph) {
        return DifferenceBoundedGraph.to(graph);
    }

    public Set<Local> getConnectedVariablesOf(Local id) {
        if (id.equals(Variable.ZERO)) {
            return Set.of();
        } else if (!this.w0zReduction()) {
            return Set.of(id);
        } else {
            Set<Local> connected = new HashSet<>();
            Deque<Local> toVisit = new ArrayDeque<>();
            connected.add(id);
            toVisit.push(id);
            while (toVisit.peek() != null) {
                Local current = toVisit.pop();
                Stream<Local> preds = this.predecessorsOf(current);
                Stream<Local> succs = this.successorsOf(current);
                Stream.concat(preds, succs)
                    .forEach(c -> {
                            if (!connected.contains(c)) {
                                connected.add(c);
                                toVisit.push(c);
                            }
                        });
            }
            return Set.copyOf(connected);
        }
    }

    private Stream<Local> predecessorsOf(Local t) {
        int j = this.localToIndices.get(t);
        Stream<Optional<Local>> preds = IntStream.range(1, N).mapToObj(i -> {
                if (this.matrix[i][j].isTop()) {
                    return Optional.empty();
                } else {
                    return Optional.of(this.indicesToLocals.get(i));
                }
            });
        return preds.filter(o -> o.isPresent())
            .map(o -> o.get());
    }

    private Stream<Local> successorsOf(Local s) {
        int i = this.localToIndices.get(s);
        Stream<Optional<Local>> succs = IntStream.range(1, N).mapToObj(j -> {
                if (this.matrix[i][j].isTop()) {
                    return Optional.empty();
                } else {
                    return Optional.of(this.indicesToLocals.get(j));
                }
            });
        return succs.filter(o -> o.isPresent())
            .map(o -> o.get());
    }

    private BinopExpr toGrimpExpr(Local s, Local t, ZoneConstraint c) {
        BinopExpr r = null;
        Grimp g = Grimp.v();
        Function<Local, BinopExpr> top = l -> {
            return g.newOrExpr(g.newLeExpr(l, IntConstant.v(0)),
                               g.newGtExpr(l, IntConstant.v(0)));
        };
        if (c.isBottom()) {
            if (s.equals(t)) {
                r = g.newLeExpr(s, g.newAddExpr(t, IntConstant.v(1)));
            } else if (s.equals(Variable.ZERO) && !t.equals(Variable.ZERO)) {
                r = g.newAndExpr(g.newLeExpr(t, IntConstant.v(0)),
                                 g.newGtExpr(t, IntConstant.v(0)));
            } else if (t.equals(Variable.ZERO) && !s.equals(Variable.ZERO)) {
                r = g.newAndExpr(g.newLeExpr(s, IntConstant.v(0)),
                                 g.newGtExpr(s, IntConstant.v(0)));
            } else {
                r = g.newAndExpr(g.newLeExpr(s, g.newAddExpr(t, IntConstant.v(0))),
                                 g.newGtExpr(s, g.newAddExpr(t, IntConstant.v(0))));
            }
        } else if (c.isTop()) {
            if (s.equals(Variable.ZERO) && !t.equals(Variable.ZERO)) {
                r = top.apply(t);
            } else if (t.equals(Variable.ZERO) && !s.equals(Variable.ZERO)) {
                r = top.apply(s);
            } else {
                r = g.newOrExpr(g.newLeExpr(s, g.newAddExpr(t, IntConstant.v(0))),
                                g.newGtExpr(s, g.newAddExpr(t, IntConstant.v(0))));
            }
        } else if (!s.equals(Variable.ZERO) && t.equals(Variable.ZERO)) {
            r = g.newLeExpr(s, IntConstant.v(c.bound().get()));
        } else if (!t.equals(Variable.ZERO) && s.equals(Variable.ZERO)) {
            r = g.newGeExpr(t, IntConstant.v(-1 * c.bound().get()));
        } else {
            IntConstant k = IntConstant.v(c.bound().get());
            r = g.newLeExpr(s, g.newAddExpr(t, k));
        }

        return r;
    }

    private boolean anyBottoms() {
        return reduceMatrix((i, j) -> this.matrix[i][j].equals(ZoneConstraint.BOT()),
                            (a, b) -> a || b,
                            false);
    }

    private <R> R reduceMatrix(BiFunction<Integer, Integer, R> fmap,
                               BinaryOperator<R> acc,
                               R initial) {
        R ret = initial;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                ret = acc.apply(ret, fmap.apply(i, j));
            }
        }
        return ret;
    }

    private void iterateMatrix(BiConsumer<Integer, Integer> op) {
        reduceMatrixToBool((i, j) -> {
                op.accept(i, j);
                return true;
            });
    }

    private boolean reduceMatrixToBool(BiFunction<Integer, Integer, Boolean> reducer) {
        return reduceMatrix(reducer, (a, b) -> a && b, true);
    }
}
