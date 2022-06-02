package driver;

import java.nio.file.Path;
import abstractinterp.scalar.state.PADO01DifferenceBoundedState;
import abstractinterp.scalar.state.factory.PADO01DifferenceBoundedStateFactory;

public class PADO01DBSAnalysisRunner extends AnalysisRunner<PADO01DifferenceBoundedState> {
    public PADO01DBSAnalysisRunner(String className,
                                   int methodId,
                                   Path outputResultsPath) {
        super(className, methodId, outputResultsPath, new PADO01DifferenceBoundedStateFactory());
    }
}
