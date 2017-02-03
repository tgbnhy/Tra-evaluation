/*
 * Decompiled with CFR 0_114.
 */
package Testers;

import java.util.ArrayList;
import trajectory.Date;
import trajectory.Edge;
import trajectory.STpoint;
import trajectory.Trajectory;

public class AddNoise {
    public static ArrayList<Trajectory> insertAndPerturb(ArrayList<Trajectory> db, double quantity, double perturbation) {
        ArrayList<Trajectory> noisyDB = new ArrayList<Trajectory>(db.size());
        for (Trajectory t : db) {
            ArrayList<STpoint> noisyPoints = new ArrayList<STpoint>();
            noisyPoints.add(new STpoint(t.getPoint(0)));
            for (Edge e : t.edges) {
                STpoint p1 = (STpoint)noisyPoints.get(noisyPoints.size() - 1);
                STpoint p2 = new STpoint(e.p2);
                double toss = Math.random();
                if (toss < quantity / 2.0) {
                    double pamt = Math.random() * perturbation - perturbation / 2.0;
                    p2.x += pamt;
                    pamt = Math.random() * perturbation - perturbation / 2.0;
                    p2.y += pamt;
                }
                if (toss > quantity / 2.0 && toss < quantity) {
                    STpoint newP = AddNoise.insertPoint(p1, p2);
                    noisyPoints.add(newP);
                }
                noisyPoints.add(p2);
            }
            noisyDB.add(new Trajectory(t.index, t.trajID, t.startDate, noisyPoints));
        }
        return noisyDB;
    }

    public static ArrayList<Trajectory> perturb(ArrayList<Trajectory> db, double quantity, double perturbation) {
        ArrayList<Trajectory> noisyDB = new ArrayList<Trajectory>(db.size());
        for (Trajectory t : db) {
            ArrayList<STpoint> noisyPoints = new ArrayList<STpoint>();
            noisyPoints.add(t.getPoint(0));
            for (Edge e : t.edges) {
                double toss = Math.random();
                if (toss < quantity) {
                    double x = e.p2.x;
                    double y = e.p2.y;
                    double pamt = Math.random() * perturbation;
                    pamt = Math.random() * perturbation;
                    noisyPoints.add(new STpoint(x += pamt, y += pamt, e.p2.time));
                    continue;
                }
                noisyPoints.add(new STpoint(e.p2));
            }
            noisyDB.add(new Trajectory(t.index, t.trajID, t.startDate, noisyPoints));
        }
        return noisyDB;
    }

    public static ArrayList<Trajectory> insert(ArrayList<Trajectory> db, double quantity) {
        ArrayList<Trajectory> noisyDB = new ArrayList<Trajectory>(db.size());
        for (Trajectory t : db) {
            ArrayList<STpoint> noisyPoints = new ArrayList<STpoint>();
            noisyPoints.add(new STpoint(t.getPoint(0)));
            for (Edge e : t.edges) {
                STpoint p1 = e.p1;
                STpoint p2 = e.p2;
                double toss = Math.random();
                if (toss < quantity) {
                    STpoint newP = AddNoise.insertPoint(p1, p2);
                    noisyPoints.add(newP);
                }
                noisyPoints.add(new STpoint(e.p2));
            }
            noisyDB.add(new Trajectory(t.index, t.trajID, t.startDate, noisyPoints));
        }
        return noisyDB;
    }

    private static STpoint insertPoint(STpoint p1, STpoint p2) {
        double newX = (p1.x + p2.x) / 2.0;
        double newY = (p1.y + p2.y) / 2.0;
        double newT = (p1.time + p2.time) / 2.0;
        STpoint newp = new STpoint(newX, newY, newT);
        return newp;
    }
}

