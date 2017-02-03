/*
 * Decompiled with CFR 0_114.
 */
package trajectory;

import java.util.ArrayList;
import trajectory.Date;
import trajectory.Edge;
import trajectory.STpoint;

public class Trajectory {
    public ArrayList<Edge> edges = new ArrayList();
    public int index;
    public int trajID;
    public Date startDate;
    public double length;

    public Trajectory(int index, int trajID, Date startDate, ArrayList<STpoint> points) {
        this.index = index;
        this.trajID = trajID;
        this.startDate = startDate;
        this.length = 0.0;
        int i = 0;
        while (i < points.size() - 1) {
            STpoint p1 = points.get(i);
            STpoint p2 = points.get(i + 1);
            this.edges.add(new Edge(p1, p2));
            this.length += this.edges.get((int)i).length;
            ++i;
        }
    }

    public String toString() {
        String str = String.valueOf(this.index) + " " + this.trajID + " " + this.startDate + " [";
        int i = 0;
        while (i < this.edges.size() + 1) {
            str = String.valueOf(str) + this.getPoint(i).toString() + ";";
            ++i;
        }
        return String.valueOf(str) + "]";
    }

    public Edge getEdge(int i) {
        return this.edges.get(i);
    }

    public double edgeLength(int i) {
        return this.edges.get((int)i).length;
    }

    public String getLabel() {
        return String.valueOf(this.index) + " " + this.trajID;
    }

    public STpoint getPoint(int j) {
        if (j == 0) {
            return this.edges.get((int)0).p1;
        }
        return this.edges.get((int)(j - 1)).p2;
    }
}

