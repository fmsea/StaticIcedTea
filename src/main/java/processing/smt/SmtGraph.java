package processing.smt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import soot.Local;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ManyToManyShortestPathsAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraManyToManyShortestPaths;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class SmtGraph {
    private Graph<Local, DefaultEdge> graph;

    private SmtGraph() {
        this.graph = new DefaultDirectedGraph<>(DefaultEdge.class);
    }

    private static void copyVertices(SmtGraph src, SmtGraph dst) {
        src.graph.vertexSet().stream().forEach(v -> dst.graph.addVertex(v));
    }

    private static void copyEdges(SmtGraph src, SmtGraph dst) {
        src.graph.edgeSet().stream().forEach(e -> {
                Local s = src.graph.getEdgeSource(e);
                Local t = src.graph.getEdgeTarget(e);
                if (!dst.graph.containsEdge(s, t)) {
                    dst.graph.addEdge(s, t);
                }
            });
    }

    public static SmtGraph empty() {
        return new SmtGraph();
    }

    public void addEdge(Local source, Local target) {
        this.graph.addVertex(source);
        this.graph.addVertex(target);
        this.graph.addEdge(source, target);
    }

    public static SmtGraph union(SmtGraph g1, SmtGraph g2) {
        SmtGraph union = new SmtGraph();
        copyVertices(g1, union);
        copyVertices(g2, union);
        copyEdges(g1, union);
        copyEdges(g2, union);
        return union;
    }

    public SmtGraph computeClosure() {
        ManyToManyShortestPathsAlgorithm<Local, DefaultEdge> alg =
            new DijkstraManyToManyShortestPaths<>(this.graph);
        ManyToManyShortestPathsAlgorithm.ManyToManyShortestPaths<Local, DefaultEdge> paths =
            alg.getManyToManyPaths(this.graph.vertexSet(), this.graph.vertexSet());
        for (Local source : paths.getSources()) {
            for (Local target : paths.getTargets()) {
                if (paths.getPath(source, target) != null) {
                    this.graph.addEdge(source, target);
                }
            }
        }
        return this;
    }

    public Map<Local, Set<Local>> connectedProjection() {
        Map<Local, Set<Local>> connected = new HashMap<>();
        this.graph.vertexSet().forEach(source -> {
                Set<Local> neighbors = this.graph.edgesOf(source)
                    .stream()
                    .flatMap(e -> Stream.of(this.graph.getEdgeSource(e),
                                            this.graph.getEdgeTarget(e)))
                    .collect(Collectors.toSet());
                connected.put(source, neighbors);
            });
        return connected;
    }

    public Map<Local, Set<Local>> reachableProjection() {
        Map<Local, Set<Local>> reachable = new HashMap<>();
        this.graph.vertexSet().forEach(source -> {
                Set<Local> outNeighbors = this.graph.outgoingEdgesOf(source)
                    .stream()
                    .map(e -> this.graph.getEdgeTarget(e))
                    .collect(Collectors.toSet());
                reachable.put(source, outNeighbors);
            });
        return reachable;
    }

    public Map<Local, Set<Local>> neighborProjection() {
        Map<Local, Set<Local>> neighbors = new HashMap<>();
        this.graph.vertexSet().forEach(v -> {
                Set<Local> inNeighbors = this.graph.incomingEdgesOf(v)
                    .stream()
                    .map(e -> this.graph.getEdgeSource(e))
                    .collect(Collectors.toSet());
                Set<Local> outNeighbors = this.graph.outgoingEdgesOf(v)
                    .stream()
                    .map(e -> this.graph.getEdgeTarget(e))
                    .collect(Collectors.toSet());
                neighbors.put(v, Stream.concat(inNeighbors.stream(),
                                               outNeighbors.stream()).collect(Collectors.toSet()));
            });
        return neighbors;
    }

    public Set<Local> neighborsProjectionOf(Local l) {
        return this.graph.edgesOf(l).stream()
            .flatMap(e -> Stream.of(this.graph.getEdgeSource(e),
                                    this.graph.getEdgeTarget(e)))
            .collect(Collectors.toSet());
    }

    public Set<Local> neighborsProjectionOf(Set<Local> ls) {
        return ls.stream()
            .flatMap(l -> neighborsProjectionOf(l).stream())
            .collect(Collectors.toSet());
    }
}
