package dev.fmsea.processing.smt;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ManyToManyShortestPathsAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraManyToManyShortestPaths;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;

import dev.fmsea.util.Sets;
import soot.Local;

public class SmtGraph {
    private Graph<Local, DefaultEdge> graph;
    private Map<DefaultEdge, Set<SmtExpression>> edges;

    private SmtGraph() {
        this.edges = new HashMap<>();
        this.graph = new DefaultDirectedGraph<>(DefaultEdge.class);
    }

    private static void copyVertices(SmtGraph src, SmtGraph dst) {
        src.graph.vertexSet().stream().forEach(v -> dst.graph.addVertex(v));
    }

    private static void copyEdges(SmtGraph src, SmtGraph dst) {
        src.graph.edgeSet().stream().forEach(e -> {
                Local s = src.graph.getEdgeSource(e);
                Local t = src.graph.getEdgeTarget(e);
                Set<SmtExpression> exprs = src.edges.get(e);
                if (!dst.graph.containsEdge(s, t)) {
                    DefaultEdge edge = dst.graph.addEdge(s, t);
                    dst.edges.put(edge, exprs);
                } else {
                    dst.edges.put(e, Stream.concat(src.edges.get(e).stream(),
                                                   exprs.stream()).collect(Collectors.toSet()));
                }
            });
    }

    public static SmtGraph empty() {
        return new SmtGraph();
    }

    public void addEdge(Local source, Local target, SmtExpression expr) {
        this.graph.addVertex(source);
        this.graph.addVertex(target);
        DefaultEdge edge = this.graph.addEdge(source, target);
        this.edges.put(edge, Set.of(expr));
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
                    DefaultEdge edge = this.graph.addEdge(source, target);
                    if (edge != null) {
                        Set<SmtExpression> exprs = paths.getPath(source, target)
                            .getEdgeList()
                            .stream()
                            .flatMap(e -> this.edges.get(e).stream())
                            .collect(Collectors.toSet());
                        this.edges.put(edge, exprs);
                    }
                }
            }
        }
        return this;
    }

    private Graph<Local, DefaultEdge> computeConnectedClosure() {
        Graph<Local, DefaultEdge> graph = new AsUndirectedGraph(this.graph);
        ManyToManyShortestPathsAlgorithm<Local, DefaultEdge> alg =
            new DijkstraManyToManyShortestPaths<>(graph);
        ManyToManyShortestPathsAlgorithm.ManyToManyShortestPaths<Local, DefaultEdge> paths =
            alg.getManyToManyPaths(graph.vertexSet(), graph.vertexSet());
        for (Local source : paths.getSources()) {
            for (Local target : paths.getTargets()) {
                if (paths.getPath(source, target) != null) {
                    graph.addEdge(source, target);
                    graph.addEdge(target, source);
                }
            }
        }
        return graph;
    }

    public Map<Local, Set<Local>> connectedProjection() {
        Graph<Local, DefaultEdge> graph = new AsUndirectedGraph(this.graph);
        Map<Local, Set<Local>> connected = new HashMap<>();
        ManyToManyShortestPathsAlgorithm<Local, DefaultEdge> alg =
            new DijkstraManyToManyShortestPaths<>(graph);
        ManyToManyShortestPathsAlgorithm.ManyToManyShortestPaths<Local, DefaultEdge> paths =
            alg.getManyToManyPaths(graph.vertexSet(), graph.vertexSet());
        for (Local source : paths.getSources()) {
            for (Local target : paths.getTargets()) {
                if (paths.getPath(source, target) != null) {
                    connected.merge(source, Set.of(target), Sets::union);
                }
            }
        }
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

    public void toDot(Set<Local> deltaV, Writer writer) {
        toDot(this, deltaV, writer);
    }

    public static void toDot(SmtGraph graph, Set<Local> deltaV, Writer writer) {
        DOTExporter<Local, DefaultEdge> exporter = new DOTExporter<>();
        Map<DefaultEdge, Set<SmtExpression>> edges = Map.copyOf(graph.edges);
        exporter.setEdgeAttributeProvider(e -> {
                Set<SmtExpression> exprs = edges.get(e);
                Map<String, Attribute> m = Map.of("label",
                                                  DefaultAttribute.createAttribute(exprs.toString()));
                return m;
            });
        exporter.setVertexAttributeProvider(v -> {
                String shape = deltaV.contains(v) ? "doublecircle" : "circle";
                Map<String, Attribute> m = Map.of("label", DefaultAttribute.createAttribute(v.toString()),
                                                  "shape", DefaultAttribute.createAttribute(shape));
                return m;
            });
        exporter.exportGraph(graph.graph, writer);
    }
}
