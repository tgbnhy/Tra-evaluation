/*
 * Decompiled with CFR 0_114.
 */
package distanceRankers;

import distanceRankers.Matrix;
import distanceRankers.TrajectoryDistance;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import Launcher.Launch;
import trajectory.Edge;
import trajectory.STpoint;
import trajectory.Trajectory;

public class DTW
implements TrajectoryDistance {
    Matrix matrix;

    @Override
    public double[] getDistance(Trajectory t1, Trajectory t2) {
        this.matrix = new Matrix(t1.edges.size() + 2, t2.edges.size() + 2);
        this.initializeMatrix();
        int i = 1;
        while (i < this.matrix.numRows()) {
            int j = 1;
            while (j < this.matrix.numCols()) {
                double score = t1.getPoint(i - 1).euclidean(t2.getPoint(j - 1));
                this.matrix.value[i][j] = score + Math.min(this.matrix.value[i - 1][j - 1], Math.min(this.matrix.value[i - 1][j], this.matrix.value[i][j - 1]));
                ++j;
            }
            ++i;
        }
        double[] answer = new double[]{this.matrix.score(), -1.0};
        return answer;
    }

    private void initializeMatrix() {
        int i = 1;
        while (i < this.matrix.value.length) {
            this.matrix.value[i][0] = Double.MAX_VALUE;
            ++i;
        }
        int j = 1;
        while (j < this.matrix.value[0].length) {
            this.matrix.value[0][j] = Double.MAX_VALUE;
            ++j;
        }
        this.matrix.value[0][0] = 0.0;
    }

    @Override
    public void printPath() {
    }

    @Override
    public String getName() {
        return "DTW";
    }

    @Override
    public String getPythonSymbol() {
        return "d";
    }
    
    public static void main(String[] args) throws NumberFormatException, IOException {
        ArrayList<Trajectory> trajectories = Launch.readTrajectories("filename", 0);
        System.out.println(" there are total " + (trajectories.size()) + " trajectories");
        
        System.out.println(trajectories.get(0)); //[(1.0,9.0,0.0);(2.0,2.0,2.0);(3.0,4.0,3.0);(4.0,5.0,3.5);
        long begin = System.currentTimeMillis();
        DTW d = new DTW();
        
        int i = 0;
        while(i <1) 	
        {
        	for(int j =0 ; j <89;j++)
        		{   
        			
        			System.out.println(d.getDistance(trajectories.get(i), trajectories.get(j))[0]);
        			double a = d.getDistance(trajectories.get(i), trajectories.get(j))[0];
        			String str = String.valueOf(a);
        	        //System.out.println(str);
        			FileWriter fstream = new FileWriter("D://Eclipse workplace//trajectory_evalutation//top-k.txt",true);
        		    BufferedWriter out = new BufferedWriter(fstream);
        		    out.write(str + '\n');
        		    out.close();
        		}
        	i ++;
        }	
        //System.out.println(d.getDistance(trajectories.get(0), trajectories.get(2))[0]);
        long end = System.currentTimeMillis();
        System.out.println("duration£º" + (end - begin));
        //d.matrix.print();
    }
}

