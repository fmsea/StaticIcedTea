package abstractinterp.scalar;

import java.io.IOException;
import java.io.Writer;

public interface Analysis {
    void runAnalysis();
    String generateReport();
    String generateSMTReport();
    void writeSMTReport(Writer writer) throws IOException;
    void report();
}
