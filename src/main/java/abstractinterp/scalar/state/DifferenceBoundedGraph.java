package abstractinterp.scalar.state;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import soot.Local;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.BFSShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.dot.DOTExporter;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DifferenceBoundedGraph {

    private static Logger LOGGER = LoggerFactory.getLogger(DifferenceBoundedGraph.class);
    private Graph<Local, DefaultEdge> graph;
    private Map<DefaultEdge, Constraint> constraints;
    private boolean feasible;
    private final Local ZERO = Variable.ZERO;

    public DifferenceBoundedGraph(Set<Local> locals) {
        this(locals, true);
    }

    public DifferenceBoundedGraph(Set<Local> locals, boolean feasible) {
        super();
        this.feasible = feasible;
        this.graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        this.constraints = new HashMap<>(locals.size() * 2);
        for (Local l : locals) {
            this.graph.addVertex(l);
        }
    }

    public DifferenceBoundedGraph(DifferenceBoundedGraph source) {
        this(source.graph.vertexSet(), source.feasible);
        source.copyTo(this);
    }

    public Set<Local> getLocals() {
        return this.graph.vertexSet();
    }

    public List<DBSTriple> getConstraints() {
        Set<DefaultEdge> edges = this.graph.edgeSet();
        List<DBSTriple> triples = new ArrayList<>(edges.size());
        for (DefaultEdge edge : edges) {
            Local s = this.graph.getEdgeSource(edge);
            Local t = this.graph.getEdgeTarget(edge);
            Constraint c = this.eval(s, t);
            triples.add(new DBSTriple(s, t, c));
        }
        Collections.sort(triples);
        return triples;
    }

    public List<DBSTriple> getConstraints(Local local) {
        Set<DefaultEdge> edges = this.graph.edgesOf(local);
        List<DBSTriple> triples = new ArrayList<>(edges.size());
        for (DefaultEdge edge : edges) {
            Local s = this.graph.getEdgeSource(edge);
            Local t = this.graph.getEdgeTarget(edge);
            Constraint c = this.eval(s, t);
            triples.add(new DBSTriple(s, t, c));
        }
        Collections.sort(triples);
        return triples;
    }

    public void copyTo(DifferenceBoundedGraph out) {
        if (out == null) { return; }
        out.constraints.clear();
        this.graph.vertexSet().forEach(v -> {
                out.graph.addVertex(v);
            });
        Graph<Local, DefaultEdge> edgeWalk = copyGraph(out.graph);
        out.graph.removeAllEdges(edgeWalk.edgeSet());
        this.graph.edgeSet().forEach(e -> {
                Local s = this.graph.getEdgeSource(e);
                Local t = this.graph.getEdgeTarget(e);
                Constraint c = this.eval(s, t);
                out.add(s, t, c);
            });
    }

    public boolean isFeasible() {
        this.computeClosure();
        this.feasible = this.computeClosure() && !this.anyBottoms();
        LOGGER.trace("isFeasible - this graph {} ? {}", this, this.feasible);
        return this.feasible;
    }

    /** Add a constraint through the two provided locals.
     *
     * The ordering implies the relationship between variables:
     * l - r < c
     *
     * Remove the edge before adding the new edge of the constraint.
     */
    public void add(Local x, Local y, Constraint c) {
        if (c.isBottom()) {
            this.feasible = false;
        }

        {
            DefaultEdge edge = this.graph.removeEdge(x, y);
            this.constraints.remove(edge);
        }

        {
            if (!c.isTop()) {
                DefaultEdge edge = this.graph.addEdge(x, y);
                this.constraints.put(edge, c);
            }
        }
    }

    private void remove(DefaultEdge edge) {
        Local s = this.graph.getEdgeSource(edge);
        Local t = this.graph.getEdgeTarget(edge);
        this.remove(s, t);
    }

    private void remove(Local x, Local y) {
        DefaultEdge edge = this.graph.removeEdge(x, y);
        if (edge != null) {
            this.constraints.remove(edge);
        }
    }

    public Constraint eval(Local x, Local y) {
        return this.getValue(x, y).orElse(Constraint.TOP());
    }

    private Constraint eval(DefaultEdge edge) {
        Local s = this.graph.getEdgeSource(edge);
        Local t = this.graph.getEdgeTarget(edge);
        return this.eval(s, t);
    }

    public Optional<Constraint> getValue(Local x, Local y) {
        DefaultEdge e;
        Constraint c;
        if ((e = this.graph.getEdge(x, y)) != null && (c = this.constraints.get(e)) != null) {
            return Optional.of(c);
        } else {
            return Optional.empty();
        }
    }

    public boolean computeClosure() {
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

        BiFunction<Integer, Integer, Optional<GraphPath<Local, DefaultEdge>>> getPath = (i, j) -> {
            Local s = indicesToVertices.get(i);
            Local t = indicesToVertices.get(j);
            return Optional.ofNullable(BFSShortestPath.findPathBetween(this.graph, s, t));
        };

        BiPredicate<Integer, Integer> isPathThroughZERO = (i, j) -> {
            Optional<GraphPath<Local, DefaultEdge>> path = getPath.apply(i, j);
            return path.map(p -> {
                    int index = p.getVertexList().indexOf(ZERO);
                    return index > 0 && index != p.getLength();
                }).orElse(false);
        };

        Constraint[][] dbm = new Constraint[dim][dim];
        for (int i = 0; i < dim; i++) {
            Arrays.fill(dbm[i], Constraint.TOP());
            dbm[i][i] = new Constraint(0);
        }

        // Copy Known Constraints into Matrix
        for (DefaultEdge e : this.graph.edgeSet()) {
            Local s = this.graph.getEdgeSource(e);
            Local t = this.graph.getEdgeTarget(e);
            int si = indices.get(s);
            int ti = indices.get(t);
            dbm[si][ti] = this.eval(s, t);
        }

        // Floyd-Warshall Shortest Paths
        for (int k = 0; k < dim; k++) {
            for (int i = 0; i < dim; i++) {
                for (int j = 0; j < dim; j++) {
                    int pathLength = Stream.of(getPath.apply(i, k),
                                               getPath.apply(k, j))
                        .map(op -> op.map(p -> p.getLength()).orElse(0))
                        .reduce(0, (a, b) -> a + b);
                    if (isPathThroughZERO.test(i, j) && pathLength >= 2) {
                        continue;
                    }
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
                if (v1i == v2i && dbm[v1i][v2i].bound() == 0) {
                    continue;
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

    private boolean anyBottoms() {
        return this.constraints.containsValue(Constraint.BOT());
    }

    public void projectIntervals(Local t) {
        this.projectIntervalsFrom(t, this);
    }

    public void projectIntervalsFrom(Local t, DifferenceBoundedGraph from) {
        from.graph.vertexSet().forEach(v -> {
                this.projectIntervalFrom(v, t, from);
            });
    }

    public void projectInterval(Local s, Local t) {
        this.projectIntervalFrom(s, t, this);
    }

    public void projectIntervalFrom(Local s, Local t, DifferenceBoundedGraph from) {
        LOGGER.debug("Project Interval from {} to {}", s, t);
        LOGGER.trace("Before Projection: {} from {}", this.toString(), from.toString());
        GraphPath<Local, DefaultEdge> path = BFSShortestPath.findPathBetween(from.graph, s, t);
        if (path != null) {
            Constraint newEdge = new Constraint(0);
            for (DefaultEdge e : path.getEdgeList()) {
                Constraint c = from.eval(e);
                assert c != null;
                newEdge.add(c);
            }
            this.add(s, t, newEdge);
        }
        LOGGER.trace("After Projection: {}", this.toString());
    }

    public void widenWith(DifferenceBoundedGraph other) {
        // close incoming graph
        other.computeClosure();
        LOGGER.debug("widening this {} with {}", this.toString(), other.toString());
        LOGGER.debug("other graph is feasible: {}", other.feasible);
        for (Local s : this.graph.vertexSet()) {
            for (Local t : this.graph.vertexSet()) {
                Constraint c1 = this.eval(s, t);
                Constraint c2 = other.eval(s, t);
                LOGGER.trace("widening ({} -> {}) {} < {}",
                             s, t, c1, c2);
                if (!(c1.isBottom() || c2.isBottom()) && c1.compareTo(c2) == 1) {
                    this.add(s, t, Constraint.TOP());
                }
            }
        }
    }

    private static Graph<Local, DefaultEdge> copyGraph(Graph<Local, DefaultEdge> graph) {
        Graph<Local, DefaultEdge> copy = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.vertexSet().forEach(v -> copy.addVertex(v));
        graph.edgeSet().forEach(e -> {
                Local s = graph.getEdgeSource(e);
                Local t = graph.getEdgeTarget(e);
                copy.addEdge(s, t, e);
            });
        return copy;
    }

    public boolean intersection(DifferenceBoundedGraph with) {
        LOGGER.debug("Intersecting two graphs");
        LOGGER.trace("Intersection between {} and {}", this, with);

        for (Local s : this.graph.vertexSet()) {
            for (Local t : this.graph.vertexSet()) {
                Constraint c1 = this.eval(s, t);
                Constraint c2 = with.eval(s, t);
                this.add(s, t, Constraint.min(c1, c2));
            }
        }
        LOGGER.trace("intersected: {}", this);
        return this.isFeasible();
    }

    public boolean union(DifferenceBoundedGraph with) {
        LOGGER.debug("computing union of two graphs");
        LOGGER.trace("union of {} and {}", this, with);
        this.computeClosure();
        with.computeClosure();

        for (Local s : this.graph.vertexSet()) {
            for (Local t : this.graph.vertexSet()) {
                Constraint c1 = this.eval(s, t);
                Constraint c2 = with.eval(s, t);
                LOGGER.trace("joining: ({} -> {}) max({}, {}) = {}",
                             s, t, c1, c2, Constraint.max(c1, c2));
                this.add(s, t, Constraint.max(c1, c2));
            }
        }
        LOGGER.trace("union: {}", this);
        return this.isFeasible();
    }

    public void addIncoming(Local local, Constraint add) {
        addIncomingFrom(local, add, this);
    }

    public void addOutgoing(Local local, Constraint add) {
        addOutgoingFrom(local, add, this);
    }

    public void subIncoming(Local local, Constraint sub) {
        subIncomingFrom(local, sub, this);
    }

    public void subOutgoing(Local local, Constraint sub) {
        subOutgoingFrom(local, sub, this);
    }

    public void addIncomingFrom(Local target,
                                Constraint add,
                                DifferenceBoundedGraph from) {
        from.graph.incomingEdgesOf(target).forEach(e -> {
                Local source = from.graph.getEdgeSource(e);
                Constraint c = from.eval(source, target);
                this.add(source, target, Constraint.add(c, add));
            });
    }

    public void addOutgoingFrom(Local source,
                                Constraint add,
                                DifferenceBoundedGraph from) {
        from.graph.outgoingEdgesOf(source).forEach(e -> {
                Local target = from.graph.getEdgeTarget(e);
                Constraint c = from.eval(source, target);
                this.add(source, target, Constraint.add(c, add));
            });
    }

    public void subIncomingFrom(Local target,
                                Constraint sub,
                                DifferenceBoundedGraph from) {
        from.graph.incomingEdgesOf(target).forEach(e -> {
                Local source = from.graph.getEdgeSource(e);
                Constraint c = from.eval(source, target);
                this.add(source, target, Constraint.subtract(c, sub));
            });
    }

    public void subOutgoingFrom(Local source,
                                Constraint sub,
                                DifferenceBoundedGraph from) {
        from.graph.outgoingEdgesOf(source).forEach(e -> {
                Local target = from.graph.getEdgeTarget(e);
                Constraint c = from.eval(source, target);
                this.add(source, target, Constraint.subtract(c, sub));
            });
    }

    public void forget(Local local) {
        this.computeClosure();
        this.forgetConstraints(local);
    }

    public void forgetConstraints(Local local) {
        this.graph.edgesOf(local).forEach(e -> {
                this.remove(e);
            });
    }

    public void updateTop(Local local) {
        Graph<Local, DefaultEdge> copy = copyGraph(this.graph);
        copy.outgoingEdgesOf(local).forEach(e -> {
                Local t = this.graph.getEdgeTarget(e);
                this.updateTop(local, t);
            });
    }

    public void updateTop(Local left, Local right) {
        this.add(left, right, Constraint.TOP());
    }

    public void makeInfeasible() {
        Graph<Local, DefaultEdge> walk = copyGraph(this.graph);
        for (DefaultEdge e : walk.edgeSet()) {
            Local s = this.graph.getEdgeSource(e);
            Local t = this.graph.getEdgeTarget(e);
            this.add(s, t, Constraint.BOT());
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (DefaultEdge edge : this.graph.edgeSet()) {
            Local s = this.graph.getEdgeSource(edge);
            Local t = this.graph.getEdgeTarget(edge);
            Constraint c = this.eval(s, t);
            sb.append(String.format("(%s - %s ≺ %s)", s, t, c));
        }
        sb.append("]");
        return sb.toString();
    }

    public void toDot(String filename) {
         try (Writer w = new FileWriter(filename)) {
             this.toDot(w);
         } catch (IOException ex) {
            LOGGER.error("Unable to export graph to DOT: {}", ex.toString());
            LOGGER.trace(Stream.of(ex.getStackTrace())
                         .map(StackTraceElement::toString)
                         .collect(Collectors.joining("\n")));
        }
    }

    public void toDot(Writer writer) {
        DOTExporter<Local, DefaultEdge> exporter = new DOTExporter<>();
        exporter.setEdgeAttributeProvider(e -> {
                Map<String, Attribute> map = new HashMap<>(2);
                map.put("label", DefaultAttribute.createAttribute(this.eval(e).toString()));
                return map;
            });
        exporter.setVertexAttributeProvider(v -> {
                Map<String, Attribute> map = new HashMap<>(2);
                map.put("label", DefaultAttribute.createAttribute(v.toString()));
                map.put("shape", DefaultAttribute.createAttribute("circle"));
                return map;
            });
        exporter.exportGraph(this.graph, writer);
    }

    @Override
    public boolean equals(Object o) {
        boolean equals = false;
        if (o != null && o instanceof DifferenceBoundedGraph) {
            DifferenceBoundedGraph other = (DifferenceBoundedGraph)o;
            equals = (this.feasible == other.feasible &&
                      this.graph.edgeSet().size() == other.graph.edgeSet().size());
            Iterator<DefaultEdge> it = this.graph.edgeSet().iterator();
            while (equals && it.hasNext()) {
                DefaultEdge edge = it.next();
                Local s = this.graph.getEdgeSource(edge);
                Local t = this.graph.getEdgeTarget(edge);
                Constraint c1 = this.eval(s, t);
                Constraint c2 = other.eval(s, t);
                if (!c1.equals(c2)) {
                    equals = false;
                }
            }
        }
        return equals;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + this.constraints.hashCode();
        result = prime * result + this.graph.hashCode();
        result = prime * result + (this.feasible ? 1 : 0);
        return result;
    }
}
