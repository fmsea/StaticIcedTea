package driver.util;

import java.io.File;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SootInitialization {

    private static final Logger LOGGER = LoggerFactory.getLogger(SootInitialization.class);

    public static SootMethod initializeSoot(String className, int methodId, String classpath) {
        LOGGER.debug("Initializing soot... [className={}, classpath={}]",
                     className, classpath);

        Scene.v().setSootClassPath(Scene.v().getSootClassPath() +
                                   File.pathSeparator +
                                   System.getProperty("java.class.path") +
                                   File.pathSeparator +
                                   classpath);

        SootClass sClass = Scene.v().loadClassAndSupport(className);
        sClass.setApplicationClass();
        Scene.v().loadNecessaryClasses();

        SootMethod method = sClass.getMethods().get(methodId);
        return method;
    }
}
