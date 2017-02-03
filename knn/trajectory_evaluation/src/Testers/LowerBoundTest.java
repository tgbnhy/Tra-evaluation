/*
 * Decompiled with CFR 0_114.
 */
package Testers;

import IO.CmdParser;
import Launcher.Launch;
import Testers.CompareDistanceRankers;
import distanceRankers.EditDistance;
import index.Cand;
import index.TrajTree;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Random;
import query.TrajScore;
import trajectory.Edge;
import trajectory.Trajectory;

public class LowerBoundTest {
    public static void main(String[] args) throws NumberFormatException, IOException {
        CmdParser parser = new CmdParser(args);
        String fileName = parser.getString("filename");
        int k = parser.getInteger("k", 5);
        double bf = (double)parser.getInteger("bf", 5) / 100.0;
        int vp = parser.getInteger("vp", 100);
        int l = parser.getInteger("minLength", 10);
        ArrayList<Trajectory> trajectories = Launch.readTrajectories(fileName, 2);
        long time = System.currentTimeMillis();
        TrajTree index = new TrajTree(trajectories, bf, vp);
        System.out.println("Indexing Time: " + (System.currentTimeMillis() - time));
        double avg = 0.0;
        double num = 50.0;
        int loop = 0;
        while ((double)loop < num) {
            Random r = new Random();
            int in = r.nextInt(trajectories.size());
            int in1 = new Random().nextInt(trajectories.size());
            int in2 = new Random().nextInt(trajectories.size());
            Trajectory q = trajectories.get(in);
            while (q.edges.size() < l) {
                in = r.nextInt(trajectories.size());
                q = trajectories.get(in);
            }
            System.out.println("Selected query: " + in + " " + q.edges.size());
            TrajScore[] s1 = new TrajScore[TrajTree.db.size()];
            TrajScore[] s2 = new TrajScore[TrajTree.db.size()];
            CompareDistanceRankers cmp = new CompareDistanceRankers();
            PriorityQueue<Cand> c1 = index.bruteTopk(q, trajectories.size());
            EditDistance d = new EditDistance(false);
            double lb = d.getDistance(q, (Trajectory)c1.peek().t)[0];
            Cand[] c2 = index.getVPTopk(q);
            int i = 0;
            double diff = 0.0;
            while (!c1.isEmpty()) {
                Cand c = c1.poll();
                double[] a = new double[]{c.score, -1.0};
                s1[i] = new TrajScore(((Trajectory)c.t).index, a);
                System.out.print(String.valueOf(((Trajectory)c.t).index) + " ");
                c = c2[i];
                System.out.println(String.valueOf(((Trajectory)c.t).index) + " ");
                double[] b = new double[]{c.score, -1.0};
                s2[i++] = new TrajScore(((Trajectory)c.t).index, b);
                double dist = d.getDistance(q, (Trajectory)c.t)[0];
                double d2 = diff = dist > diff ? dist : diff;
            }
            System.out.println("Difference: " + diff / lb);
            System.out.println("--------------------------");
            avg += d.getDistance(trajectories.get(in1), q)[0] / lb;
            ++loop;
        }
        System.out.println("Avg Difference: " + avg / num);
    }
}

