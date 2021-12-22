package util;

import java.util.function.LongConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalysisTimer {

    private static final Logger LOG = LoggerFactory.getLogger(AnalysisTimer.class);

    private AnalysisTimer() {
    }

    public static void time(LongConsumer analysis) {
        AnalysisTimer.time(analysis, "analysis took {} ms");
    }

    public static void time(LongConsumer analysis, String messageFormat) {
        System.gc();
        long start = System.currentTimeMillis();
        analysis.accept(start);
        long end = System.currentTimeMillis();
        LOG.info(messageFormat, end - start);
    }
}
