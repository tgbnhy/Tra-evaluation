/*
 * Decompiled with CFR 0_114.
 */
package Testers;

import Launcher.Launch;
import distanceRankers.EditDistance;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import trajectory.Trajectory;

public class TriangularInequality {
    public static void main(String[] args) throws NumberFormatException, IOException {
        String fileName = args[0];
        ArrayList<Trajectory> trajectories = Launch.readTrajectories(fileName, 0);
        int i = 0;
        EditDistance d = new EditDistance();
        while (i < trajectories.size()) {
            Trajectory a = trajectories.get(i++);
            Trajectory b = trajectories.get(i++);
            Trajectory c = trajectories.get(i++);
            double ab = d.getDistance(a, b)[0];
            double bc = d.getDistance(b, c)[0];
            double ac = d.getDistance(a, c)[0];
            System.out.println(String.valueOf(a.trajID) + " " + b.trajID + " " + c.trajID + " " + ab + " " + bc + " " + ac + " " + (ac - ab - bc));
        }
    }
}

