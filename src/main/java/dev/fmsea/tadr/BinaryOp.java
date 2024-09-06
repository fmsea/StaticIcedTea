package dev.fmsea.tadr;

public abstract class BinaryOp extends TADR {
    public final TADR left;
    public final TADR right;

    public BinaryOp(TADR left, TADR right) {
        this.left = left;
        this.right = right;
    }
}
