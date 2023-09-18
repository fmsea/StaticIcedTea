package common;

import java.util.Set;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

    public static Set<Local> get(String... ids) {
        return Stream.of(ids)
            .map(id -> Locals.get(id))
            .collect(Collectors.toSet());
    }

    public static Collection<Local> values() {
        return Collections.unmodifiableCollection(locals.values());
    }
}
