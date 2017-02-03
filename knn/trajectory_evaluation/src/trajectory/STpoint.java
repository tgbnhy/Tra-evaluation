/*
 * Decompiled with CFR 0_114.
 */
package trajectory;

import trajectory.Box;
import trajectory.Edge;
import trajectory.Point;

public class STpoint
implements Point {
    public double x;
    public double y;
    public double time;

    public STpoint(double x, double y, double time) {
        this.x = x;
        this.y = y;
        this.time = time;
        if (this.time == -1.0) {
            this.time = 0.0;
        }
    }

    public boolean equals(STpoint p) {
        if (this.x == p.x && this.y == p.y) {
            return true;
        }
        return false;
    }

    public STpoint(String pt) {

        String[] p = pt.split(",");
        this.x = new Double(p[0]);
        this.y = new Double(p[1]);
        this.time = new Double(p[2]);
        if (this.time == -1.0) {
            this.time = 0.0;
        }
    }

    public STpoint(STpoint p1) {
        this.x = p1.x;
        this.y = p1.y;
        this.time = p1.time;
    }

    public String toString() {
        String str = "(" + this.x + "," + this.y + "," + this.time + ")";
        return str;
    }

    public double euclidean(STpoint point2) {
        return new Double(Math.sqrt((this.x - point2.x) * (this.x - point2.x) + (this.y - point2.y) * (this.y - point2.y)));
    }

    public double euclidean(Box box) {
        return box.euclidean(this);
    }

    @Override
    public double euclidean(Point p) {
        if (p instanceof STpoint) {
            return this.euclidean((STpoint)p);
        }
        return this.euclidean((Box)p);
    }

    public double euclidean(Edge e) {
        STpoint point1 = e.p1;
        STpoint point2 = e.p2;
        double l2 = (point1.x - point2.x) * (point1.x - point2.x) + (point1.y - point2.y) * (point1.y - point2.y);
        if (l2 == 0.0) {
            return this.euclidean(point1);
        }
        double t = ((this.x - point1.x) * (point2.x - point1.x) + (this.y - point1.y) * (point2.y - point1.y)) / l2;
        if (t < 0.0) {
            return this.euclidean(point1);
        }
        if (t > 1.0) {
            return this.euclidean(point2);
        }
        return new STpoint(point1.x + t * (point2.x - point1.x), point1.y + t * (point2.y - point1.y), point1.time).euclidean(this);
    }

    public boolean inBox(Box b) {
        if (this.x >= b.x1 && this.x <= b.x2 && this.y >= b.y1 && this.y <= b.y2) {
            return true;
        }
        return false;
    }
}

