/**
 * 
 */
package distanceRankers;

import java.util.Collection;

import core.Trajectory;
import core.TrajectoryException;
import core.distance.DistanceOperator;
import classifier.Classifier;

/**
 * @author Hui
 *
 */
public class FullDTWOperator extends DistanceOperator {
	/** the warping window of DTW */
	private static int m_warpingwindow = Integer.MAX_VALUE;
	
	
	/**
	 * constructor
	 * @param t
	 */
	public FullDTWOperator() {
		super();
	}
	
	
	/* (non-Javadoc)
	 * @see core.DistanceOperator#computeDistance(core.Trajectory)
	 */
	@Override
	public double computeDistance(Trajectory tr1, Trajectory tr2) 
								throws TrajectoryException {
		/*
		 * sanity check.
		 * future version should support missing values, unequal lengths and
		 * temporal shifting etc.
		 */
		if ( !checkTimeConsistency(tr1, tr2) ) {
			throw new TrajectoryException(
								"Trajectories incompatible for DTW distance," +
								"time interval not the same!");
		}
	    
		return DTWImpl(tr1, tr2);
		/*
	    int m = tr1.m_numofpoints;
	    int n = tr2.m_numofpoints;
	    
	    double[][] df = new double[m][n]; // warping matrix
	    for (int i = 0; i < m; i++)
	      for (int j = 0; j < n; j++)
	        df[i][j] = -1.0;
	    
	    return Math.sqrt(DTWHelper(df, tr1, tr2, m-1, n-1));
	    //*/		
	}
	
	private double DTWHelper(double[][] df, Trajectory tr1, Trajectory tr2, 
							int i, int j) {
	    double tdiff = Math.abs(tr1.getPoint(i).getTime() - 
	    						tr2.getPoint(j).getTime());
	    
	    if (df[i][j] > -1.0) {
	      return df[i][j];
	    }
	    else if (tdiff > m_warpingwindow ) {
	      df[i][j] = Double.MAX_VALUE;
	    }
	    else if (i == 0 && j == 0) {
	      df[i][j] = tr1.getFirstPoint().distancesquare(tr2.getFirstPoint());
	    }
	    else if (i > 0 && j == 0) {
	      df[i][j] = tr1.getPoint(i).distancesquare(tr2.getFirstPoint()) + 
	      				DTWHelper(df, tr1, tr2, i-1, 0);
	    }
	    else if (i == 0 && j > 0) {
	      df[i][j] = tr1.getFirstPoint().distancesquare(tr2.getPoint(j)) + 
	      				DTWHelper(df, tr1, tr2, 0, j-1);
	    }
	    else if (i > 0 && j > 0) {
	      double min1 = Math.min(DTWHelper(df, tr1, tr2, i-1, j), 
	    		  					DTWHelper(df, tr1, tr2, i-1, j-1));
	      double min2 = Math.min(min1, DTWHelper(df, tr1, tr2, i, j-1));
	      df[i][j] = tr1.getPoint(i).distancesquare(tr2.getPoint(j)) + min2;
	    }
	    else {// how come there is still an else?
	      assert(false);
	    }
	    
	    return df[i][j];		
	}
	
	private double DTWImpl(Trajectory tr1, Trajectory tr2) {
		int m = tr1.getNumOfPoints();
		int n = tr2.getNumOfPoints();
		
		double df[][] = new double[m][n];
		
		df[0][0] = tr1.getFirstPoint().distancesquare(tr2.getFirstPoint());
		for (int i = 1; i < m; i++) {
			df[i][0] = 
				df[i-1][0] + tr1.getPoint(i).distancesquare(tr2.getPoint(0));
		}
		for (int j = 1; j < n; j++) {
			df[0][j] = 
				df[0][j-1] + tr1.getPoint(0).distancesquare(tr2.getPoint(j));
		}
		
		for (int i = 1; i < m; i++) {
			for (int j = 1; j < n; j++) {
				if (((i < j) && (j-i) <= m_warpingwindow) || 
						((i >= j) && (i-j) <= m_warpingwindow)) {
					double min1 = Math.min(df[i][j-1], df[i-1][j]);
					double min2 = Math.min(min1, df[i-1][j-1]);
					df[i][j] = 
						min2 + tr1.getPoint(i).distancesquare(tr2.getPoint(j));
				}
				else {
					df[i][j] = Double.MAX_VALUE;
				}
			}
		}

		return Math.sqrt(df[m - 1][n - 1]);
	}

	public String toString() {
		return "DTWOperator:\n" + "m_warpingwindowsize: " + m_warpingwindow;
	}


	@Override
	public boolean needTuning() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void tuneOperator(Collection<Trajectory> trainset,
							Collection<Integer> labelset,
							Classifier classifier) {
		return;
	}
	
	/*
	public static void setWarpingWindow(int w) {
		m_warpingwindow = w;
	}
	
	public static int getWarpingWindow() {
		return m_warpingwindow;
	}
	//*/
	
	public boolean hasLowerBound() {
		return false;
	}
	
	public double computeLowerBound(Trajectory tr, Trajectory q) {
		return Double.MIN_VALUE;
	}
}
