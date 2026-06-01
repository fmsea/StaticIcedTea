package dev.fmsea.picotelem.model;

import java.util.Set;

import soot.Local;

public enum PrecisionLevel {
    NONRELATIONAL,
    RELATIONAL;

    @Override
    public String toString() {
        switch (this) {
            case NONRELATIONAL:
                return "non-relational";
            case RELATIONAL:
                return "relational";
            default:
                throw new RuntimeException("wat");
        }
    }

    public static PrecisionLevel from(Set<Local> variables) {
        if (variables.size() <= 1) {
            return NONRELATIONAL;
        } else {
            return RELATIONAL;
        }
    }
}
