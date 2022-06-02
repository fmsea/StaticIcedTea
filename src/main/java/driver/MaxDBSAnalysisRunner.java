package driver;

import java.nio.file.Path;
import abstractinterp.scalar.state.MaxDifferenceBoundedState;
import abstractinterp.scalar.state.factory.MaxDifferenceBoundedStateFactory;

public class MaxDBSAnalysisRunner extends AnalysisRunner<MaxDifferenceBoundedState> {
    public MaxDBSAnalysisRunner(String className,
                                int methodId,
                                Path outputResultsPath) {
        super(className, methodId, outputResultsPath,  new MaxDifferenceBoundedStateFactory());
    }
}
