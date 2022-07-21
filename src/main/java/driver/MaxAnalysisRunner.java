package driver;

import java.nio.file.Path;
import abstractinterp.scalar.state.MaxZoneState;
import abstractinterp.scalar.state.factory.MaxZoneStateFactory;

public class MaxAnalysisRunner extends AnalysisRunner<MaxZoneState> {
    public MaxAnalysisRunner(String className,
                                int methodId,
                                Path outputResultsPath) {
        super(className, methodId, outputResultsPath,  new MaxZoneStateFactory());
    }
}
