package driver;

import java.nio.file.Path;
import abstractinterp.scalar.state.ZoneState;
import abstractinterp.scalar.state.factory.ZoneStateFactory;

public class ZoneAnalysisRunner extends AnalysisRunner<ZoneState> {
    public ZoneAnalysisRunner(String className,
                                   int methodId,
                                   Path outputResultsPath) {
        super(className, methodId, outputResultsPath, new ZoneStateFactory());
    }
}
