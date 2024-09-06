package dev.fmsea.absint.scalar.state;

import soot.IntType;
import soot.Local;
import soot.jimple.Jimple;

public class Variable {

    // it's given a weird name to ensure it sorts first, lexigraphically.
    public static final Local ZERO = Jimple.v().newLocal("!$ZERO0", IntType.v());

    private Variable() {
    }
}
