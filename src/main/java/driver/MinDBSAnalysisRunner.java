package driver;

import java.nio.file.Path;
import abstractinterp.scalar.state.MinDifferenceBoundedState;
import abstractinterp.scalar.state.factory.MinDifferenceBoundedStateFactory;

public class MinDBSAnalysisRunner extends AnalysisRunner<MinDifferenceBoundedState> {
    public MinDBSAnalysisRunner(String className,
                                int methodId,
                                Path outputResultsPath) {
        super(className, methodId, outputResultsPath, new MinDifferenceBoundedStateFactory());
    }
}
