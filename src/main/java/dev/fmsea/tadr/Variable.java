package dev.fmsea.tadr;

import soot.Local;

public class Variable extends TADR {
    public final Local variable;

    public Variable(Local variable) {
        this.variable = variable;
    }

    public String toSmt() {
        return this.variable.toString();
    }

    public <R> R accept(TADR.Visitor<R> visitor) {
        return visitor.visitVariable(this);
    }
}
