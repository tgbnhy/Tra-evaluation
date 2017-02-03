/*
 * Decompiled with CFR 0_114.
 */
package distanceRankers;

import trajectory.Trajectory;

public interface TrajectoryDistance {
    public double[] getDistance(Trajectory var1, Trajectory var2);

    public void printPath();

    public String getName();

    public String getPythonSymbol();
}