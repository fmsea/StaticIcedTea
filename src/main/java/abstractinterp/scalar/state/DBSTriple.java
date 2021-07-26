package abstractinterp.scalar.state;

import soot.Local;

public class DBSTriple implements Comparable<DBSTriple> {
    public final Local source;
    public final Local target;
    public final Constraint constraint;

    public DBSTriple(Local source, Local target, Constraint constraint) {
        this.source = source;
        this.target = target;
        this.constraint = constraint;
    }

    public int compareTo(DBSTriple other) {
        return this.source.toString().compareTo(other.toString());
    }

    @Override
    public String toString() {
        return String.format("%s - %s ≺ %s",
                             this.source,
                             this.target,
                             this.constraint);
    }
}
