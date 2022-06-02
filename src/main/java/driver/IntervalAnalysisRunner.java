package driver;

import java.nio.file.Path;
import abstractinterp.scalar.state.IntervalBoxState;
import abstractinterp.scalar.state.factory.IntervalBoxStateFactory;

public class IntervalAnalysisRunner extends AnalysisRunner<IntervalBoxState> {
    public IntervalAnalysisRunner(String className,
                                  int methodId,
                                  Path outputResultsPath) {
        super(className, methodId, outputResultsPath, new IntervalBoxStateFactory());
    }
}
