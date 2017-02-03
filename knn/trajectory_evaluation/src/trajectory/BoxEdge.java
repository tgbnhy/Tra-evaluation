/*
 * Decompiled with CFR 0_114.
 */
package trajectory;

import trajectory.Box;
import trajectory.Point;
import trajectory.STpoint;

public class BoxEdge {
    public double length;
    public Box p1;
    public Box p2;

    public BoxEdge(Box x, Box y) {
        this.p1 = x;
        this.p2 = y;
        this.length = this.p1.euclidean(this.p2);
    }

    public BoxEdge(Point p, Box b) {
        Box b1 = p instanceof STpoint ? new Box((STpoint)p) : (Box)p;
        this.p1 = b1;
        this.p2 = b;
        this.length = this.p1.euclidean(this.p2);
    }

    public double getArea() {
        STpoint s4;
        STpoint s2;
        double base;
        STpoint s3;
        STpoint s1;
        if (this.p1.area() == 0.0) {
            return this.p2.area();
        }
        if (this.p2.area() == 0.0) {
            return this.p1.area();
        }
        double extra = 0.0;
        if (this.p1.x1 < this.p2.x1) {
            if (this.p1.y1 < this.p2.y1) {
                s2 = this.p2.p2();
                base = this.p2.y2 - this.p1.y2;
                if (base == 0.0) {
                    base = 1.0;
                }
                s1 = new STpoint(this.p1.x1 - this.p1.height * (this.p2.x1 - this.p1.x1) / base, this.p1.y1, -1.0);
            } else {
                base = this.p2.y1 - this.p1.y1;
                if (base == 0.0) {
                    base = 1.0;
                }
                s2 = new STpoint(this.p1.x1 - this.p1.height * (this.p2.x1 - this.p1.x1) / base, this.p1.y2, -1.0);
                s1 = this.p2.p1();
            }
        } else if (this.p1.y1 < this.p2.y1) {
            base = this.p2.y1 - this.p1.y1;
            if (base == 0.0) {
                base = 1.0;
            }
            s2 = new STpoint(this.p2.x1 - this.p2.height * (this.p2.x1 - this.p1.x1) / base, this.p2.y2, -1.0);
            s1 = this.p1.p1();
        } else {
            s2 = this.p1.p2();
            base = this.p2.y2 - this.p1.y2;
            if (base == 0.0) {
                base = 1.0;
            }
            s1 = new STpoint(this.p2.x1 - this.p2.height * (this.p2.x1 - this.p1.x1) / base, this.p2.y2, -1.0);
            extra += 0.5 * this.p2.height * (this.p2.height * (this.p2.x1 - this.p1.x1) / base);
        }
        if (this.p1.x2 > this.p2.x2) {
            if (this.p1.y1 < this.p2.y1) {
                s4 = this.p2.p4();
                base = this.p1.y2 - this.p2.y2;
                if (base == 0.0) {
                    base = 1.0;
                }
                s3 = new STpoint(this.p1.x2 + this.p1.height * (this.p1.x2 - this.p2.x2) / base, this.p1.y1, -1.0);
            } else {
                s3 = this.p2.p3();
                base = this.p1.y1 - this.p2.y1;
                if (base == 0.0) {
                    base = 1.0;
                }
                s4 = new STpoint(this.p1.x2 + this.p1.height * (this.p1.x2 - this.p2.x2) / base, this.p1.y2, -1.0);
            }
        } else if (this.p1.y1 < this.p2.y1) {
            s3 = this.p1.p3();
            base = this.p2.y1 - this.p1.y1;
            if (base == 0.0) {
                base = 1.0;
            }
            s4 = new STpoint(this.p2.x2 + this.p2.height * (this.p2.x2 - this.p1.x2) / base, this.p2.y2, -1.0);
        } else {
            s4 = this.p1.p4();
            base = this.p1.y2 - this.p2.y2;
            if (base == 0.0) {
                base = 1.0;
            }
            s3 = new STpoint(this.p2.x2 + this.p2.height * (this.p1.x2 - this.p2.x2) / base, this.p2.y1, -1.0);
        }
        return this.trapArea(s1, s2, s3, s4) / 2.0;
    }

    private double trapArea(STpoint s1, STpoint s2, STpoint s3, STpoint s4) {
        return 0.5 * (Math.abs(s3.x - s1.x) + Math.abs(s4.x - s2.x)) * Math.abs(s2.y - s1.y);
    }
}

