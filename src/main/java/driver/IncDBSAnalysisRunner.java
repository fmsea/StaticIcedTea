package driver;

import java.nio.file.Path;
import abstractinterp.scalar.state.IncDifferenceBoundedState;
import abstractinterp.scalar.state.factory.IncDifferenceBoundedStateFactory;

public class IncDBSAnalysisRunner extends AnalysisRunner<IncDifferenceBoundedState> {

    public IncDBSAnalysisRunner(String className,
                                int methodId,
                                Path outputResultsPath) {
        super(className, methodId, outputResultsPath, new IncDifferenceBoundedStateFactory());
    }
}
