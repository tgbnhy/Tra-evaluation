/**
 * 
 */
package distanceRankers;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Logger;

import core.Point;
import core.Trajectory;
import core.TrajectoryException;
import core.distance.DistanceOperator;
import classifier.Classifier;
import classifier.ClassifierManager;

/**
 * @author Hui
 *
 */
public class DTWOperator extends DistanceOperator {
	/** the warping window of DTW */
	private static int m_warpingwindow = 75;//Integer.MAX_VALUE;
	
	private double[] lbound = null;
	
	private double[] ubound = null;
	
	private Trajectory query = null;
	
	private int[][] m_env = null;
	
	
	/**
	 * constructor
	 * @param t
	 */
	public DTWOperator() {
		super();
	}
	
	/**
	 * constructor
	 * @param t
	 * @param warpingwindow
	 */
	public DTWOperator(int warpingwindow) {	
		m_warpingwindow = warpingwindow;
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
	    
	    System.out.println("tabular dist:" + DTWImpl(tr1, tr2));
	    
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
		Logger lg = ClassifierManager.getLogger();
		
		int bestw = 0;
		double besterror = Double.MAX_VALUE;
		int maxlength = trainset.iterator().next().getNumOfPoints();
		
		// transfer data into a vector for easy leave-one-out manipulation
		Vector<Trajectory> vdata = new Vector<Trajectory>(trainset); 
		Vector<Integer> vlabels = new Vector<Integer>(labelset);
		
		for (int w = 1; w < maxlength * 0.25; w++) {
			lg.fine("tuning with w:" + w);
			m_warpingwindow = w;
			double error = tuneByLeaveOneOut(vdata, vlabels, classifier);
			if (error < besterror) {
				bestw = w;
				besterror = error;
			}
		}
		
		lg.info("best w:" + bestw);
		// set the parameter
		m_warpingwindow = bestw;
	}
	
	
	public static void setWarpingWindow(int w) {
		m_warpingwindow = w;
	}
	
	public static int getWarpingWindow() {
		return m_warpingwindow;
	}
		
	private void computeEnv(int length) {
		m_env = new int[length][2];
		
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < 2; j++) {
				m_env[i][j] = length;
			}
		}
		
		int cur_height = 0;
		// lower envelope
		for (int i = 0; i < length; i++) {
			m_env[i][0] = cur_height;
			cur_height++;
		}
		
		for (int i = m_warpingwindow; i < length; i++) {
			m_env[i][0] = m_warpingwindow;
		}
		
		// upper envelope
		for (int i = 0; i < length - m_warpingwindow; i++) {
			m_env[i][1] = m_warpingwindow;
		}
		
		cur_height = m_warpingwindow-1;
		for (int i = length - m_warpingwindow; i < length; i++) {
			m_env[i][1] = cur_height;
			cur_height--;
		} 
	}

	protected double[] computeU(Trajectory t) {
		double[] coords = new double[t.getNumOfPoints() * Point.DIMENSION];
		for (int i = 0; i < t.getNumOfPoints(); i++) {
			double x = 0.0;
			double y = 0.0;
			
			int lb = (i - m_warpingwindow > 0) ? i - m_warpingwindow : 0;
			int ub = (i + m_warpingwindow < t.getNumOfPoints()) ? 
						i + m_warpingwindow : t.getNumOfPoints()-1;
			/*
			int lb = (i - m_env[i][1] > 0) ? (i - m_env[i][1]) : 0;
			int ub = (i + m_env[i][1] < t.getNumOfPoints()) ? 
							i + m_env[i][1] : t.getNumOfPoints()-1;
			//*/
			
			for (int j = lb; j <= ub; j++) {
				if (x < t.getPoint(j).getXPos()) {
					x = t.getPoint(j).getXPos();
				}
				if (y < t.getPoint(j).getYPos()) {
					y = t.getPoint(j).getYPos();
				}
			}
			coords[i * Point.DIMENSION + Point.X_DIM] = x;
			coords[i * Point.DIMENSION + Point.Y_DIM] = y;
		}
		return coords;
	}

	protected double[] computeL(Trajectory t) {
		double[] coords = new double[t.getNumOfPoints() * Point.DIMENSION];
		for (int i = 0; i < t.getNumOfPoints(); i++) {
			double x = 0.0;
			double y = 0.0;
			
			int lb = (i - m_warpingwindow > 0) ? i - m_warpingwindow : 0;
			int ub = (i + m_warpingwindow < t.getNumOfPoints()) ? 
						i + m_warpingwindow : t.getNumOfPoints()-1;
			/*
			int lb = (i - m_env[i][0] > 0) ? (i - m_env[i][0]) : 0;
			int ub = (i + m_env[i][0] < t.getNumOfPoints()) ? 
							i + m_env[i][0] : t.getNumOfPoints()-1;
							//*/
			for (int j = lb; j <= ub; j++) {
				if (x > t.getPoint(j).getXPos()) {
					x = t.getPoint(j).getXPos();
				}
				if (y > t.getPoint(j).getYPos()) {
					y = t.getPoint(j).getYPos();
				}
			}
			coords[i * Point.DIMENSION + Point.X_DIM] = x;
			coords[i * Point.DIMENSION + Point.Y_DIM] = y;
		}
		return coords;
	}
	
	public boolean hasLowerBound() {
		return true;
	}
	
	public double computeLowerBound(Trajectory tr, Trajectory q) {
		return computeLBKeogh(tr, q);
	}
	
	protected double computeLBKeogh(Trajectory tr, Trajectory q) {
		double dist = 0.0;
		if (tr.getNumOfPoints() != q.getNumOfPoints()) {
			throw new RuntimeException("LBKeogh requires same number of points");
		}
		
		int n = tr.getNumOfPoints();
		double[] ubound, lbound;
		if (this.query != q) {
			//computeEnv(tr.getNumOfPoints());
			ubound = computeU(q);
			lbound = computeL(q);
			// remember the transformation
			this.query = q;
			this.ubound = ubound;
			this.lbound = lbound;
		}
		else {
			// reuse
			ubound = this.ubound;
			lbound = this.lbound;
		}
		
		for (int i = 0; i < n; i++) {
			Point pt = tr.getPoint(i);
			if (pt.getXPos() > ubound[i * Point.DIMENSION + Point.X_DIM]) {
				dist += 
					(pt.getXPos() - ubound[i * Point.DIMENSION + Point.X_DIM]) *
					(pt.getXPos() - ubound[i * Point.DIMENSION + Point.X_DIM]);
			}
			else if (pt.getXPos() < lbound[i * Point.DIMENSION + Point.X_DIM]) {
				dist += 
					(pt.getXPos() - lbound[i * Point.DIMENSION + Point.X_DIM]) *
					(pt.getXPos() - lbound[i * Point.DIMENSION + Point.X_DIM]);
			}
			
			if (pt.getYPos() > ubound[i * Point.DIMENSION + Point.Y_DIM]) {
				dist += 
					(pt.getYPos() - ubound[i * Point.DIMENSION + Point.Y_DIM]) *
					(pt.getYPos() - ubound[i * Point.DIMENSION + Point.Y_DIM]);
			}
			else if (pt.getYPos() < lbound[i * Point.DIMENSION + Point.Y_DIM]) {
				dist += 
					(pt.getYPos() - lbound[i * Point.DIMENSION + Point.Y_DIM]) *
					(pt.getYPos() - lbound[i * Point.DIMENSION + Point.Y_DIM]);
			}
		}

		return Math.sqrt(dist);
	}

	public static Collection<Point> read(String trajectoryfile) {
		LineNumberReader lr = null;
		String line = null;
		ArrayList<Point> pts = new ArrayList<Point>();

		try {
			lr = new LineNumberReader(new FileReader(trajectoryfile));

			int time = 0;
			while ((line = lr.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line);
				Double value = new Double(st.nextToken());
				//data.add(value);
				
				double[] coords = {value.doubleValue(), 0.0, time};
				pts.add(new Point(coords));
				time++;
			}

			lr.close();
		} catch (FileNotFoundException e) {
			System.err.println("Cannot open file: " + trajectoryfile);
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return pts;
	}
	
	public static void normalize(Trajectory tr) {
		double stdx = tr.getStdDeviation();
		
		double avgx = tr.getXAverage();
		
		
		for (int i = 0; i < tr.getNumOfPoints(); i++) {
			//tr.m_coords[i * 3] = (tr.m_coords[i * 3] - avgx) / stdx;
			Point pt = tr.getPoint(i);
			pt.m_pCoords[Point.X_DIM] = (pt.m_pCoords[Point.X_DIM] - avgx) / stdx; 
		}
	}
	
	public static void main(String[] args) {
		String queryfile = "swale//traj13.dat";
		String targetfile = "swale//traj4.dat";
		
		DTWOperator op = new DTWOperator();
		
		try {
			Trajectory query = new Trajectory(1, read(queryfile), op);
			Trajectory target = new Trajectory(2, read(targetfile), op);
			
			// now normalize the data
			normalize(query);
			normalize(target);
			
			System.out.println(op);
			
			// now compute distance
			System.out.println("distance:" + query.getDistance(target));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
