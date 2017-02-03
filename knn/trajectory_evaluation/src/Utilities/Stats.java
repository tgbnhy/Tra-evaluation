/*
 * Decompiled with CFR 0_114.
 */
package Utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import trajectory.Edge;
import trajectory.STpoint;
import trajectory.Trajectory;

public class Stats {
    public static void main(String[] args) throws NumberFormatException, IOException {
    }

    public static float getAverageSpeed(ArrayList<Trajectory> trajectories) {
        float sum = 0.0f;
        int count = 0;
        for (Trajectory t : trajectories) {
            for (Edge e : t.edges) {
                sum = (float)((double)sum + e.speed * e.length);
                count = (int)((double)count + e.length);
            }
        }
        return sum / (float)count;
    }

    public static double getStdDev(ArrayList<Trajectory> trajectories) {
        ArrayList<Double> x = new ArrayList<Double>();
        ArrayList<Double> y = new ArrayList<Double>();
        for (Trajectory t : trajectories) {
            for (Edge e : t.edges) {
                x.add(e.p1.x);
                y.add(e.p1.y);
            }
        }
        return Math.max(Stats.stdDev(x), Stats.stdDev(y));
    }

    private static double stdDev(ArrayList<Double> x) {
        double d;
        double avg = 0.0;
        double stddev = 0.0;
        Iterator<Double> iterator = x.iterator();
        while (iterator.hasNext()) {
            d = iterator.next();
            avg += d / (double)x.size();
        }
        iterator = x.iterator();
        while (iterator.hasNext()) {
            d = iterator.next();
            stddev += (d - avg) * (d - avg);
        }
        return Math.sqrt(stddev / (double)x.size());
    }
}

