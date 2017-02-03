/*
 * Decompiled with CFR 0_114.
 * 
 * Could not load the following classes:
 *  org.apache.commons.math3.linear.RealMatrix
 */
package Launcher;

import IO.CmdParser;
import Testers.AddNoise;
import Testers.CompareDistanceRankers;
import Utilities.PyPlot;
import Utilities.Stats;
import distanceRankers.DTW;
import distanceRankers.EditDistance;
import distanceRankers.LCSS;
import distanceRankers.TrajectoryDistance;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.math3.linear.RealMatrix;
import query.Topk;
import query.TrajScore;
import trajectory.Date;
import trajectory.Edge;
import trajectory.STpoint;
import trajectory.Trajectory;

public class Launch {
    public static boolean DEBUG = false;
    public static double MIN;

    public static void main(String[] args) throws NumberFormatException, IOException {
        CmdParser parser = new CmdParser(args);
        String fileName = parser.getString("EDwp-data.txt");
        DEBUG = parser.hasOption("debug");
        MIN = parser.getDouble("sThresh", 2.0) * 60.0;
        int sample = parser.getInteger("sample", 100);
        int k = parser.getInteger("k", 5);
        ArrayList<Trajectory> trajectories = Launch.readTrajectories(fileName, parser.getInteger("minLength", 2));
        EditDistance d = new EditDistance();
        if (parser.hasOption("corr")) {
            Launch.testCorrelation(trajectories, sample);
        }
        if (parser.hasOption("plot")) {
            Launch.top1AndPlot(trajectories);
        }
        if (parser.hasOption("symmetry")) {
            Launch.testSymmetry(trajectories);
        }
        if (parser.hasOption("noise")) {
            Launch.testNoiseResistance(trajectories, parser.getDouble("noise", 0.05), parser.getDouble("perturbation", 0.0), sample, k);
        }
        if (parser.hasOption("compute")) {
            double sThresh = parser.getDouble("sThresh", 2.0);
            int t1 = parser.getInteger("t1");
            int t2 = parser.getInteger("t2");
            TrajectoryDistance[] drs = new TrajectoryDistance[]{new EditDistance(), new DTW()};
            CompareDistanceRankers cmp = new CompareDistanceRankers(drs);
            cmp.compareDistance(trajectories.get(t1), trajectories.get(t2));
        }
    }

    private static void testNoiseResistance(ArrayList<Trajectory> trajectories, double quantity, double perturbation, int sampleSize, int k) {
        ArrayList<Trajectory> noisyTrajectories;
        double sThresh = (double)Stats.getAverageSpeed(trajectories) * MIN;
        if (perturbation == 0.0) {
            new Testers.AddNoise();
            noisyTrajectories = AddNoise.insert(trajectories, quantity);
        } else {
            new Testers.AddNoise();
            noisyTrajectories = AddNoise.perturb(trajectories, quantity, (double)Stats.getAverageSpeed(trajectories) * perturbation * 60.0);
        }
        TrajectoryDistance[] d = new TrajectoryDistance[]{new EditDistance(false), new DTW()};
        double[] avg = new double[d.length];
        int i = 0;
        while (i < sampleSize) {
            int index = (int)(Math.random() * (double)(trajectories.size() - 1));
            Trajectory query = trajectories.get(index);
            CompareDistanceRankers cd = new CompareDistanceRankers(d);
            double[] corr = cd.correlationWithNoise(query, trajectories, noisyTrajectories, k);
            int j = 0;
            while (j < corr.length) {
                double[] arrd = avg;
                int n = j;
                arrd[n] = arrd[n] + corr[j] / (double)sampleSize;
                ++j;
            }
            ++i;
        }
        System.out.println(Arrays.toString(avg));
    }

    private static void testCorrelation(ArrayList<Trajectory> trajectories, int sample) {
        int[] kseries;
        double sThresh = Stats.getAverageSpeed(trajectories) * 300.0f;
        TrajectoryDistance[] d = new TrajectoryDistance[]{new EditDistance(), new LCSS(sThresh, Double.MAX_VALUE), new DTW()};
        int n = sample;
        int[] arrn = kseries = new int[]{3, 5, 10, 20, 50, 100};
        int n2 = arrn.length;
        int n3 = 0;
        while (n3 < n2) {
            int k = arrn[n3];
            double[] corr = new double[d.length - 1];
            int i = 0;
            while (i < n) {
                int index = (int)(Math.random() * (double)(trajectories.size() - 1));
                System.out.println(index);
                Trajectory query = trajectories.get(index);
                CompareDistanceRankers cd = new CompareDistanceRankers(d);
                RealMatrix avg = cd.correlationBetweenDistanceRankers(query, trajectories, k);
                int j = 1;
                while (j < d.length) {
                    double[] arrd = corr;
                    int n4 = j - 1;
                    arrd[n4] = arrd[n4] + avg.getEntry(0, j) / (double)n;
                    ++j;
                }
                ++i;
            }
            System.out.println("k=" + k + " " + Arrays.toString(corr));
            ++n3;
        }
    }

    private static void top1AndPlot(ArrayList<Trajectory> trajectories) {
        double sThresh = Stats.getAverageSpeed(trajectories) * 120.0f;
        TrajectoryDistance[] d = new TrajectoryDistance[]{new EditDistance(), new LCSS(sThresh, Double.MAX_VALUE), new DTW()};
        ArrayList<Trajectory> noisyTrajectories = AddNoise.insert(trajectories, 0.25);
        noisyTrajectories = AddNoise.insert(noisyTrajectories, 0.25);
        noisyTrajectories = AddNoise.insert(noisyTrajectories, 0.25);
        noisyTrajectories = AddNoise.insert(noisyTrajectories, 0.25);
        noisyTrajectories = AddNoise.insert(noisyTrajectories, 0.25);
        Topk tk = new Topk(trajectories);
        int i = 0;
        while (i < 5) {
            Trajectory query = noisyTrajectories.get((int)(Math.random() * (double)(noisyTrajectories.size() - 1)));
            System.out.println("Query Index: " + query.trajID);
            PyPlot plot = new PyPlot("pyplot_template.txt");
            plot.addTrajectory(query, "*", "query");
            TrajScore[][] scores = new TrajScore[d.length][];
            int c = 0;
            TrajectoryDistance[] arrtrajectoryDistance = d;
            int n = arrtrajectoryDistance.length;
            int n2 = 0;
            while (n2 < n) {
                TrajectoryDistance dist = arrtrajectoryDistance[n2];
                scores[c] = tk.getTopK(query, dist, 2);
                ++c;
                ++n2;
            }
            if (scores[0][scores[0].length - 1].index == scores[2][scores[2].length - 1].index) {
                --i;
            } else {
                int j = 0;
                while (j < scores.length) {
                    int id = scores[j][scores[j].length - 1].index;
                    System.out.println(String.valueOf(d[j].getName()) + ": " + " " + scores[j][scores[j].length - 1] + " Length: " + trajectories.get((int)id).edges.size());
                    plot.addTrajectory(trajectories.get(id), d[j].getPythonSymbol(), d[j].getName());
                    ++j;
                }
                System.out.println("-----------------------");
                plot.plot();
            }
            ++i;
        }
    }

    private static void testTopk(ArrayList<Trajectory> trajectories) {
        Topk tk = new Topk(trajectories);
        TrajScore[] scores = tk.getTopK(trajectories.get(0), new EditDistance(), 20);
        int i = 0;
        while (i < scores.length) {
            System.out.println(String.valueOf(i) + ". " + scores[i]);
            ++i;
        }
    }

    private static void testExamples(ArrayList<Trajectory> trajectories) {
        TrajectoryDistance[] drs = new TrajectoryDistance[]{new EditDistance(), new LCSS(2.0, 3.4028234663852886E38)};
        CompareDistanceRankers cmp = new CompareDistanceRankers(drs);
        cmp.compareDistance(trajectories.get(0), trajectories.get(1));
        cmp.compareDistance(trajectories.get(2), trajectories.get(3));
        cmp.compareDistance(trajectories.get(0), trajectories.get(4));
    }

    private static void testSymmetry(ArrayList<Trajectory> trajectories) {
        EditDistance distance1 = new EditDistance();
        int i = 0;
        while (i < trajectories.size()) {
            int j = i;
            while (j < trajectories.size()) {
                if (trajectories.get((int)i).edges.size() > 5 && trajectories.get((int)j).edges.size() > 5) {
                    double dist1 = distance1.getDistance(trajectories.get(i), trajectories.get(j))[0];
                    double dist2 = distance1.getDistance(trajectories.get(j), trajectories.get(i))[0];
                    if (i == j && dist1 != 0.0) {
                        System.err.println("ERROR: Distance should be 0 for same trajectories");
                    } else if (dist1 != dist2) {
                        System.err.println("ERROR: Distance should be symmetric for " + i + " - " + dist1 + " " + j + " - " + dist2);
                    }
                }
                ++j;
            }
            ++i;
        }
    }

    public static ArrayList<Trajectory> readTrajectories(String fileName, int minLength) throws NumberFormatException, IOException {
        String line;
        ArrayList<Trajectory> trajectories = new ArrayList<Trajectory>();
        FileInputStream fstream = new FileInputStream("gpsdata-person1.txt");
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        int trajID = 0;
        int index = 0;
        while ((line = br.readLine()) != null) {
            String[] p;
            String[] parts = line.trim().split(" ");
            Date date = new Date(parts[1], parts[2]);
            ArrayList<STpoint> points = new ArrayList<STpoint>();
            String[] arrstring = p = parts[3].trim().substring(0, parts[3].length() - 1).split(";");
            int n = arrstring.length;
            int n2 = 0;
            while (n2 < n) {
                String pt = arrstring[n2]; 
//                ±£´æµÄ¹ì¼£
//                1,9,0
//                2,2,2
//                3,4,3
//                4,5,3.5                
                //System.out.println(pt); 
                if (!p.equals("")) {
                    points.add(new STpoint(pt));
                }
                ++n2;
            }
            //System.out.println();
            if (points.size() >= minLength) {
                trajectories.add(new Trajectory(index++, trajID, date, points));
            }
            ++trajID;
        }
        in.close();
        return trajectories;
    }
}

