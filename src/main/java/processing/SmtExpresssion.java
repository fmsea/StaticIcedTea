package processing;

import java.util.Set;

class SmtExpression {
    String identifier;
    Set<String> identifiers;
    String expression;

    public SmtExpression(String identifier,
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
}
