package util;

import java.util.function.LongConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalysisTimer {

    private static final Logger LOG = LoggerFactory.getLogger(AnalysisTimer.class);

    private AnalysisTimer() {
    }

    public static void time(LongConsumer analysis) {
        System.gc();
        long start = System.nanoTime();
        analysis.accept(start);
        long end = System.nanoTime();
        LOG.info("analysis took {}μs", end - start);
    }
}
