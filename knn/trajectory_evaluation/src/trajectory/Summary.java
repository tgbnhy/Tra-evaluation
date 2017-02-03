/*
 * Decompiled with CFR 0_114.
 */
package trajectory;

import Launcher.Launch;
import distanceRankers.EditDistance;
import distanceRankers.Matrix;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import trajectory.Box;
import trajectory.BoxEdge;
import trajectory.Edge;
import trajectory.Point;
import trajectory.STpoint;
import trajectory.Trajectory;

public class Summary {
    public ArrayList<BoxEdge> edges;
    public double length;
    public double area;
    public ArrayList<Integer> ids;
    public Box box;

    public Summary(LinkedList<Box> boxes, int id, ArrayList<Integer> ids) {
        this.edges = new ArrayList();
        this.ids = new ArrayList();
        this.ids.addAll(ids);
        this.ids.add(id);
        this.length = 0.0;
        this.area = 0.0;
        int i = 0;
        while (i < boxes.size() - 1) {
            Box p1 = boxes.get(i);
            Box p2 = boxes.get(i + 1);
            this.edges.add(new BoxEdge(p1, p2));
            this.length += this.edges.get((int)i).length;
            this.area += this.edges.get(i).getArea();
            ++i;
        }
    }

    public Summary(Trajectory trajectory) {
        this.ids = new ArrayList();
        this.edges = new ArrayList();
        this.ids.add(trajectory.index);
        this.length = trajectory.length;
        int i = 0;
        for (Edge e : trajectory.edges) {
            Box b1 = i == 0 ? new Box(e.p1) : this.edges.get((int)(i - 1)).p2;
            BoxEdge be = new BoxEdge(b1, new Box(e.p2));
            this.edges.add(be);
        }
        this.box = new Box();
        this.box.join(trajectory);
    }

    public Summary() {
        this.edges = new ArrayList();
        this.ids = new ArrayList();
        this.length = Double.MAX_VALUE;
        this.area = Double.MAX_VALUE;
    }

    public Summary(ArrayList<Trajectory> db) {
        this.edges = new ArrayList();
        this.ids = new ArrayList();
        this.length = Double.MAX_VALUE;
        this.area = Double.MAX_VALUE;
        int i = 0;
        while (i < db.size()) {
            this.ids.add(db.get((int)i).index);
            ++i;
        }
    }

    public Box getBox(int j) {
        if (j == 0) {
            return this.edges.get((int)0).p1;
        }
        return this.edges.get((int)(j - 1)).p2;
    }

    public double edgeLength(int i) {
        return this.edges.get((int)i).length;
    }

    public static void main(String[] args) {
    }

    public Summary join(Trajectory t, Matrix matrix, int i, int j, ArrayList<BoxEdge> e1, ArrayList<Edge> e2, LinkedList<Box> box, boolean mappedEdge) {
        Object e;
        Point insert;
        if (i == 0 && j == 0) {
            Box b1 = e1.get((int)(e1.size() - 1)).p1;
            Box b2 = e1.get((int)(e1.size() - 1)).p2;
            Box b = null;
            STpoint p1 = e2.get((int)(e2.size() - 1)).p1;
            STpoint p2 = e2.get((int)(e2.size() - 1)).p2;
            b = p1.equals(p2) && !mappedEdge ? new Box(b1) : (b1.equals(b2) && !mappedEdge ? new Box(p1) : new Box(e1.get((int)(e1.size() - 1)).p1, e2.get((int)(e2.size() - 1)).p1));
            b = new Box(e1.get((int)(e1.size() - 1)).p2, e2.get((int)(e2.size() - 1)).p2);
            box.addFirst(b);
            return new Summary(box, t.index, this.ids);
        }
        if (matrix.parent[i][j] == 1) {
            if (e1.size() > 0) {
                e1.add(new BoxEdge(this.edges.get((int)(i - 1)).p1, e1.get((int)(e1.size() - 1)).p1));
                e2.add(new Edge(t.getEdge((int)(j - 1)).p1, e2.get((int)(e2.size() - 1)).p1));
            } else {
                e1.add(this.edges.get(i - 1));
                e2.add(t.getEdge(j - 1));
            }
            --i;
            --j;
        } else if (matrix.parent[i][j] == 2) {
            insert = matrix.rowEdits[i][j];
            e = null;
            e = e1.size() > 0 ? new BoxEdge(insert, e1.get((int)(e1.size() - 1)).p1) : new BoxEdge(insert, this.edges.get((int)(i - 1)).p2);
            e1.add((BoxEdge)e);
            if (e2.size() > 0) {
                e2.add(new Edge(t.getEdge((int)(j - 1)).p1, e2.get((int)(e2.size() - 1)).p1));
            } else {
                e2.add(t.getEdge(j - 1));
            }
            --j;
        } else {
            insert = (STpoint)matrix.colEdits[i][j];
            e = null;
            e = e2.size() > 0 ? new Edge((STpoint)insert, e2.get((int)(e2.size() - 1)).p1) : new Edge((STpoint)insert, t.getEdge((int)(j - 1)).p2);
            e2.add((Edge)e);
            if (e1.size() > 0) {
                e1.add(new BoxEdge(this.edges.get((int)(i - 1)).p1, e1.get((int)(e1.size() - 1)).p1));
            } else {
                e1.add(this.edges.get(i - 1));
            }
            --i;
        }
        Box b1 = e1.get((int)(e1.size() - 1)).p1;
        Box b2 = e1.get((int)(e1.size() - 1)).p2;
        Box b = null;
        STpoint p1 = e2.get((int)(e2.size() - 1)).p1;
        STpoint p2 = e2.get((int)(e2.size() - 1)).p2;
        b = new Box(e1.get((int)(e1.size() - 1)).p2, e2.get((int)(e2.size() - 1)).p2);
        box.addFirst(b);
        if (Launch.DEBUG) {
            System.out.println(e1.get(e1.size() - 1));
            System.out.println(e2.get(e2.size() - 1));
        }
        return this.join(t, matrix, i, j, e1, e2, box, mappedEdge);
    }

    public Summary join(Trajectory t) {
        EditDistance ed = new EditDistance();
        ed.getDistance(this, t);
        ArrayList<BoxEdge> e1 = new ArrayList<BoxEdge>();
        ArrayList<Edge> e2 = new ArrayList<Edge>();
        return this.join(t, ed.matrix, ed.matrix.numRows() - 1, ed.matrix.numCols() - 1, e1, e2, new LinkedList<Box>(), false);
    }

    public String toString() {
        String s = "";
        Box b = null;
        for (BoxEdge e : this.edges) {
            s = String.valueOf(s) + " " + e.p1.toString();
            b = e.p2;
        }
        s = String.valueOf(s) + " " + b.toString();
        return s;
    }

    public double[] distance(Trajectory t) {
        EditDistance d = new EditDistance();
        double[] score = d.getDistance(this, t);
        d.matrix.printPath();
        return score;
    }

    public double[] subDistance(Trajectory t) {
        EditDistance d = new EditDistance();
        double[] score = d.getSubDistance(this, t);
        d.matrix.printPath();
        return score;
    }
}

