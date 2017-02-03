/*
 * Decompiled with CFR 0_114.
 */
package trajectory;

import trajectory.STpoint;

public class Edge {
    public double length;
    public STpoint p1;
    public STpoint p2;
    public double speed;

    public Edge(STpoint x, STpoint y) {
        this.p1 = x;
        this.p2 = y;
        this.length = this.p1.euclidean(this.p2);
        this.speed = this.length == 0.0 ? 0.0 : this.length / (this.p2.time - this.p1.time);
        if (this.speed < 0.0) {
            this.speed = 0.0;
        }
    }

    public String toString() {
        return String.valueOf(this.p1.toString()) + ";" + this.p2.toString() + ";" + "Speed: " + this.speed;
    }
}

