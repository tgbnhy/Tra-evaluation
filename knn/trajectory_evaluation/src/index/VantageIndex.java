/*
 * Decompiled with CFR 0_114.
 */
package index;

import IO.CmdParser;
import Launcher.Launch;
import distanceRankers.EditDistance;
import index.Cand;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Random;
import trajectory.Box;
import trajectory.Edge;
import trajectory.STpoint;
import trajectory.Trajectory;

public class VantageIndex {
    ArrayList<Trajectory> db;
    double[][] fv;

    public VantageIndex(ArrayList<Trajectory> trajectories, int c) {
        this.db = trajectories;
        STpoint[] vp = this.createVPs(c);
        this.fv = new double[this.db.size()][vp.length];
        int i = 0;
        while (i < this.db.size()) {
            int j = 0;
            while (j < vp.length) {
                this.fv[i][j] = this.getMinDist(this.db.get(i), vp[j]);
                ++j;
            }
            ++i;
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
        for (Trajectory t : this.db) {
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
        System.out.println(minY);
        System.out.println(minX);
        System.out.println(maxY);
        System.out.println(maxX);
        return new Box(minX, minY, maxX, maxY);
    }

    private PriorityQueue<Cand> bruteTopk(Trajectory q, int k) {
        PriorityQueue<Cand> pq = new PriorityQueue<Cand>();
        EditDistance d = new EditDistance();
        for (Trajectory t : this.db) {
            double dist = d.getDistance(t, q)[0];
            if (pq.size() < k || dist < pq.peek().score) {
                pq.add(new Cand(t, dist));
            }
            if (pq.size() != k + 1) continue;
            pq.poll();
        }
        return pq;
    }

    private PriorityQueue<Cand> topk(int in, int k) {
        PriorityQueue<Cand> pq = new PriorityQueue<Cand>();
        ArrayList<Trajectory> topk = new ArrayList<Trajectory>();
        k *= 5;
        int i = 0;
        while (i < this.db.size()) {
            double dist = this.vpDist(in, i);
            if (pq.size() < k || dist < ((Cand)pq.peek()).score) {
                pq.add(new Cand(this.db.get(i), dist));
            }
            if (pq.size() == k + 1) {
                pq.poll();
            }
            ++i;
        }
        while (!pq.isEmpty()) {
            topk.add((Trajectory)((Cand)pq.poll()).t);
        }
        VantageIndex index = new VantageIndex(topk, this.fv[0].length);
        pq = new PriorityQueue();
        k /= 5;
        int i2 = 0;
        while (i2 < index.db.size()) {
            double dist = index.vpDist(topk.size() - 1, i2);
            if (pq.size() < k || dist < pq.peek().score) {
                pq.add(new Cand(index.db.get(i2), dist));
            }
            if (pq.size() == k + 1) {
                pq.poll();
            }
            ++i2;
        }
        return pq;
    }

    private double vpDist(int in, int t) {
        double d = 0.0;
        int j = 0;
        while (j < this.fv[t].length) {
            d += 1.0 - Math.min(this.fv[t][j], this.fv[in][j]) / Math.max(this.fv[t][j], this.fv[in][j]);
            ++j;
        }
        return d;
    }

    public static void main(String[] args) throws NumberFormatException, IOException {
        CmdParser parser = new CmdParser(args);
        String fileName = parser.getString("filename");
        int k = parser.getInteger("k", 5);
        int n = parser.getInteger("c", 100);
        int l = parser.getInteger("minLength", 2);
        ArrayList<Trajectory> trajectories = Launch.readTrajectories(fileName, 2);
        Random r = new Random();
        int in = r.nextInt(trajectories.size());
        VantageIndex index = new VantageIndex(trajectories, n);
        long time = System.currentTimeMillis();
        PriorityQueue<Cand> pq = index.bruteTopk(index.db.get(in), k);
        int i = k;
        while (!pq.isEmpty()) {
            Cand c = pq.poll();
            System.out.println(String.valueOf(i--) + ". " + c + " " + index.vpDist(((Trajectory)c.t).index, in));
        }
        System.out.println("Time: " + (System.currentTimeMillis() - time));
        System.out.println("---------------");
        time = System.currentTimeMillis();
        pq = index.topk(in, k);
        i = k;
        EditDistance d = new EditDistance(false);
        Object sum = null;
        while (!pq.isEmpty()) {
            Cand c = pq.poll();
            Trajectory t = (Trajectory)c.t;
            System.out.println(String.valueOf(i--) + ". " + c + " " + d.getDistance(t, index.db.get(in))[0]);
        }
        System.out.println("Time: " + (System.currentTimeMillis() - time));
    }

    private double max(int in, int t) {
        double d = 0.0;
        int j = 0;
        while (j < this.fv[t].length) {
            if (Math.abs(this.fv[t][j] - this.fv[in][j]) > d) {
                d = Math.abs(this.fv[t][j] - this.fv[in][j]);
            }
            ++j;
        }
        return d;
    }
}

