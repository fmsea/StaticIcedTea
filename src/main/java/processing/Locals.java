package processing;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import soot.Local;
import soot.IntType;
import soot.jimple.Jimple;

public class Locals {
    private static Map<String, Local> locals = new HashMap<>();

    private Locals() {
    }

    public static Local get(String id) {
        locals.putIfAbsent(id, Jimple.v().newLocal(id, IntType.v()));
        return locals.get(id);
    }

    public static Collection<Local> values() {
        return Collections.unmodifiableCollection(locals.values());
    }
}
