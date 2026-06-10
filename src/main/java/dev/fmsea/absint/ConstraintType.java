package dev.fmsea.absint;

public enum ConstraintType {
    INTERVAL,
    ZONAL,
    OCTAGONAL;

    public String toString() {
        switch (this) {
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
