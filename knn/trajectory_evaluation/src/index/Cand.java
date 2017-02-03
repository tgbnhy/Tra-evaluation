/*
 * Decompiled with CFR 0_114.
 */
package index;

import trajectory.Trajectory;

public class Cand
implements Comparable<Cand> {
    public double score;
    public Object t;

    public Cand(Object t, double dist) {
        this.score = dist;
        this.t = t;
    }

    public String toString() {
        if (this.t instanceof Trajectory) {
            return String.valueOf(((Trajectory)this.t).trajID) + ": " + this.score;
        }
        return "Summary " + this.score;
    }

    @Override
    public int compareTo(Cand c) {
        if (this.score < c.score) {
            return 1;
        }
        if (this.score > c.score) {
            return -1;
        }
        return 0;
    }
}

