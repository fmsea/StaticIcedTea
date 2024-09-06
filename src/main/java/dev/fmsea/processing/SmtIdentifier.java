package dev.fmsea.processing;

import java.util.Set;
import soot.Local;

class SmtIdentifier implements Comparable<SmtIdentifier> {
    final Local identifier;
    final boolean branchOut;

    public SmtIdentifier(Local identifier) {
        this(identifier, false);
    }

    public SmtIdentifier(Local identifier, boolean branchOut) {
        this.identifier = identifier;
        this.branchOut = branchOut;
    }

    @Override
    public String toString() {
        return String.format("%s%s",
                             this.identifier.toString(),
                             this.branchOut ? "f" : "");
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SmtIdentifier) {
            return this.equals((SmtIdentifier) o);
        } else {
            return false;
        }
    }

    public boolean equals(SmtIdentifier o) {
        return (this.identifier.toString().equals(o.identifier.toString()) &&
                this.branchOut == o.branchOut);
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + this.identifier.hashCode();
        result = prime * result + (this.branchOut ? 0 : 1);
        return result;
    }

    public int compareTo(SmtIdentifier o) {
        if (this.branchOut == o.branchOut) {
            return this.identifier.toString().compareTo(o.identifier.toString());
        } else if (this.branchOut && !o.branchOut) {
            return 1;
        } else if (!this.branchOut && o.branchOut) {
            return -1;
        }
        return 0;
    }
}
