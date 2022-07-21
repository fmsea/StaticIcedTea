package driver;

import java.nio.file.Path;
import abstractinterp.scalar.state.MinZoneState;
import abstractinterp.scalar.state.factory.MinZoneStateFactory;

public class MinZoneAnalysisRunner extends AnalysisRunner<MinZoneState> {
    public MinZoneAnalysisRunner(String className,
                                int methodId,
                                Path outputResultsPath) {
        super(className, methodId, outputResultsPath, new MinZoneStateFactory());
    }
}
