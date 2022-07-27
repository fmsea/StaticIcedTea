package abstractinterp.scalar.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import soot.Local;
import org.jgrapht.Graph;
import org.jgrapht.nio.dot.DOTExporter;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import abstractinterp.scalar.state.State;
import abstractinterp.scalar.state.ZoneConstraint;

public class StateGraphExporter {

    private static Logger LOGGER = LoggerFactory.getLogger(StateGraphExporter.class);

    public static void toDot(String fileName, State abstractState) {
        fileName = fileName.replaceAll(" ", "-");
        LOGGER.trace("converting {} to dot/graph here: {}", abstractState, fileName);
        try (Writer writer = new FileWriter(fileName)) {
            toDot(writer, abstractState);
            writer.flush();
        } catch (IOException ex) {
            LOGGER.error("Unable to export graph to DOT: {}", ex.toString());
            LOGGER.trace(Stream.of(ex.getStackTrace())
                         .map(StackTraceElement::toString)
                         .collect(Collectors.joining("\n")));
        }
    }

    public static void toDot(Writer writer, State abstractState) {
        DOTExporter<Local, ZoneConstraint> exporter = new DOTExporter<>();
        exporter.setEdgeAttributeProvider(e -> {
                Map<String, Attribute> m = new HashMap<>(2);
                m.put("label", DefaultAttribute.createAttribute(e.toString()));
                return m;
            });
        exporter.setVertexAttributeProvider(v -> {
                Map<String, Attribute> m = new HashMap<>(2);
                m.put("label", DefaultAttribute.createAttribute(v.toString()));
                m.put("shape", DefaultAttribute.createAttribute("circle"));
                return m;
            });
        exporter.exportGraph(abstractState.toGraph(), writer);
    }
}
