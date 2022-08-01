package abstractinterp.scalar.state.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
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
import org.jgrapht.nio.dot.DOTExporter;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;

import abstractinterp.scalar.state.Constraint;

public class GraphProjection {
    private static Logger LOGGER = LoggerFactory.getLogger(GraphProjection.class);

    private Graph<Local, DefaultEdge> graph;
    private Map<DefaultEdge, Constraint> constraints;

    public GraphProjection(Set<Local> locals) {
        this.graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        this.constraints = new HashMap<>(locals.size() * 2);
        locals.forEach(l -> this.graph.addVertex(l));
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

    public void toDot(String fileName) {
        fileName = fileName.replace(" ", "-");
        LOGGER.trace("converting {} to dot/graph here: {}", this, fileName);
        try (Writer writer = new FileWriter(fileName)) {
            this.toDot(writer);
            writer.flush();
        } catch (IOException ex) {
            LOGGER.error("Unable to export graph to DOT: {}", ex.getMessage());
            LOGGER.trace(Stream.of(ex.getStackTrace())
                         .map(StackTraceElement::toString)
                         .collect(Collectors.joining("\n")));
        }
    }

    public void toDot(Writer writer) {
        DOTExporter<Local, DefaultEdge> exporter = new DOTExporter<>();
        Map<DefaultEdge, Constraint> edges = Map.copyOf(this.constraints);
        exporter.setEdgeAttributeProvider(e -> {
                Constraint c = edges.get(e);
                Map<String, Attribute> m = Map.of("label",
                                                  DefaultAttribute.createAttribute(c.toString()));
                return m;
            });
        exporter.setVertexAttributeProvider(v -> {
                Map<String, Attribute> m = Map.of("label", DefaultAttribute.createAttribute(v.toString()),
                                                  "shape", DefaultAttribute.createAttribute("circle"));
                return m;
            });
        exporter.exportGraph(this.graph, writer);
    }
}
