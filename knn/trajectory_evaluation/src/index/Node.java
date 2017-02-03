/*
 * Decompiled with CFR 0_114.
 */
package index;

import index.Cand;
import index.TrajTree;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import trajectory.Box;
import trajectory.Edge;
import trajectory.STpoint;
import trajectory.Summary;
import trajectory.Trajectory;

public class Node {
    Summary s;
    STpoint[] vps;
    double[][] fv;
    Node[] children;
    static final double ratio = TrajTree.db.size() / 20;

    public Node() {
        this.s = new Summary(TrajTree.db);
        this.vps = this.createVPs(TrajTree.vps);
        this.fv = new double[this.s.ids.size()][this.vps.length];
        int i = 0;
        while (i < this.s.ids.size()) {
            this.fv[i] = this.getFV(TrajTree.db.get(this.s.ids.get(i)));
            ++i;
        }
        if ((double)this.s.ids.size() > ratio) {
            this.createChildren();
        }
    }

    public Node(Summary sum) {
        this.s = sum;
        this.s.box = this.getBoundingBox();
        this.vps = this.createVPs(TrajTree.vps);
        this.fv = new double[this.s.ids.size()][this.vps.length];
        int i = 0;
        while (i < this.s.ids.size()) {
            this.fv[i] = this.getFV(TrajTree.db.get(this.s.ids.get(i)));
            ++i;
        }
        if ((double)this.s.ids.size() > ratio) {
            this.createChildren();
        }
    }

    private void createChildren() {
        Summary[] sums = this.getSummaries(TrajTree.b);
        this.children = new Node[sums.length];
        int j = 0;
        while (j < sums.length) {
            this.children[j] = new Node(sums[j]);
            ++j;
        }
    }

    private double getMinDist(Trajectory t, STpoint p) {
        double min = Double.MAX_VALUE;
        int i = 0;
        while (i < t.edges.size()) {
            double d = p.euclidean(t.edges.get(i));
            if (d < min) {
                min = d;
            }
            ++i;
        }
        return min;
    }

    private STpoint[] createVPs(int c) {
        Box b = this.getBoundingBox();
        STpoint[] vp = new STpoint[c];
        int i = 0;
        while (i < vp.length) {
            vp[i] = b.samplePoint();
            ++i;
        }
        return vp;
    }

    private Box getBoundingBox() {
        double maxX;
        double minX;
        double maxY = maxX = -2.147483648E9;
        double minY = minX = 2.147483647E9;
        int j = 0;
        while (j < this.s.ids.size()) {
            Trajectory t = TrajTree.db.get(this.s.ids.get(j));
            int i = 0;
            while (i < t.edges.size() + 1) {
                STpoint p = t.getPoint(i);
                if (p.x >= maxX) {
                    maxX = p.x;
                }
                if (p.y >= maxY) {
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
            ++j;
        }
        return new Box(minX, minY, maxX, maxY);
    }

    private Summary[] getSummaries(double b) {
        System.out.println("Creating summaries");
        ArrayList<Integer> centers = this.getCenters(b);
        Summary[] sums = new Summary[centers.size()];
        int i = 0;
        while (i < centers.size()) {
            sums[i] = new Summary(TrajTree.db.get(centers.get(i)));
            ++i;
        }
        i = 0;
        while (i < this.s.ids.size()) {
            Trajectory t = TrajTree.db.get(this.s.ids.get(i));
            if (!centers.contains(i)) {
                double minDist = Double.MAX_VALUE;
                int minI = -1;
                int j = 0;
                while (j < sums.length) {
                    double dist = this.vpDist(this.fv[centers.get(j)], this.fv[i]);
                    if (dist < minDist) {
                        minDist = dist;
                        minI = j;
                    }
                    ++j;
                }
                sums[minI].ids.add(t.index);
            }
            ++i;
        }
        return sums;
    }

    private Summary[] getSummaries() {
        ArrayList<Integer> centers = this.getCenters(10.0);
        Summary[] sums = new Summary[centers.size()];
        int i = 0;
        while (i < sums.length) {
            sums[i] = new Summary(TrajTree.db.get(centers.get(i)));
            ++i;
        }
        i = 0;
        while (i < this.s.ids.size()) {
            Trajectory t = TrajTree.db.get(this.s.ids.get(i));
            if (!centers.contains(i)) {
                Summary best = new Summary();
                double minDist = Double.MAX_VALUE;
                int minI = -1;
                int j = 0;
                while (j < sums.length) {
                    double dist = this.vpDist(this.fv[centers.get(j)], this.fv[i]);
                    if (dist < minDist) {
                        minDist = dist;
                        minI = j;
                    }
                    ++j;
                }
                sums[minI].ids.add(t.index);
            }
            ++i;
        }
        return sums;
    }

    private ArrayList<Integer> getCenters(double b) {
        double prevDist = Double.MAX_VALUE;
        if (this.s.ids.size() < 10) {
            return null;
        }
        System.out.println("FASTMAP");
        ArrayList<Integer> centers = new ArrayList<Integer>();
        int index = (int)(Math.random() * (double)(this.s.ids.size() - 1));
        centers.add(index);
        int windowSize = 20;
        double[] fIndex = new double[2];
        double max = 0.0;
        LinkedList<Double> q = new LinkedList<Double>();
        while (centers.size() <= windowSize || max > 1.0 + b) {
            double h;
            fIndex = this.getFarthest(centers);
            centers.add((int)fIndex[1]);
            double r = prevDist / fIndex[0];
            prevDist = fIndex[0];
            q.add(r);
            if (q.size() > windowSize && (h = ((Double)q.poll()).doubleValue()) == max) {
                max = 0.0;
                Iterator iterator = q.iterator();
                while (iterator.hasNext()) {
                    double d = (Double)iterator.next();
                    double d2 = max = d > max ? d : max;
                }
            }
            if (r > max) {
                max = r;
            }
            System.out.println(String.valueOf(max) + " " + r);
        }
        System.out.println("BF: " + centers.size() + " " + centers);
        return centers;
    }

    private double[] getFarthest(ArrayList<Integer> centers) {
        double max = 0.0;
        double maxID = -1.0;
        int i = 0;
        while (i < this.s.ids.size()) {
            double min = Double.MAX_VALUE;
            Trajectory t1 = TrajTree.db.get(this.s.ids.get(i));
            int j = 0;
            while (j < centers.size()) {
                Trajectory t2 = TrajTree.db.get(this.s.ids.get(centers.get(j)));
                double dist = centers.get(j) == i ? 0.0 : this.vpDist(this.fv[i], this.fv[centers.get(j)]);
                if (dist < min) {
                    min = dist;
                }
                ++j;
            }
            if (min >= max) {
                max = min;
                maxID = i;
            }
            ++i;
        }
        double[] ans = new double[]{max, maxID};
        return ans;
    }

    private double joinArea(Box box, Trajectory t) {
        double x1 = box.x1;
        double x2 = box.x2;
        double y1 = box.y1;
        double y2 = box.y2;
        int i = 0;
        while (i < t.edges.size() + 1) {
            STpoint p = t.getPoint(i);
            if (p.x >= x2) {
                x2 = p.x;
            }
            if (p.y >= y2) {
                y2 = p.y;
            }
            if (p.x < x1) {
                x1 = p.x;
            }
            if (p.y < y1) {
                y1 = p.y;
            }
            ++i;
        }
        double width = x2 - x1;
        double height = y2 - y1;
        return width * height;
    }

    PriorityQueue<Cand> getVPTopk(Trajectory q, int k, HashSet<Integer> added) {
        PriorityQueue<Cand> pq = new PriorityQueue<Cand>();
        double[] qvp = this.getFV(q);
        int i = 0;
        while (i < this.fv.length) {
            Trajectory t = TrajTree.db.get(this.s.ids.get(i));
            if (!added.contains(t.index)) {
                double dist = this.vpDist(this.fv[i], qvp);
                if (pq.size() < k || dist < pq.peek().score) {
                    pq.add(new Cand(t, dist));
                }
                if (pq.size() == k + 1) {
                    pq.poll();
                }
            }
            ++i;
        }
        return pq;
    }

    double vpDist(double[] fv, double[] qvp) {
        double d = 0.0;
        int j = 0;
        while (j < fv.length) {
            d = Math.max(fv[j], qvp[j]) == 0.0 ? (d += 0.0) : (d += 1.0 - Math.min(fv[j], qvp[j]) / Math.max(fv[j], qvp[j]));
            ++j;
        }
        return d;
    }

    double[] getFV(Trajectory q) {
        double[] fv = new double[TrajTree.vps];
        int j = 0;
        while (j < this.vps.length) {
            fv[j] = this.getMinDist(q, this.vps[j]);
            ++j;
        }
        return fv;
    }
}

