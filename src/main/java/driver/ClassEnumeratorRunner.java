package driver;

import java.util.List;
import driver.util.SootInitialization;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassEnumeratorRunner implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassEnumeratorRunner.class);

    private final String className;

    public ClassEnumeratorRunner(String className) {
        this.className = className;
    }

    public void run() {
        LOGGER.info("Enumerating method ids of {}", this.className);
        SootClass sClass = Scene.v().getSootClass(this.className);
        List<SootMethod> methods = sClass.getMethods();
        for (int i = 1; i < methods.size(); i++) {
            System.out.println(String.format("%s\t%d", this.className, i));
        }
    }
}
