/*
 * Decompiled with CFR 0_114.
 */
package distanceRankers;

import Launcher.Launch;
import distanceRankers.Matrix;
import distanceRankers.TrajectoryDistance;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import trajectory.Trajectory;

public class EDR
implements TrajectoryDistance {
    Matrix matrix;
    double sThresh;
    double tThresh;

    public EDR(double sThresh, double tThresh) {
        this.sThresh = sThresh;
        this.tThresh = tThresh;
    }

    public EDR(double sThresh) {
        this.sThresh = sThresh;
        this.tThresh = Double.MAX_VALUE;
    }

    @Override
    public double[] getDistance(Trajectory t1, Trajectory t2) {
        this.matrix = new Matrix(t1.edges.size() + 2, t2.edges.size() + 2);
        this.initializeMatrix(t1.edges.size() + 1, t2.edges.size() + 1);
        int i = 1;
        while (i < this.matrix.numRows()) {
            int j = 1;
            while (j < this.matrix.numCols()) {
                double temp = this.matrix.value[i - 1][j - 1] + t1.getPoint(i - 1).euclidean(t2.getPoint(j - 1)) > this.sThresh ? 1 : 0;
                this.matrix.value[i][j] = Math.min(temp, Math.min(this.matrix.value[i - 1][j] + 1.0, this.matrix.value[i][j - 1] + 1.0));
                ++j;
            }
            ++i;
        }
        double[] answer = new double[]{this.matrix.score(), -1.0};
        return answer;
    }

    private void initializeMatrix(int n, int m) {
        int i = 0;
        while (i < this.matrix.numRows()) {
            Arrays.fill(this.matrix.value[i], 0.0);
            ++i;
        }
        i = 1;
        while (i < this.matrix.value.length) {
            this.matrix.value[i][0] = m;
            ++i;
        }
        int j = 1;
        while (j < this.matrix.value[0].length) {
            this.matrix.value[0][j] = n;
            ++j;
        }
    }

    @Override
    public void printPath() {
    }

    @Override
    public String getName() {
        return "EDR";
    }

    @Override
    public String getPythonSymbol() {
        return "s";
    }

    public static void main(String[] args) throws NumberFormatException, IOException {
        ArrayList<Trajectory> trajectories = Launch.readTrajectories("filename", 0);
        System.out.println(" there are total " + (trajectories.size()) + " trajectories");
        
        System.out.println(trajectories.get(0)); //[(1.0,9.0,0.0);(2.0,2.0,2.0);(3.0,4.0,3.0);(4.0,5.0,3.5);
        long begin = System.currentTimeMillis();
        EDR d = new EDR(0.5, 1.0);
        
        int i = 0;
        while(i < 10) 	
        {
        	for(int j = 0 ; j<89;j++)
        		{
        			System.out.println(d.getDistance(trajectories.get(i), trajectories.get(j))[0]);
        			
        		}
        	i ++;
        }	
//         System.out.println(d.getDistance(trajectories.get(0), trajectories.get(2))[0]);
         long end = System.currentTimeMillis();
         System.out.println("duration£º" + (end - begin));
         //d.matrix.print();
    }
}

