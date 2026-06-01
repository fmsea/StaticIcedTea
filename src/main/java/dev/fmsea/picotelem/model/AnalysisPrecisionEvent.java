package dev.fmsea.picotelem.model;

import java.util.Map;

public class AnalysisPrecisionEvent implements TEvent {
    public final String statement;
    public final PrecisionLevel level;

    public AnalysisPrecisionEvent(String statement, PrecisionLevel level) {
        this.statement = statement;
        this.level = level;
    }

    public String getEventName() {
        return "analysis.precision.change";
    }

    public Map<String, String> getEventRepr() {
        return Map.of(
            "statement", this.statement,
            "precision-level", this.level.toString()
        );
    }
}
