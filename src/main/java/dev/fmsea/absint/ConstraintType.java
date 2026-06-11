package dev.fmsea.absint;

public enum ConstraintType {
    CONSTANT,
    INTERVAL,
    ZONAL,
    OCTAGONAL;

    public String toString() {
        switch (this) {
            case CONSTANT:
                return "CONSTANT";
            case INTERVAL:
                return "INTERVAL";
            case ZONAL:
                return "ZONAL";
            case OCTAGONAL:
                return "OCTAGONAL";
            default:
                return "UNKNOWN";
        }
    }
}
