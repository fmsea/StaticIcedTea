package driver;

import java.nio.file.Path;
import abstractinterp.scalar.state.IncZoneState;
import abstractinterp.scalar.state.factory.IncZoneStateFactory;

public class IncZoneAnalysisRunner extends AnalysisRunner<IncZoneState> {

    public IncZoneAnalysisRunner(String className,
                                int methodId,
                                Path outputResultsPath) {
        super(className, methodId, outputResultsPath, new IncZoneStateFactory());
    }
}
