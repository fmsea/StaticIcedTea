package dev.fmsea.processing;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Local;

public class Smt2Projection {
    private static Logger LOGGER = LoggerFactory.getLogger(Smt2Projection.class);

    public static void format(Set<Local> projectedVariables, Reader reader, Writer writer) throws IOException {
        LOGGER.debug("Reading and parsing analysis");
        SmtReport input = Smt2Reader.fromAnalysisReport(Smt2Reader.parse(reader));
        LOGGER.debug("Projecting analysis report along variables: {}", projectedVariables);
        writer.write(input.format(expr -> expr.toProjectedSmt2(projectedVariables).orElse("true")));
    }
}
