/*
 * Decompiled with CFR 0_114.
 */
package trajectory;

import java.util.ArrayList;
import java.util.Random;
import trajectory.Edge;
import trajectory.Point;
import trajectory.STpoint;
import trajectory.Trajectory;

public class Box
implements Point {
    public double x1;
    public double y1;
    public double x2;
    public double y2;
    public double width;
    public double height;

    public Box(STpoint p1) {
        this.x1 = p1.x;
        this.y1 = p1.y;
        this.x2 = this.x1;
        this.y2 = this.y1;
        this.width = 0.0;
        this.height = 0.0;
    }

    public Box() {
        this.y2 = this.x2 = -2.147483648E9;
        this.y1 = this.x1 = 2.147483647E9;
    }

    public Box(ArrayList<Trajectory> db) {
        double minX;
        double maxX;
        double maxY = maxX = -2.147483648E9;
        double minY = minX = 2.147483647E9;
        for (Trajectory t : db) {
            int i = 0;
            while (i < t.edges.size() + 1) {
                STpoint p = t.getPoint(i);
                if (p.x > maxX) {
                    maxX = p.x;
                }
                if (p.y > maxY) {
                    maxY = p.y;
                }
                if (p.x < minX) {
                    minX = p.x;
                }
                if (p.y < minY) {
                    minY = p.y;
                }
                ++i;
            }
        }
        this.x1 = minX;
        this.y1 = minY;
        this.x2 = maxX;
        this.y2 = maxY;
        this.width = this.x2 - this.x1;
        this.height = this.y2 - this.y1;
    }

    public String toString() {
        return "(" + this.x1 + " " + this.y1 + ")" + "(" + this.x2 + " " + this.y2 + ")";
    }

    public Box(double minx, double miny, double maxx, double maxy) {
        this.x1 = minx;
        this.y1 = miny;
        this.x2 = maxx;
        this.y2 = maxy;
        this.width = this.x2 - this.x1;
        this.height = this.y2 - this.y1;
    }

    public Box(Box b, STpoint p) {
        this.x1 = p.x < b.x1 ? p.x : b.x1;
        this.y1 = p.y < b.y1 ? p.y : b.y1;
        this.x2 = p.x > b.x2 ? p.x : b.x2;
        this.y2 = p.y > b.y2 ? p.y : b.y2;
        this.width = this.x2 - this.x1;
        this.height = this.y2 - this.y1;
    }

    public Box(Box b) {
        this.x1 = b.x1;
        this.x2 = b.x2;
        this.y1 = b.y1;
        this.y2 = b.y2;
        this.width = b.width;
        this.height = b.height;
    }

    public Box(Box b1, Box b2) {
        this.x1 = b1.x1 < b2.x1 ? b1.x1 : b2.x1;
        this.y1 = b1.y1 < b2.y1 ? b1.y1 : b2.y1;
        this.x2 = b1.x2 > b2.x2 ? b1.x2 : b2.x2;
        this.y2 = b1.y2 > b2.y2 ? b1.y2 : b2.y2;
        this.width = this.x2 - this.x1;
        this.height = this.y2 - this.y1;
    }

    public Box(STpoint b1, STpoint b2) {
        this.x1 = b1.x < b2.x ? b1.x : b2.x;
        this.y1 = b1.y < b2.y ? b1.y : b2.y;
        this.x2 = b1.x > b2.x ? b1.x : b2.x;
        this.y2 = b1.y > b2.y ? b1.y : b2.y;
        this.width = this.x2 - this.x1;
        this.height = this.y2 - this.y1;
    }

    public boolean equals(Box b) {
        if (this.x1 == b.x1 && this.x2 == b.x2 && this.y1 == b.y1 && this.y2 == b.y2) {
            return true;
        }
        return false;
    }

    public double euclidean(Box p) {
        double dx = (this.x1 > p.x1 ? this.x1 : p.x1) - (this.x2 < p.x2 ? this.x2 : p.x2);
        double dy = (this.y1 > p.y1 ? this.y1 : p.y1) - (this.y2 < p.y2 ? this.y2 : p.y2);
        if (dx < 0.0) {
            return dy > 0.0 ? dy : 0.0;
        }
        if (dy < 0.0) {
            return dx;
        }
        return Math.sqrt(dx * dx + dy * dy);
    }

    public double euclidean(STpoint p) {
        if (p.inBox(this)) {
            return 0.0;
        }
        double dx = (this.x1 > p.x ? this.x1 : p.x) - (this.x2 < p.x ? this.x2 : p.x);
        double dy = (this.y1 > p.y ? this.y1 : p.y) - (this.y2 < p.y ? this.y2 : p.y);
        if (dx < 0.0) {
            return dy > 0.0 ? dy : 0.0;
        }
        if (dy < 0.0) {
            return dx;
        }
        return Math.sqrt(dx * dx + dy * dy);
    }

    public STpoint p4() {
        return new STpoint(this.x2, this.y2, -1.0);
    }

    public STpoint p3() {
        return new STpoint(this.x2, this.y1, -1.0);
    }

    public STpoint p2() {
        return new STpoint(this.x1, this.y2, -1.0);
    }

    public STpoint p1() {
        return new STpoint(this.x1, this.y1, -1.0);
    }

    public double area() {
        return this.width * this.height;
    }

    public void join(STpoint p) {
        this.x1 = p.x < this.x1 ? p.x : this.x1;
        this.y1 = p.y < this.y1 ? p.y : this.y1;
        this.x2 = p.x > this.x2 ? p.x : this.x2;
        this.y2 = p.y > this.y2 ? p.y : this.y2;
        this.width = this.x2 - this.x1;
        this.height = this.y2 - this.y1;
    }

    public void join(Trajectory t) {
        int i = 0;
        while (i < t.edges.size() + 1) {
            STpoint p = t.getPoint(i);
            if (p.x >= this.x2) {
                this.x2 = p.x;
            }
            if (p.y >= this.y2) {
                this.y2 = p.y;
            }
            if (p.x < this.x1) {
                this.x1 = p.x;
            }
            if (p.y < this.y1) {
                this.y1 = p.y;
            }
            ++i;
        }
        this.width = this.x2 - this.x1;
        this.height = this.y2 - this.y1;
    }

    @Override
    public double euclidean(Point p) {
        if (p instanceof STpoint) {
            return this.euclidean((STpoint)p);
        }
        return this.euclidean((Box)p);
    }

    public STpoint samplePoint() {
        Random r = new Random();
        if (this.width == 0.0) {
            this.width = 1.0;
        }
        if (this.height == 0.0) {
            this.height = 1.0;
        }
        return new STpoint((double)r.nextInt((int)this.width) + this.x1, (double)r.nextInt((int)this.height) + this.y1, -1.0);
    }
}

