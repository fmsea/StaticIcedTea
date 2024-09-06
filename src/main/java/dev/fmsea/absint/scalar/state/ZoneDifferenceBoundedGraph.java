package dev.fmsea.absint.scalar.state;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.fmsea.absint.scalar.state.util.GraphProjection;
import soot.Local;

public class ZoneDifferenceBoundedGraph {
    private static Logger LOGGER = LoggerFactory.getLogger(ZoneDifferenceBoundedGraph.class);

    private Graph<Local, DefaultEdge> graph;
    private Map<DefaultEdge, Constraint> constraints;

    public ZoneDifferenceBoundedGraph(Set<Local> locals, boolean top) {
        this.graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        this.constraints = new HashMap<>(locals.size() * 2);
        this.graph.addVertex(Variable.ZERO);
        locals.forEach(l -> this.graph.addVertex(l));
        this.graph.vertexSet().forEach(s -> {
                this.graph.vertexSet().forEach(t -> {
                        if (s.equals(t)) {
                            DefaultEdge edge = this.graph.addEdge(s, t);
                            this.constraints.put(edge, Constraint.of(0));
                        } else {
                            DefaultEdge edge = this.graph.addEdge(s, t);
                            this.constraints.put(edge, top ? Constraint.TOP() : Constraint.BOT());
                        }
                    });
            });
    }

    public Set<Local> getLocals() {
        return Collections.unmodifiableSet(this.graph.vertexSet());
    }

    public void setConstraint(Local source, Local target, Constraint constraint) {
        if (source.equals(target) && constraint.bound().map(b -> b < 0).orElse(false)) {
            DefaultEdge edge = this.graph.addEdge(source, target);
            this.constraints.put(edge, Constraint.BOT());
        } else if (source.equals(target) &&
                   (!constraint.isBottom() || constraint.bound().map(b -> b > 0).orElse(false))) {
            DefaultEdge edge = this.graph.addEdge(source, target);
            this.constraints.put(edge, Constraint.of(0));
        } else {
            if (this.graph.containsEdge(source, target)) {
                DefaultEdge edge = this.graph.removeEdge(source, target);
                this.constraints.remove(edge);
            }
            DefaultEdge edge = this.graph.addEdge(source, target);
            this.constraints.put(edge, constraint);
        }
    }

    public Set<Local> getConnectedVariablesOf(Local source) {
        LOGGER.debug("Visiting: {}", source);
        Set<Local> connected = new HashSet<>();
        connected.add(source);
        Deque<Local> toVisit = new ArrayDeque<>();
        toVisit.push(source);
        while (toVisit.peek() != null) {
            Local current = toVisit.pop();
            LOGGER.trace("popped {}", current);
            List<Local> preds = Graphs.predecessorListOf(this.graph, current);
            List<Local> succs = Graphs.successorListOf(this.graph, current);
            Stream.concat(preds.stream().filter(p -> {
                        DefaultEdge e = this.graph.getEdge(p, current);
                        Constraint c = this.constraints.get(e);
                        return !c.isTop();
                    }),
                succs.stream().filter(s -> {
                        DefaultEdge e = this.graph.getEdge(current, s);
                        Constraint c = this.constraints.get(e);
                        return !c.isTop();
                    }))
                .filter(c -> !c.equals(Variable.ZERO))
                .forEach(c -> {
                        if (!connected.contains(c)) {
                            connected.add(c);
                            toVisit.push(c);
                        }
                    });
        }
        return Set.copyOf(connected);
    }

    public GraphProjection toGraph() {
        GraphProjection g = new GraphProjection(this.getLocals().stream().map(l -> l.toString()).collect(Collectors.toSet()));
        this.graph.edgeSet().stream().forEach(e -> {
                Local s = this.graph.getEdgeSource(e);
                Local t = this.graph.getEdgeTarget(e);
                Constraint c = this.constraints.get(e);
                g.setConstraint(s.toString(), t.toString(), c);
            });
        return g;
    }

    public static ZoneDifferenceBoundedMatrix to(ZoneDifferenceBoundedGraph g) {
        Set<Local> locals = g.graph.vertexSet();
        ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(locals, true);
        g.graph.edgeSet().stream().forEach(e -> {
                Local s = g.graph.getEdgeSource(e);
                Local t = g.graph.getEdgeTarget(e);
                Constraint c = g.constraints.get(e);
                m.setConstraint(s, t, c);
            });
        return m;
    }

    public static ZoneDifferenceBoundedGraph from(ZoneDifferenceBoundedMatrix m) {
        return ZoneDifferenceBoundedMatrix.to(m);
    }

    @Override
    public String toString() {
        return ZoneDifferenceBoundedGraph.to(this).toString();
    }
}
