/*
 * Decompiled with CFR 0_114.
 */
package index;

import IO.CmdParser;
import Launcher.Launch;
import distanceRankers.EditDistance;
import index.Cand;
import index.Node;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;
import trajectory.Box;
import trajectory.Edge;
import trajectory.Summary;
import trajectory.Trajectory;

public class TrajTree {
    static Node root;
    public static ArrayList<Trajectory> db;
    static double b;
    static int vps;

    public TrajTree(ArrayList<Trajectory> db, double bf, int vps) {
        TrajTree.db = db;
        b = bf;
        TrajTree.vps = vps;
        root = new Node();
    }

    public PriorityQueue<Cand> topk(Trajectory q, int k) {
        PriorityQueue<Cand> acPQ = new PriorityQueue<Cand>();
        PriorityQueue<Cand> candidates = new PriorityQueue<Cand>();
        candidates.add(new Cand(root, 0.0));
        HashSet<Integer> added = new HashSet<Integer>();
        EditDistance dist = new EditDistance();
        Cand[] vpPQ = this.getVPTopk(q);
        int pos = 0;
        while (pos < k) {
            Cand c = vpPQ[pos];
            Trajectory t = (Trajectory)c.t;
            added.add(t.index);
            acPQ.add(new Cand(t, dist.getDistance(q, t)[0]));
            if (acPQ.size() > k) {
                acPQ.poll();
            }
            ++pos;
        }
        double lb = ((Cand)acPQ.peek()).score;
        int pruned = 0;
        int ev1 = 0;
        int ev2 = 0;
        while (!candidates.isEmpty() && - ((Cand)candidates.peek()).score < lb) {
            Node cand = (Node)((Cand)candidates.poll()).t;
            if (cand.children != null) {
                int bsize = 10 * k;
                while (bsize-- > 0 && pos < db.size()) {
                    Cand c = vpPQ[pos++];
                    Trajectory t = (Trajectory)c.t;
                    double d = dist.getDistance(q, t)[0];
                    if (added.contains(t.index) || d >= lb) continue;
                    ++ev2;
                    added.add(t.index);
                    acPQ.add(new Cand(t, d));
                    if (acPQ.size() <= k) continue;
                    acPQ.poll();
                }
                lb = acPQ.peek().score;
                int i = 0;
                while (i < cand.children.length) {
                    double d = dist.getSubDistance(cand.children[i].s.box, q);
                    ++ev1;
                    if (d < lb) {
                        candidates.add(new Cand(cand.children[i], - d));
                    } else {
                        ++pruned;
                    }
                    ++i;
                }
                continue;
            }
            int i = 0;
            while (i < cand.s.ids.size()) {
                Trajectory t = db.get(cand.s.ids.get(i));
                if (!added.contains(t.index)) {
                    added.add(t.index);
                    double d = dist.getDistance(q, t)[0];
                    ++ev2;
                    acPQ.add(new Cand(t, d));
                    if (acPQ.size() > k) {
                        acPQ.poll();
                    }
                }
                ++i;
            }
            lb = acPQ.peek().score;
        }
        System.out.println("Pruned: " + (pruned + candidates.size()) + " " + ev1 + " " + ev2);
        return acPQ;
    }

    public Cand[] getVPTopk(Trajectory q) {
        Object[] pq = new Cand[TrajTree.root.s.ids.size()];
        double[] qvp = root.getFV(q);
        System.out.println("Query fv: " + Arrays.toString(qvp));
        int i = 0;
        while (i < TrajTree.root.fv.length) {
            Trajectory t = db.get(TrajTree.root.s.ids.get(i));
            double dist = root.vpDist(TrajTree.root.fv[i], qvp);
            pq[i] = new Cand(t, - dist);
            ++i;
        }
        Arrays.sort(pq);
        return (Cand[]) pq;
    }

    public PriorityQueue<Cand> bruteTopk(Trajectory q, int k) {
        PriorityQueue<Cand> pq = new PriorityQueue<Cand>();
        EditDistance d = new EditDistance();
        for (Trajectory t : db) {
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
        String fileName = parser.getString("EDwP-data.txt");
        int k = parser.getInteger("k", 5);
        double bf = (double)parser.getInteger("bf", 5) / 100.0;
        int vp = parser.getInteger("vp", 100);
        int l = parser.getInteger("minLength", 10);
        ArrayList<Trajectory> trajectories = Launch.readTrajectories(fileName, 2);
        long time = System.currentTimeMillis();
        TrajTree index = new TrajTree(trajectories, bf, vp);
        System.out.println("Indexing Time: " + (System.currentTimeMillis() - time));
        int loop = 0;
        while (loop < 1) {
            Random r = new Random();
            int in = r.nextInt(trajectories.size());
            Trajectory q = trajectories.get(in);
            while (q.edges.size() < l) {
                in = r.nextInt(trajectories.size());
                q = trajectories.get(in);
            }
            System.out.println("Selected query: " + in + " " + q.edges.size());
            time = System.currentTimeMillis();
            PriorityQueue<Cand> bpq = index.bruteTopk(q, k);
            int i = k;
            System.out.println("Brute Force Time: " + (System.currentTimeMillis() - time));
            time = System.currentTimeMillis();
            PriorityQueue<Cand> pq = index.topk(q, k);
            System.out.println("Querying Time: " + (System.currentTimeMillis() - time));
            i = k;
            while (!pq.isEmpty()) {
                Cand c1 = pq.poll();
                Cand c2 = bpq.poll();
                Trajectory t1 = (Trajectory)c1.t;
                Trajectory t2 = (Trajectory)c2.t;
                if (t1.index != t2.index) {
                    System.out.println("ERROR: " + i + ". " + c1 + " " + c2);
                }
                --i;
            }
            ++loop;
        }
    }
}

