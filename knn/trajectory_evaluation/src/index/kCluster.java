/*
 * Decompiled with CFR 0_114.
 */
package index;

import IO.CmdParser;
import Launcher.Launch;
import distanceRankers.EditDistance;
import distanceRankers.Matrix;
import index.Cand;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import trajectory.Box;
import trajectory.BoxEdge;
import trajectory.Edge;
import trajectory.STpoint;
import trajectory.Summary;
import trajectory.Trajectory;

public class kCluster {
    Summary[] sums;
    ArrayList<Trajectory> db;

    public void index(int k) {
        this.sums = new Summary[k];
        HashSet<Integer> centers = new HashSet<Integer>();
        int i = 0;
        while (i < k) {
            int index = (int)(Math.random() * (double)(this.db.size() - 1));
            while (centers.contains(index)) {
                index = (int)(Math.random() * (double)(this.db.size() - 1));
            }
            this.sums[i] = new Summary(this.db.get(index));
            centers.add(index);
            ++i;
        }
        i = 0;
        while (i < this.db.size()) {
            if (!centers.contains(i)) {
                Trajectory t = this.db.get(i);
                Summary best = new Summary();
                int minI = -1;
                int j = 0;
                while (j < this.sums.length) {
                    EditDistance ed = new EditDistance();
                    ed.getDistance(this.sums[j], t);
                    ArrayList<BoxEdge> e1 = new ArrayList<BoxEdge>();
                    ArrayList<Edge> e2 = new ArrayList<Edge>();
                    Summary cand = this.sums[j].join(t, ed.matrix, ed.matrix.numRows() - 1, ed.matrix.numCols() - 1, e1, e2, new LinkedList<Box>(), false);
                    if (cand.area < best.area) {
                        best = cand;
                        minI = j;
                    }
                    ++j;
                }
                this.sums[minI] = best;
            }
            ++i;
        }
    }

    public kCluster(ArrayList<Trajectory> trajectories) {
        this.db = trajectories;
    }

    public PriorityQueue<Cand> topk(Trajectory q, int k) {
        long time = System.currentTimeMillis();
        PriorityQueue<Cand> pq = new PriorityQueue<Cand>();
        Cand[] clus = new Cand[this.sums.length];
        EditDistance d = new EditDistance();
        int i = 0;
        while (i < this.sums.length) {
            double dist = d.getSubDistance(this.sums[i], q)[0];
            clus[i] = new Cand(this.sums[i], - dist);
            System.out.println("Area: " + this.sums[i].area + " " + clus[i]);
            ++i;
        }
        Arrays.sort(clus);
        System.out.println("Index search time: " + (System.currentTimeMillis() - time));
        int searched = 0;
        int skipped = 0;
        int j = 0;
        while (j < clus.length) {
            if (pq.size() == k && ((Cand)pq.peek()).score < - clus[j].score) {
                skipped += ((Summary)clus[j].t).ids.size();
            } else {
                Summary sum = (Summary)clus[j].t;
                int i2 = 0;
                while (i2 < sum.ids.size()) {
                    double bound = pq.size() < k ? Double.MAX_VALUE : ((Cand)pq.peek()).score;
                    Trajectory t = this.db.get(sum.ids.get(i2));
                    double dist = d.getDistance(t, q)[0];
                    ++searched;
                    if (dist < bound) {
                        pq.add(new Cand(t, dist));
                    }
                    if (pq.size() == k + 1) {
                        pq.poll();
                    }
                    ++i2;
                }
            }
            ++j;
        }
        System.out.println("Searched: " + searched + " out of " + this.db.size());
        System.out.println("Skipped: " + skipped + " out of " + this.db.size());
        return pq;
    }

    private double lbDist(Trajectory q, Trajectory t) {
        double d = q.getPoint(0).euclidean(t.getPoint(0)) * Math.min(t.edgeLength(0), q.edgeLength(0));
        return d += q.getPoint(q.edges.size()).euclidean(t.getPoint(t.edges.size())) * Math.min(t.edgeLength(t.edges.size() - 1), q.edgeLength(q.edges.size() - 1));
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

    public static void main(String[] args) throws NumberFormatException, IOException {
        CmdParser parser = new CmdParser(args);
        String fileName = parser.getString("filename");
        int k = parser.getInteger("k", 5);
        int clus = parser.getInteger("c", 50);
        int in = 1;
        int l = parser.getInteger("minLength", 2);
        ArrayList<Trajectory> trajectories = Launch.readTrajectories(fileName, 2);
        kCluster index = new kCluster(trajectories);
        long time = System.currentTimeMillis();
        PriorityQueue<Cand> pq = index.bruteTopk(index.db.get(in), k);
        for (Cand c2 : pq) {
            System.out.println(c2);
        }
        System.out.println("Time: " + (System.currentTimeMillis() - time));
        index.index(clus);
        time = System.currentTimeMillis();
        pq = index.topk(index.db.get(in), k);
        System.out.println("Time: " + (System.currentTimeMillis() - time));
        for (Cand c2 : pq) {
            System.out.println(c2);
        }
    }
}

