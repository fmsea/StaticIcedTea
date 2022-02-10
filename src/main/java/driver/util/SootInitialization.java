package driver.util;

import java.io.File;
import java.nio.file.Path;
import soot.Body;
import soot.options.Options;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SootInitialization {

    private static final Logger LOGGER = LoggerFactory.getLogger(SootInitialization.class);

    public static void initializeSoot(String className, Path classpath) {
        initializeSoot(new String[] { className, }, classpath);
    }

    public static void initializeSoot(String[] classNames, Path classpath) {
        LOGGER.debug("Initializing soot... [className={}, classpath={}]",
                     classNames, classpath);

        Options.v().set_whole_program(true);
        Scene.v().setSootClassPath(Scene.v().getSootClassPath() +
                                   File.pathSeparator +
                                   System.getProperty("java.class.path") +
                                   File.pathSeparator +
                                   classpath.toAbsolutePath().toString());

        for (int i = 0; i < classNames.length; i++) {
            Scene.v().loadClass(classNames[i], SootClass.SIGNATURES);
        }

        SootClass sClass = Scene.v().getSootClass(classNames[0]);
        sClass.setApplicationClass();
        Scene.v().loadNecessaryClasses();
    }

    public static SootMethod getSootMethod(String className, int methodId) {
        SootClass sClass = Scene.v().getSootClass(className);
        SootMethod method = sClass.getMethods().get(methodId);
        return method;
    }
}
