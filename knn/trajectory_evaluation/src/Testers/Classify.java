/*
 * Decompiled with CFR 0_114.
 */
package Testers;

import Utilities.Stats;
import distanceRankers.EDR;
import distanceRankers.EditDistance;
import distanceRankers.LCSS;
import distanceRankers.TrajectoryDistance;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import trajectory.Date;
import trajectory.STpoint;
import trajectory.Trajectory;

public class Classify {
    HashMap<String, ArrayList<Trajectory>> trainset = new HashMap();
    HashMap<String, ArrayList<Trajectory>> testset = new HashMap();
    TrajectoryDistance d;

    public Classify(String fname) throws FileNotFoundException {
        File dir = new File("EDwP-data.txt");
        String[] chld = dir.list();
        if (chld == null) {
            System.out.println("Specified directory does not exist or is not a directory.");
            System.exit(0);
        } else {
            boolean c = false;
            String[] arrstring = chld;
            int n = arrstring.length;
            int n2 = 0;
            while (n2 < n) {
                String folder = arrstring[n2];
                if (new Random().nextDouble() <= 0.25) {
                    System.out.println(folder);
                    File[] arrfile = new File(String.valueOf(fname) + "/" + folder).listFiles();
                    int n3 = arrfile.length;
                    int n4 = 0;
                    while (n4 < n3) {
                        File f = arrfile[n4];
                        double r = new Random().nextDouble();
                        if (r < 0.1) {
                            this.addToTestSet(f, folder);
                        } else {
                            this.addtoTrainSet(f, folder);
                        }
                        ++n4;
                    }
                }
                ++n2;
            }
        }
    }

    private void addtoTrainSet(File f, String folder) throws FileNotFoundException {
        Scanner in = new Scanner(f);
        ArrayList<STpoint> points = new ArrayList<STpoint>();
        while (in.hasNext()) {
            String[] parts = in.nextLine().trim().split(",");
            points.add(new STpoint(new Double(parts[0]), new Double(parts[1]), -1.0));
        }
        in.close();
        if (points.size() == 0) {
            return;
        }
        ArrayList db = !this.trainset.containsKey(folder) ? new ArrayList<Trajectory>() : this.trainset.get(folder);
        db.add(new Trajectory(this.trainset.size(), this.trainset.size(), null, points));
        this.trainset.put(folder, db);
    }

    private void addToTestSet(File f, String folder) throws FileNotFoundException {
        Scanner in = new Scanner(f);
        ArrayList<STpoint> points = new ArrayList<STpoint>();
        while (in.hasNext()) {
            String[] parts = in.nextLine().trim().split(",");
            points.add(new STpoint(new Double(parts[0]), new Double(parts[1]), -1.0));
        }
        in.close();
        ArrayList db = !this.testset.containsKey(folder) ? new ArrayList<Trajectory>() : this.testset.get(folder);
        db.add(new Trajectory(this.testset.size(), this.testset.size(), null, points));
        this.testset.put(folder, db);
    }

    public static void main(String[] args) throws FileNotFoundException {
        int i = 0;
        while (i < 100) {
            Classify c = new Classify(args[0]);
            System.out.println(String.valueOf(i) + " " + 0.25 * Stats.getStdDev(c.getTrajectories()));
            c.d = new EDR(new Double(args[1]));
            double accuracy = c.classify(1);
            System.out.println("EDR: " + accuracy);
            c.d = new LCSS(new Double(args[1]));
            accuracy = c.classify(1);
            System.out.println("LCSS: " + accuracy);
            c.d = new EditDistance(false);
            accuracy = c.classify(1);
            System.out.println("EDwP: " + accuracy);
            ++i;
        }
    }

    private ArrayList<Trajectory> getTrajectories() {
        ArrayList<Trajectory> db = new ArrayList<Trajectory>();
        for (String key : this.trainset.keySet()) {
            db.addAll((Collection)this.trainset.get(key));
        }
        return db;
    }

    private double classify(int k) {
        double acc = 0.0;
        int size = 0;
        for (String key : this.testset.keySet()) {
            for (Trajectory t : this.testset.get(key)) {
                ++size;
                if (this.classify(t) != key) continue;
                acc += 1.0;
            }
        }
        return acc / (double)size;
    }

    private String classify(Trajectory q) {
        double min = Double.MAX_VALUE;
        String label = null;
        for (String key : this.trainset.keySet()) {
            for (Trajectory t : this.trainset.get(key)) {
                double dist = this.d.getDistance(t, q)[0];
                if (dist >= min) continue;
                label = key;
                min = dist;
            }
        }
        return label;
    }
}

