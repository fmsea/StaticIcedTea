package dev.fmsea.absint.scalar.state;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.fmsea.absint.ConstraintType;
import dev.fmsea.absint.scalar.state.util.GraphProjection;
import dev.fmsea.util.Pair;
import dev.fmsea.util.Properties;
import dev.fmsea.util.Sets;

public class OctagonDifferenceBoundedMatrix {

    protected static Logger LOGGER = LoggerFactory.getLogger(OctagonDifferenceBoundedMatrix.class);
    protected final int N;
    protected Constraint[][] matrix;
    protected boolean isClosed = false;
    public OctagonDifferenceBoundedMatrix(int N, boolean top) {
        this.N = N;
        this.matrix = new Constraint[N][N];

        if (top) {
            iterateMatrix((i, j) -> {
                    this.matrix[i][j] = i == j ? Constraint.of(0) : Constraint.TOP();
                });
        } else {
            iterateMatrix((i, j) -> {
                    this.matrix[i][j] = Constraint.BOT();
                });
        }
    }

    public OctagonDifferenceBoundedMatrix(OctagonDifferenceBoundedMatrix copy) {
        this(copy.N, false);
        this.isClosed = copy.isClosed;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                this.matrix[i][j] = copy.matrix[i][j].copy();
            }
        }
    }

    public int size() {
        return this.N;
    }

    public void copyTo(OctagonDifferenceBoundedMatrix destination) {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                destination.matrix[i][j] = this.matrix[i][j].copy();
            }
        }
        // destination.constants.addAll(this.constants);
        destination.isClosed = this.isClosed;
    }

    public void checkConstants() {
        iterateMatrix(this::checkAndUpdateConstants);
    }

    public void checkAndUpdateConstants(int i, int j) {
        if ((i ^ j) == 1) {
            Interval32Box interval = this.projectToInterval(i, j);
            if (interval.isSingleton()) {
                // this.constants.add(this.indicesToLocals.get(i));
                // this.constants.add(this.indicesToLocals.get(j));
            } else {
                // this.constants.remove(this.indicesToLocals.get(i));
                // this.constants.remove(this.indicesToLocals.get(j));
            }
        }
    }

    public void setConstraint(int i, int j, Constraint c) {
        this.isClosed = false;
        if (i == j && c.bound().map(b -> b < 0).orElse(false)) {
            LOGGER.trace("Setting bottom i = {}, c = {}", i, c);
            this.matrix[i][j] = Constraint.BOT();
        } else if (i == j && (!c.isBottom() || c.bound().map(b -> b > 0).orElse(false))) {
            this.matrix[i][j] = Constraint.of(0);
        } else {
            this.matrix[i][j] = c;
            checkAndUpdateConstants(i, j);
        }
    }

    public boolean putConstraint(int i, int j, Constraint constraint) {
        return this.putConstraint(i, j, constraint, this);
    }

    public boolean putConstraint(int i, int j, Constraint c, OctagonDifferenceBoundedMatrix in) {
        boolean added = false;
        LOGGER.trace("Compare to existing constraint: {} ≤ {}", c, in.matrix[i][j]);
        if (Constraint.compare(c, in.matrix[i][j]) < 0) {
            this.setConstraint(i, j, c);
            added = true;
        }
        return added;
    }

    public boolean putIncremental(int i, int j, Constraint constraint) {
        return this.putIncremental(i, j, constraint, this);
    }

    public boolean putIncremental(int i, int j, Constraint constraint, OctagonDifferenceBoundedMatrix in) {
        switch (Properties.IncrementalClosureAlgorithm) {
            case CHAWDHARY:
                if (putConstraint(i, j, constraint, in)) {
                    return this.incrementalZClosure(i, j, constraint);
                } else {
                    return this.isFeasible();
                }
            case SEARCH:
            default:
                return this.incrementalClosure(i, j, constraint);
        }
    }

    public Constraint getConstraint(int i, int j) {
        return this.matrix[i][j];
    }

    public void union(OctagonDifferenceBoundedMatrix other) {
        assert this.N == other.N;
        LOGGER.debug("computing least upper bound:");
        LOGGER.trace("{} ⊔ {}", this, other);
        this.iterateMatrix((i, j) -> {
                this.setConstraint(i, j, Constraint.max(this.matrix[i][j], other.matrix[i][j]));
            });
        LOGGER.debug("finished least upper bound");
        LOGGER.trace("⊔ result: {}", this);
    }

    public void intersection(OctagonDifferenceBoundedMatrix other) {
        assert this.N == other.N;
        LOGGER.debug("computing greatest lower bound");
        LOGGER.trace("{} ⊓ {}", this, other);
        this.iterateMatrix((i, j) -> {
                this.setConstraint(i, j, Constraint.min(this.matrix[i][j], other.matrix[i][j]));
            });
        LOGGER.debug("finished computing greatest lower bound");
        LOGGER.trace("⊓ result: {}", this);
    }

    public boolean isTop() {
        return reduceMatrixToBool((i, j) -> {
                if (i == j) {
                    return this.matrix[i][j].equals(Constraint.of(0));
                } else {
                    return this.matrix[i][j].isTop();
                }
            });
    }

    public boolean isFeasible() {
        boolean feasible = true;
        // IntStream.range(0, N)
        //     .boxed()
        //     .map(i -> this.matrix[i][i])
        for (int i = 0; i < N; i++) {
            if (this.matrix[i][i].bound().map(b -> b < 0).orElse(false)) {
                feasible = false;
                break;
            }
        }
        feasible = feasible && !this.anyBottoms();
        return feasible;
    }

    public boolean isSubset(OctagonDifferenceBoundedMatrix other) {
        return this.reduceMatrixToBool((i, j) -> {
                return Constraint.compare(this.matrix[i][j],
                                          other.matrix[i][j]) <= 0;
            });
    }

    // public void closeConstants(Set<> sources) {
    //     throw new UnsupportedOperationException();
    // }

    /** return whether we should close/update this edge
     *
     */
    protected boolean closureShouldUpdate(int i, int j, Constraint shortPath, Constraint longPath) {
        return Constraint.compare(shortPath, longPath) > 0;
    }

    public boolean incrementalClosure(int si, int ti, Constraint d) {
        return this.incrementalClosure(List.of(ConstraintUpdateThunk.of(si, ti, d)));
    }

    public boolean incrementalClosure(Collection<ConstraintUpdateThunk> thunks) {
        return this.incrementalClosure(thunks, this);
    }

    public boolean incrementalClosure(Collection<ConstraintUpdateThunk> thunks, OctagonDifferenceBoundedMatrix in) {
        Set<Integer> worklist = new HashSet<>();
        thunks.stream().forEach(thunk -> {
                boolean added = this.putConstraint(thunk.s, thunk.t, thunk.c, in);
                if (added) {
                    worklist.add(thunk.t);
                }
            });

        if (worklist.isEmpty()) {
            return this.isFeasible();
        }

        for (int i = 0; i < N; i++) {
            for (ConstraintUpdateThunk thunk : thunks) {
                Constraint sum = Constraint.add(this.matrix[thunk.s][thunk.t], this.matrix[thunk.t][i]);
                if (this.putConstraint(thunk.s, i, sum, this)) {
                    worklist.add(i);
                }
            }
        }

        for (int i = 0; i < N; i++) {
            for (Integer c : worklist) {
                for (ConstraintUpdateThunk thunk : thunks) {
                    Constraint longPath = Constraint.add(this.matrix[i][thunk.s], this.matrix[thunk.s][c]);
                    if (closureShouldUpdate(i, c, this.matrix[i][c], longPath)) {
                        this.setConstraint(i, c, longPath);
                    }
                }
            }
        }

        this.isClosed = true;
        return this.tighten() && this.isZConsistent() && this.computeStrongClosure();
    }

    public boolean deferredIncrementalClosure(int si, int ti, Constraint ci) {
        return this.deferredIncrementalClosure(si, ti, ci, this);
    }

    public boolean deferredIncrementalClosure(int si, int ti, Constraint ci, OctagonDifferenceBoundedMatrix in) {
        return this.deferredIncrementalClosure(List.of(ConstraintUpdateThunk.of(si, ti, ci)), in);
    }

    public boolean deferredIncrementalClosure(Collection<ConstraintUpdateThunk> thunks) {
        return this.deferredIncrementalClosure(thunks, this);
    }

    public boolean deferredIncrementalClosure(Collection<ConstraintUpdateThunk> thunks, OctagonDifferenceBoundedMatrix in) {
        Map<Integer, Set<Integer>> parents  = new HashMap<>();
        Map<Integer, Set<Integer>> children = new HashMap<>();
        thunks.stream().forEach(thunk -> {
            boolean added = this.putConstraint(thunk.s, thunk.t, thunk.c, in);
            if (added) {
                for (int i = 0; i < N; i++) {
                    if (in.matrix[i][thunk.s].compareTo(Constraint.TOP()) < 0) {
                        parents.put(thunk.s,
                            Stream.concat(parents.getOrDefault(thunk.s, Set.of()).stream(),
                            Set.of(i).stream()).collect(Collectors.toSet()));
                    }
                    if (in.matrix[thunk.t][i].compareTo(Constraint.TOP()) < 0) {
                        children.put(thunk.t,
                            Stream.concat(children.getOrDefault(thunk.t, Set.of()).stream(),
                            Set.of(i).stream()).collect(Collectors.toSet()));
                    }
                }
            }
        });

        if (parents.isEmpty() && children.isEmpty()) {
            return this.isFeasible();
        }

        for (ConstraintUpdateThunk thunk : thunks) {
            for (Integer parent : parents.getOrDefault(thunk.s, Set.of())) {
                for (Integer child : children.getOrDefault(thunk.t, Set.of())) {
                    this.putConstraint(parent, thunk.t, Constraint.add(this.matrix[parent][thunk.s], thunk.c));
                    this.putConstraint(thunk.s, child, Constraint.add(thunk.c, this.matrix[thunk.t][child]));
                    this.putConstraint(parent, child, Constraint.add(this.matrix[parent][thunk.s], thunk.c, this.matrix[thunk.t][child]));
                }
            }
        }

        this.isClosed = true;
        return this.tighten() && this.isZConsistent() && this.computeStrongClosure();
    }

    public boolean incrementalZClosure(int si, int ti, Constraint d) {
        return incrementalZClosure(List.of(ConstraintUpdateThunk.of(si, ti, d)));
    }

    private Stream<Constraint> generateStrongConstraints(Collection<ConstraintUpdateThunk> triples, int i) {
        int ibar = i ^ 1;
        return triples.stream().flatMap(triple -> {
                return Stream.<Constraint>of(
                    this.matrix[i][ibar],
                    Constraint.add(this.matrix[i][triple.s],
                        triple.c,
                        this.matrix[triple.t][ibar]),
                    Constraint.add(this.matrix[i][triple.tbar],
                        triple.c,
                        this.matrix[triple.sbar][ibar]),
                    Constraint.add(this.matrix[i][triple.tbar],
                        triple.c,
                        this.matrix[triple.sbar][triple.s],
                        triple.c,
                        this.matrix[triple.t][ibar]),
                    Constraint.add(this.matrix[i][triple.s],
                        triple.c,
                        this.matrix[triple.t][triple.tbar],
                        triple.c,
                        this.matrix[triple.sbar][ibar]));
            });
    }

    private Stream<Constraint> generateConstraints(Collection<ConstraintUpdateThunk> triples, int i, int j) {
        int ibar = i ^ 1;
        int jbar = j ^ 1;
        Constraint two = Constraint.of(2);
        return triples.stream().flatMap(triple -> {
                return Stream.<Constraint>of(
                    this.matrix[i][j],
                    Constraint.add(this.matrix[i][triple.s],
                        triple.c,
                        this.matrix[triple.t][j]),
                    Constraint.add(this.matrix[i][triple.tbar],
                        triple.c,
                        this.matrix[triple.sbar][j]),
                    Constraint.add(this.matrix[i][triple.tbar],
                        triple.c,
                        this.matrix[triple.sbar][triple.s],
                        triple.c,
                        this.matrix[triple.t][j]),
                    Constraint.add(this.matrix[i][triple.s],
                        triple.c,
                        this.matrix[triple.t][triple.tbar],
                        triple.c,
                        this.matrix[triple.sbar][j]),
                    Constraint.divide(Constraint.add(this.matrix[i][ibar],
                        this.matrix[jbar][j]),
                        two));
            });
    }

    public boolean incrementalZClosure(Collection<ConstraintUpdateThunk> triples) {
        Constraint two = Constraint.of(2);
        Constraint zero = Constraint.of(0);

        for (int i = 0; i < N; i++) {
            int ibar = i ^ 1;
            Constraint min = Constraint.min(generateStrongConstraints(triples, i));
            this.matrix[i][ibar] = Constraint.multiply(two, Constraint.divide(min, two));
        }

        if (!isZConsistent()) {
            bottomOut();
            return false;
        }

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (j != (i ^ 1)) {
                    Constraint min = Constraint.min(generateConstraints(triples, i, j));
                    this.matrix[i][j] = min;
                }
            }
            if (Constraint.compare(this.matrix[i][i], zero) < 0) {
                // bottomOut();
                return false;
            }
        }
        this.isClosed = true;
        return true;
    }

    public boolean computeClosure(boolean recompute) {
        // skip closure if matrix contains bottoms
        if (this.anyBottoms()) {
            this.isClosed = true;
            return false;
        }

        LOGGER.trace("previous: {}", this);
        if (recompute || !this.isClosed) {
            // ensure diagonal is zeroed.
            for (int i = 0; i < N; i++) {
                this.matrix[i][i] = Constraint.of(0);
            }

            for (int k = 0; k < N; k++) {
                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < N; j++) {
                        Constraint[] candidates = new Constraint[] {
                            this.matrix[i][j],
                            Constraint.add(this.matrix[i][k],
                                           this.matrix[k][j]),
                        };
                        Constraint min = Constraint.min(Stream.of(candidates));
                        LOGGER.trace("[constraints = {}, min={}]", candidates, min);
                        this.matrix[i][j] = min;
                        if (min.equals(Constraint.BOT())) {
                            this.isClosed = true;
                            bottomOut();
                            return false;
                        }
                    }
                }
            }
        }

        boolean feasible = true;
        for (int i = 0; i < N; i++) {
            if (this.matrix[i][i].isBottom() || this.matrix[i][i].bound().orElse(0) < 0) {
                feasible = false;
                bottomOut();
                break;
            }
        }
        this.isClosed = true;
        return feasible;
    }

    public boolean computeClosure() {
        return computeClosure(false);
    }

    public boolean computeStrongClosure() {
        // Assumes closure
        assert this.isClosed == true : "Octagon was not previously closed.";

        // skip strong closure if infeasible
        if (!isFeasible()) {
            return false;
        }

        Constraint two = Constraint.of(2);
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                // î = i ^ 1;
                // ĵ = j ^ 1;
                Constraint t = Constraint.divide(Constraint.add(this.matrix[i][i ^ 1],
                                                                this.matrix[j ^ 1][j]),
                                                 two);
                if (closureShouldUpdate(i, j, this.matrix[i][j], t)) {
                    this.matrix[i][j] = Constraint.min(this.matrix[i][j], t);
                }
            }
        }

        this.isClosed = true;
        return this.isFeasible();
    }

    public boolean canonicalize(boolean recompute) {
        return (computeClosure(recompute) &&
                tighten() &&
                isZConsistent() &&
                computeStrongClosure());
    }

    public boolean canonicalize() {
        return canonicalize(false);
    }

    /** compute the "reduced" closure of this matrix
     *
     * Compute the full closure (if necessary), then compute its reduction.
     * This reduction is loosely based on the reduction from Larsen in
     * larsen-1997-effic-verif.
     */
    public boolean computeReducedClosure() {
        if (!canonicalize()) {
            return false;
        }

        // We only iterate over the lower portion of the matrix because we
        // additionally remove the upper entry to maintain coherence.
        for (int k = 0; k < N; k++) {
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < (i + 1); j++) {
                    if (i == j || i == k || j == k) {
                        continue;
                    }
                    Constraint transitivePath = Constraint.add(this.matrix[i][k],
                                                               this.matrix[k][j]);
                    if (Constraint.compare(this.matrix[i][j], transitivePath) >= 0) {
                        this.setConstraint(i, j, Constraint.TOP());
                        this.setConstraint(j ^ 1, i ^ 1, Constraint.TOP());
                    }
                }
            }
        }
        return true;
    }

    public boolean tighten() {
        Constraint two = Constraint.of(2);
        for (int i = 0; i < N; i++) {
            Constraint tight = Constraint.multiply(Constraint.divide(this.matrix[i][i ^ 1], two), two);
            LOGGER.trace("[original = {}, tight = {}]", this.matrix[i][i ^ 1], tight);
            this.matrix[i][i ^ 1] = tight;
        }
        return true;
    }

    public boolean isZConsistent() {
        boolean consistent = true;
        if (!this.isFeasible()) {
            consistent = false;
        }
        for (int i = 0; i < N && consistent; i++) {
            if (Constraint.compare(Constraint.add(this.matrix[i][i ^ 1],
                                                  this.matrix[i ^ 1][i]),
                                   Constraint.of(0)) < 0) {
                consistent = false;
                break;
            }
        }
        return consistent;
    }


    public boolean w0zReduction() {
        Constraint two = Constraint.of(2);
        if (this.isClosed || this.canonicalize()) {
            for (int i = 0; i < this.N; i++) {
                for (int j = 0; j <= i; j++) {
                    if (i == j || (i ^ j) == 1) {
                        continue;
                    }
                    var ij = this.matrix[i][j];
                    var ibar = Constraint.divide(this.matrix[i][i ^ 1], two);
                    var jbar = Constraint.divide(this.matrix[j ^ 1][j], two);
                    if (Constraint.compare(ij, Constraint.add(ibar, jbar)) >= 0) {
                        this.matrix[i][j] = Constraint.TOP();
                        this.matrix[j ^ 1][i ^ 1] = Constraint.TOP();
                    }
                }
            }
            return true;
        }
        return false;
    }

    public static OctagonDifferenceBoundedMatrix widen(OctagonDifferenceBoundedMatrix m,
                                                       OctagonDifferenceBoundedMatrix n) {
        return widen(m, n, Set.of());
    }

    public static OctagonDifferenceBoundedMatrix widen(OctagonDifferenceBoundedMatrix m,
                                                       OctagonDifferenceBoundedMatrix n,
                                                       Set<Integer> steps) {
        OctagonDifferenceBoundedMatrix result = new OctagonDifferenceBoundedMatrix(m);
        result.widenWith(n, steps);
        return result;
    }

    public void widenWith(OctagonDifferenceBoundedMatrix n, Set<Integer> steps) {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (i == j) {
                    continue;
                }
                Constraint c1 = this.matrix[i][j];
                Constraint c2 = n.matrix[i][j];
                LOGGER.debug("widen check: {} > {} ?", c2, c1);
                if (!c1.isBottom() && !c2.isBottom() && c2.compareTo(c1) == 1) {
                    Constraint min = steps.stream()
                        .map(s -> Constraint.of(s))
                        .filter(s -> s.compareTo(c2) >= 0)
                        .min(Constraint::compare)
                        .orElse(Constraint.TOP());
                    LOGGER.trace("widening {} to {} [i = {}, j = {}]", c1, min, i, j);
                    this.matrix[i][j] = min;
                }
            }
        }
    }

    public void forgetConstraintsSimple(int k) {
        for (int i = 0; i < N; i++) {
            if (i == k) {
                continue;
            } else {
                this.setConstraint(k, i, Constraint.TOP());
                this.setConstraint(i, k, Constraint.TOP());
            }
        }
    }

    public void forgetConstraints(int k) {
        if (!this.isClosed) {
            iterateMatrix((i, j) -> {
                    if (i == j && j == k) {
                        this.setConstraint(i, j, Constraint.of(0));
                    } else if (i != k && j != k) {
                        Constraint c = Constraint.min(this.matrix[i][j],
                                                      Constraint.add(this.matrix[i][k],
                                                                     this.matrix[k][j]));
                        this.setConstraint(i, j, c);
                    }
                });
        }
        this.forgetConstraintsSimple(k);
    }

    public void makeInfeasible() {
        bottomOut();
    }

    public Interval32Box projectToInterval(int i, int j) {
        if (j == (i ^ 1) && i == (j ^ 1)) {
            Optional<Integer> lower = this.matrix[j][i].bound().map(b -> b / -2);
            Optional<Integer> upper = this.matrix[i][j].bound().map(b -> b / 2);
            return Interval32Box.of(lower, upper);
        } else {
            Optional<Integer> lower = this.matrix[j][i].bound().map(b -> b * -1);
            Optional<Integer> upper = this.matrix[i][j].bound();
            return Interval32Box.of(lower, upper);
        }
    }

    public Interval32Box projectToInterval(Pair<Integer, Integer> ij) {
        return this.projectToInterval(ij.fst(), ij.snd());
    }

    public void addIncoming(int k, Constraint add) {
        this.addIncoming(k, add, this);
    }

    public void addOutgoing(int k, Constraint add) {
        this.addOutgoing(k, add, this);
    }

    public void subIncoming(int k, Constraint sub) {
        this.subIncoming(k, sub, this);
    }

    public void subOutgoing(int k, Constraint sub) {
        this.subOutgoing(k, sub, this);
    }

    public void addInterval(int k, Constraint add) {
        this.addInterval(k, add, this);
    }

    public void addIncoming(int k, Constraint add, OctagonDifferenceBoundedMatrix from) {
        for (int i = 0; i < N; i++) {
            if (i == k || (i ^ k) == 1) {
                continue;
            }
            this.setConstraint(i, k, Constraint.add(from.matrix[i][k], add));
        }
    }

    public void addOutgoing(int k, Constraint add, OctagonDifferenceBoundedMatrix from) {
        for (int i = 0; i < N; i++) {
            if (i == k || (i ^ k) == 1) {
                continue;
            }
            this.setConstraint(k, i, Constraint.add(from.matrix[k][i], add));
        }
    }

    public void addInterval(int k, Constraint add, OctagonDifferenceBoundedMatrix from) {
        assert k % 2 == 0 : "Called with the wrong k";
        int khat = k ^ 1;
        Constraint shift = Constraint.multiply(add, Constraint.of(2));
        this.setConstraint(k, khat, Constraint.add(from.matrix[k][khat], shift));
        this.setConstraint(khat, k, Constraint.subtract(from.matrix[khat][k], shift));
    }

    public void subIncoming(int k, Constraint sub, OctagonDifferenceBoundedMatrix from) {
        for (int i = 0; i < N; i++) {
            if (i == k || (i ^ k) == 1) {
                continue;
            }
            this.setConstraint(i, k, Constraint.subtract(from.matrix[i][k], sub));
        }
    }

    public void subOutgoing(int k, Constraint sub, OctagonDifferenceBoundedMatrix from) {
        for (int i = 0; i < N; i++) {
            if (i == k || (i ^ k) == 1) {
                continue;
            }
            this.setConstraint(k, i, Constraint.subtract(from.matrix[k][i], sub));
        }
    }

    public GraphProjection toGraph(Function<Integer, String> lookup) {
        GraphProjection graph = new GraphProjection(IntStream.range(0, N).boxed().map(i -> lookup.apply(i)).collect(Collectors.toSet()));
        if (this.isFeasible()) {
            iterateMatrix((i, j) -> {
                    if (this.matrix[i][j].isTop()) {
                    } else if (i == j) {
                    } else {
                        graph.setConstraint(lookup.apply(i), lookup.apply(j), this.matrix[i][j].copy());
                    }
                });
        } else {
            iterateMatrix((i, j) -> {
                    if (i == j) {
                        graph.setConstraint(lookup.apply(i), lookup.apply(i), Constraint.BOT());
                    }
                });
        }
        return graph;
    }

    public Map<ConstraintType, Set<Integer>> queryConstraintTypes() {
        Map<Integer, ConstraintType> map = new HashMap<>();

        for (int i = 0; i < N; i += 2) {
            map.put(i, ConstraintType.INTERVAL);
        }

        for (int i = 0; i < N; i += 2) {
            for (int j = i; j < N; j += 2) {
                int ibar = i ^ 1;
                int jbar = j ^ 1;
                if (i == j || i == jbar || j == ibar) {
                    continue;
                }
                if (!this.matrix[i][j].isTop() || !this.matrix[j][i].isTop()) {
                    map.computeIfPresent(i, (k, v) -> v == ConstraintType.OCTAGONAL ? ConstraintType.OCTAGONAL : ConstraintType.ZONAL);
                    map.computeIfPresent(j, (k, v) -> v == ConstraintType.OCTAGONAL ? ConstraintType.OCTAGONAL : ConstraintType.ZONAL);
                }
                if (!this.matrix[ibar][j].isTop() || !this.matrix[i][jbar].isTop()) {
                    map.put(i, ConstraintType.OCTAGONAL);
                    map.put(j, ConstraintType.OCTAGONAL);
                }
            }
        }

        return map.entrySet()
            .stream()
            .map(kv -> Map.entry(kv.getValue(), Set.of(kv.getKey())))
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue, Sets::union));
    }

    @Override public boolean equals(Object o) {
        boolean equal = false;
        if (o != null && o instanceof OctagonDifferenceBoundedMatrix) {
            equal = this.equals((OctagonDifferenceBoundedMatrix) o);
        }
        return equal;
    }

    public boolean equals(OctagonDifferenceBoundedMatrix other) {
        if (this.N != other.N) {
            return false;
        } else {
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (!this.matrix[i][j].equals(other.matrix[i][j])) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    protected boolean isConstant(int i, int j) {
        return ((i ^ j) == 1) &&
                this.matrix[i][j].bound()
                .flatMap(a -> this.matrix[j][i].bound().map(b -> Math.abs(a) == Math.abs(b)))
                .orElse(false);
    }

    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + N;
        result = prime * result + this.matrix.hashCode();
        return result;
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append(Arrays.deepToString(this.matrix).replace("],", "],\n"));
        return sb.toString();
    }

    private void bottomOut() {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                this.matrix[i][j] = Constraint.BOT();
            }
        }
    }

    private boolean anyBottoms() {
        final Constraint bot = Constraint.BOT();
        return reduceMatrix((i, j) -> this.matrix[i][j].equals(bot),
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
