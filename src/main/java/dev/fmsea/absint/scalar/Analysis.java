package dev.fmsea.absint.scalar;

import java.io.IOException;
import java.io.Writer;

public interface Analysis {
    void runAnalysis();
    void writeReport(Writer writer) throws IOException;
}
