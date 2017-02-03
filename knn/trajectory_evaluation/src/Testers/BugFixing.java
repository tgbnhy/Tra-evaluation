/*
 * Decompiled with CFR 0_114.
 */
package Testers;

import IO.CmdParser;
import Launcher.Launch;
import distanceRankers.EditDistance;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import trajectory.Summary;
import trajectory.Trajectory;

public class BugFixing {
    public static void main(String[] args) throws NumberFormatException, IOException {
        CmdParser parser = new CmdParser(args);
        String fileName = parser.getString("filename");
        ArrayList<Trajectory> db = Launch.readTrajectories(fileName, parser.getInteger("minLength", 2));
        Summary p = new Summary(db.get(0));
        EditDistance d = new EditDistance();
        int i = 0;
        while (i < db.size()) {
            p = p.join(db.get(i));
            System.out.println(p);
            System.out.println("Distance: " + d.getSubDistance(p, db.get(i))[0]);
            ++i;
        }
        System.out.println("Summary Area " + p.area);
        System.out.println(p);
        i = 0;
        while (i < db.size()) 
        {
            System.out.println("Distance: " + d.getSubDistance(p, db.get(i))[0] + " " + d.getDistance(db.get(0), db.get(i))[0] + " " + d.getDistance(db.get(1), db.get(i))[0]);
            ++i;
        }
    }
}

