package org.robovm.debugger.utils;

/**
 * Another Pair implementation (to remove dependencies to other libs)
 */
public class Pair<L, R> {
    public final L left;
    public final R right;

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }
}
