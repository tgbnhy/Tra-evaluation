/*
 * Decompiled with CFR 0_114.
 */
package distanceRankers;

import distanceRankers.Matrix;
import distanceRankers.TrajectoryDistance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import Launcher.Launch;
import trajectory.Edge;
import trajectory.STpoint;
import trajectory.Trajectory;

public class LCSS
implements TrajectoryDistance {
    Matrix matrix;
    double sThresh;
    double tThresh;

    public LCSS(double sThresh, double tThresh) {
        this.sThresh = sThresh;
        this.tThresh = tThresh;
    }

    public LCSS(double sThresh) {
        this.sThresh = sThresh;
        this.tThresh = Double.MAX_VALUE;
    }

    @Override
    public String getName() {
        return "LCSS";
    }

    @Override
    public String getPythonSymbol() {
        return "o";
    }

    @Override
    public double[] getDistance(Trajectory t1, Trajectory t2) {
        this.matrix = new Matrix(t1.edges.size() + 2, t2.edges.size() + 2);
        int i = 0;
        while (i < this.matrix.numRows()) {
            Arrays.fill(this.matrix.value[i], 0.0);
            ++i;
        }
        i = 1;
        while (i < this.matrix.numRows()) {
            int j = 1;
            while (j < this.matrix.numCols()) {
                this.matrix.value[i][j] = t1.getPoint(i - 1).euclidean(t2.getPoint(j - 1)) <= this.sThresh ? 1.0 + this.matrix.value[i - 1][j - 1] : Math.max(this.matrix.value[i - 1][j], this.matrix.value[i][j - 1]);
                ++j;
            }
            ++i;
        }
        double[] answer = new double[]{  this.matrix.score(),  1.0};
        return answer;
    }

    @Override
    public void printPath() {
    }
    
    public static void main(String[] args) throws NumberFormatException, IOException {
        ArrayList<Trajectory> trajectories = Launch.readTrajectories("filename", 0);
        System.out.println(" there are total " + (trajectories.size()) + " trajectories");
        
        //System.out.println(trajectories.get(0)); //[(1.0,9.0,0.0);(2.0,2.0,2.0);(3.0,4.0,3.0);(4.0,5.0,3.5);
        long begin = System.currentTimeMillis();
        LCSS d = new LCSS(1, 0.1);        
        int i = 0;
        while(i < 10) 	
        {
        	for(int j =0 ; j <89;j++)
        		{
        			System.out.println(d.getDistance(trajectories.get(i), trajectories.get(j))[0]);
        		}
        	i ++;
        }	
//        System.out.println(d.getDistance(trajectories.get(0), trajectories.get(0))[0]);
        long end = System.currentTimeMillis();
        System.out.println("duration£º" + (end - begin));
        //d.matrix.print();
    }
}

