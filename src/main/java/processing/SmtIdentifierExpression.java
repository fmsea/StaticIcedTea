package processing;

import java.util.Set;

class SmtIdentifierExpression implements Comparable<SmtIdentifierExpression> {
    String identifier;
    Set<String> identifiers;
    String expression;

    public SmtIdentifierExpression(String identifier,
                                   Set<String> identifiers,
                                   String expression) {
        this.identifier = identifier;
        this.identifiers = identifiers;
        this.expression = expression;
    }

    @Override
    public String toString() {
        return String.format("%s->%s", this.identifier, this.expression);
    }

    public boolean isBranchOut() {
        return this.identifier.endsWith("f");
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SmtIdentifierExpression) {
            return this.equals((SmtIdentifierExpression) o);
        } else {
            return false;
        }
    }

    public boolean equals(SmtIdentifierExpression o) {
        return (this.identifier.equals(o.identifier) &&
                this.identifiers.equals(o.identifiers) &&
                this.expression.equals(o.expression));
    }

    public int compareTo(SmtIdentifierExpression o) {
        return this.identifier.compareTo(o.identifier);
    }
}
