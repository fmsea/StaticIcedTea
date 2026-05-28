package dev.fmsea.picotelem.model;

import java.util.Map;

public class SmtUnionEvent implements TEvent {
    public final int leftSuper;
    public final int rightSuper;
    public final int neither;
    public final double proportion;

    public SmtUnionEvent(int leftSuper, int rightSuper, int neither, double proportion) {
        this.leftSuper = leftSuper;
        this.rightSuper = rightSuper;
        this.neither = neither;
        this.proportion = proportion;
    }

    public String getEventName() {
        return "smt.processing.union";
    }

    public Map<String, String> getEventRepr() {
        return Map.of(
            "leftSuper", Integer.valueOf(leftSuper).toString(),
            "rightSuper", Integer.valueOf(rightSuper).toString(),
            "neither", Integer.valueOf(neither).toString(),
            "proportion", Double.valueOf(proportion).toString()
        );
    }
}
