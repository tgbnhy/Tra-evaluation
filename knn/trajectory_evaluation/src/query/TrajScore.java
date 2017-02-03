/*
 * Decompiled with CFR 0_114.
 */
package query;

public class TrajScore
implements Comparable {
    public int index;
    public double[] score;

    public TrajScore(int index, double[] score) {
        this.index = index;
        this.score = score;
    }

    public String toString() {
        return "Index: " + this.index + " Spatial Score: " + this.score[0] + " Temporal Score: " + this.score[1];
    }

    public int compareTo(Object o) {
        TrajScore s = (TrajScore)o;
        if (this.score[0] < s.score[0]) {
            return 1;
        }
        if (this.score[0] > s.score[0]) {
            return -1;
        }
        if (this.score[1] < s.score[1]) {
            return 1;
        }
        if (this.score[1] > s.score[1]) {
            return -1;
        }
        return 0;
    }
}

