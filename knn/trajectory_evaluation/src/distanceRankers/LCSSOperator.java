/**
 * 
 */
package distanceRankers;

import java.util.Collection;
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
public class LCSSOperator extends DistanceOperator {
	/* temporal warping window */
	private static int m_warpingwindow = Integer.MAX_VALUE;
	
	/* threshold for */
	private static double m_threshold = 0.25;
	
	/* query trajectory */
	private Trajectory m_query = null;
	
	/* query upper bound */
	private double[] m_ubound = null;
	
	/* query lower bound */
	private double[] m_lbound = null;

	/**
	 * @param t
	 */
	public LCSSOperator() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see core.DistanceOperator#computeDistance(core.Trajectory)
	 */
	@Override
	public double computeDistance(Trajectory tr1, Trajectory tr2) 
										throws TrajectoryException {
		int m = tr1.getNumOfPoints();
		int n = tr2.getNumOfPoints();

		/*
		int[][] df = new int[m][n];
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++)
				df[i][j] = 0;

		int lcsslength = LCSSRecursiveHelper(df, tr1, tr2, m - 1, n - 1);
		//*/
		int[][] df = new int[m+1][n+1];
		for (int i = 1; i <= m; i++) {
			df[i][0] = 0;
		}
		for (int j = 0; j <= n; j++) {
			df[0][j] = 0;
		}
		
		for (int i = 1; i <=m; i++) {
			for (int j = 1; j <=n; j++) {
				if (match(tr1.getPoint(i-1), tr2.getPoint(j-1))) {
					df[i][j] = df[i-1][j-1] + 1;
				}
				else if (df[i-1][j] >= df[i][j-1]) {
					df[i][j] = df[i-1][j];
				}
				else {
					df[i][j] = df[i][j-1];
				}
			}
		}
		
		return 1.0 - ((double)df[m][n])/Math.min(m, n);
	}
	
	private boolean match(Point pt1, Point pt2) {
		double xdiff = Math.abs(pt1.getXPos() - pt2.getXPos()),
				ydiff = Math.abs(pt1.getYPos() - pt2.getYPos()),
				tdiff = Math.abs(pt1.getTime() - pt2.getTime());
		if ( (xdiff <= m_threshold) && (ydiff < m_threshold) &&
				(tdiff <= m_warpingwindow) ) {
			return true;
		} 
		return false;
	}

	@Override
	public String toString() {
		String output = "LCSS Operator:\n" +
				"m_threshold:" + m_threshold + "\n" +
				"m_warpingwindow:" + m_warpingwindow + "\n";
		return output;
	}
	
	private double[] computeU(Trajectory t) {
		double[] coords = new double[t.getNumOfPoints() * Point.DIMENSION];
		for (int i = 0; i < t.getNumOfPoints(); i++) {
			double x = 0.0; // maxx
			double y = 0.0; // maxy
			
			int lb = (i - m_warpingwindow > 0) ? i - m_warpingwindow : 0;
			int ub = (i + m_warpingwindow < t.getNumOfPoints()) ? 
						i + m_warpingwindow : t.getNumOfPoints()-1;
			
			for (int j = lb; j <= ub; j++) {
				if (x < t.getPoint(j).getXPos()) {
					x = t.getPoint(j).getXPos();
				}
				if (y < t.getPoint(j).getYPos()) {
					y = t.getPoint(j).getYPos();
				}
			}
			coords[i * Point.DIMENSION + Point.X_DIM] = x + m_threshold;
			coords[i * Point.DIMENSION + Point.Y_DIM] = y + m_threshold;
		}
		return coords;
	}
	
	private double[] computeL(Trajectory t) {
		double[] coords = new double[t.getNumOfPoints() * Point.DIMENSION];
		for (int i = 0; i < t.getNumOfPoints(); i++) {
			double x = 0.0; // minx
			double y = 0.0; // miny
			
			int lb = (i - m_warpingwindow > 0) ? i - m_warpingwindow : 0;
			int ub = (i + m_warpingwindow < t.getNumOfPoints()) ? 
						i + m_warpingwindow : t.getNumOfPoints()-1;
			
			for (int j = lb; j <= ub; j++) {
				if (x > t.getPoint(j).getXPos()) {
					x = t.getPoint(j).getXPos();
				}
				if (y > t.getPoint(j).getYPos()) {
					y = t.getPoint(j).getYPos();
				}
			}
			coords[i * Point.DIMENSION + Point.X_DIM] = x - m_threshold;
			coords[i * Point.DIMENSION + Point.Y_DIM] = y - m_threshold;
		}
		return coords;
	}

	@Override
	public double computeLowerBound(Trajectory tr, Trajectory q)
			throws TrajectoryException {
		if (tr.getNumOfPoints() != q.getNumOfPoints()) {
			return 0;
			//throw new RuntimeException(
			//		"currently LCSS bound requires same number of points");
		}
		
		// first compute the upper and lower envelope
		int n = tr.getNumOfPoints();
		double[] ubound, lbound;
		if (q != m_query) {
			ubound = computeU(q);
			lbound = computeL(q);
			// remember the transformation
			m_query = q;
			m_ubound = ubound;
			m_lbound = lbound;
		}
		else {
			// reuse
			ubound = m_ubound;
			lbound = m_lbound;
		}
		

		// then compute the upper bound for lcss, which leads to the lower-bound
		// distance
		int count = 0;
		for (int i = 0; i < n; i++) {
			Point pt = tr.getPoint(i);
			if ((pt.getXPos() > ubound[i * Point.DIMENSION + Point.X_DIM]) ||
				(pt.getXPos() < lbound[i * Point.DIMENSION + Point.X_DIM])) {
				continue;
			}
			
			/*
			if ((pt.getYPos() > ubound[i * Point.DIMENSION + Point.Y_DIM]) ||
				(pt.getYPos() < lbound[i * Point.DIMENSION + Point.Y_DIM])) {
				continue;
			}
			//*/
			count++;
		}

		return 1.0 - ((double)count)/n; 
	}
		

	@Override
	public boolean hasLowerBound() {
		return true;
	}

	@Override
	public boolean needTuning() {
		return true;
	}

	@Override
	public void tuneOperator(Collection<Trajectory> trainset,
			Collection<Integer> labelset, Classifier classifier) {
		Logger lg = ClassifierManager.getLogger();
		
		double bestt = 0; // best threshold
		int bestw = 0; // best warping window size
		double besterror = Double.MAX_VALUE;
		int maxlength = trainset.iterator().next().getNumOfPoints();
		
		// transfer data into a vector for easy leave-one-out manipulation
		Vector<Trajectory> vdata = new Vector<Trajectory>(trainset); 
		Vector<Integer> vlabels = new Vector<Integer>(labelset);
		
		/* collect statistics about the trainset: maxstd */
		double setstd = 0.0;
		for (int i = 0; i < vdata.size(); i++) {
			setstd += vdata.get(i).getStdDeviation();
		}
		setstd /= vdata.size();
		
		double tstep = setstd * 0.02;
		
		for (int w = 1; w <= maxlength * 0.25; w++) {
			lg.fine("tuning with window:" + w);
			m_warpingwindow = w;
			for (int t = 1; t <= 50; t++) {
				lg.fine("tuning with threshold:" + t * tstep);
				m_threshold = t * tstep;
				double error = tuneByLeaveOneOut(vdata, vlabels, classifier);
				if (error < besterror) {
					bestw = w;
					bestt = t * tstep;
					besterror = error;
				}
			}
		}
		
		lg.info("best w:" + bestw + "\t best t:" + bestt);
		// set the parameter
		m_warpingwindow = bestw;
		m_threshold = bestt;
		
	}

}
