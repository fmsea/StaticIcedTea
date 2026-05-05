package dev.fmsea.tadr;

import soot.Local;

public class Variable extends TADR {
    public final Local variable;

    public Variable(Local variable) {
        this.variable = variable;
    }

    public <R> R accept(TADR.Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
