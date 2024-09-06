package dev.fmsea.driver;

import dev.fmsea.driver.util.SootInitialization;
import soot.Body;
import soot.SootMethod;

public class PrintJimpleRunner implements Runnable {

    private final String className;
    private final int methodId;
    private final SootMethod sootMethod;
    private final Body body;

    public PrintJimpleRunner(String className, int methodId) {
        this.className = className;
        this.methodId = methodId;
        this.sootMethod = SootInitialization.getSootMethod(this.className, this.methodId);
        this.body = this.sootMethod.retrieveActiveBody();
    }

    public void run() {
        System.out.println(this.body.toString());
    }
}
