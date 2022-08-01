package abstractinterp.scalar.state;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.ArrayDeque;
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
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultDirectedGraph;

import abstractinterp.scalar.state.util.GraphProjection;
import solver.SolverWrapper;
import util.Configuration;

public class DifferenceBoundedGraph {
    private static Logger LOGGER = LoggerFactory.getLogger(DifferenceBoundedGraph.class);

    private Graph<Local, DefaultEdge> graph;
    private Map<DefaultEdge, ZoneConstraint> constraints;

    public DifferenceBoundedGraph(Set<Local> locals, boolean top) {
        this.graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        this.constraints = new HashMap<>(locals.size() * 2);
        this.graph.addVertex(Variable.ZERO);
        locals.forEach(l -> this.graph.addVertex(l));
        this.graph.vertexSet().forEach(s -> {
                this.graph.vertexSet().forEach(t -> {
                        if (s.equals(t)) {
                            DefaultEdge edge = this.graph.addEdge(s, t);
                            this.constraints.put(edge, ZoneConstraint.of(0));
                        } else {
                            DefaultEdge edge = this.graph.addEdge(s, t);
                            this.constraints.put(edge, top ? ZoneConstraint.TOP() : ZoneConstraint.BOT());
                        }
                    });
            });
    }

    public Set<Local> getLocals() {
        return Collections.unmodifiableSet(this.graph.vertexSet());
    }

    public void setConstraint(Local source, Local target, ZoneConstraint constraint) {
        if (source.equals(target) && constraint.bound().map(b -> b < 0).orElse(false)) {
            DefaultEdge edge = this.graph.addEdge(source, target);
            this.constraints.put(edge, ZoneConstraint.BOT());
        } else if (source.equals(target) &&
                   (!constraint.isBottom() || constraint.bound().map(b -> b > 0).orElse(false))) {
            DefaultEdge edge = this.graph.addEdge(source, target);
            this.constraints.put(edge, ZoneConstraint.of(0));
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
                        ZoneConstraint c = this.constraints.get(e);
                        return !c.isTop();
                    }),
                succs.stream().filter(s -> {
                        DefaultEdge e = this.graph.getEdge(current, s);
                        ZoneConstraint c = this.constraints.get(e);
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
        GraphProjection g = new GraphProjection(this.getLocals());
        this.graph.edgeSet().stream().forEach(e -> {
                Local s = this.graph.getEdgeSource(e);
                Local t = this.graph.getEdgeTarget(e);
                ZoneConstraint c = this.constraints.get(e);
                g.setConstraint(s, t, c);
            });
        return g;
    }

    public static DifferenceBoundedMatrix to(DifferenceBoundedGraph g) {
        Set<Local> locals = g.graph.vertexSet();
        DifferenceBoundedMatrix m = new DifferenceBoundedMatrix(locals, true);
        g.graph.edgeSet().stream().forEach(e -> {
                Local s = g.graph.getEdgeSource(e);
                Local t = g.graph.getEdgeTarget(e);
                ZoneConstraint c = g.constraints.get(e);
                m.setConstraint(s, t, c);
            });
        return m;
    }

    public static DifferenceBoundedGraph from(DifferenceBoundedMatrix m) {
        return DifferenceBoundedMatrix.to(m);
    }

    @Override
    public String toString() {
        return DifferenceBoundedGraph.to(this).toString();
    }
}
