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
public class EDROperator extends DistanceOperator {
	public static final int HIT = 0;
	
	public static final int MISS = -1;
	
	public static final int GAP = -1;
	
	/* the threshold for edit distance matching */
	private double m_threshold = 0.02;
	
	/* the warping window for EDR */
	private int m_warpingwindow = Integer.MAX_VALUE;

	
	/**
	 * @param t
	 */
	public EDROperator() {
	}

	/* (non-Javadoc)
	 * @see core.DistanceOperator#computeDistance(core.Trajectory)
	 */
	@Override
	public double computeDistance(Trajectory tr1, Trajectory tr2) 
									throws TrajectoryException {
		/* note the +1 in definition of m and n!!! */
		int m = tr1.getNumOfPoints() + 1;
		int n = tr2.getNumOfPoints() + 1;
		int s1 = 0, s2 = 0, s3 = 0;

		int[][] df = new int[n][m];
		for (int i = 0; i < m; i++)
			df[0][i] = -i;
		for (int j = 0; j < n; j++)
			df[j][0] = -j;

		for (int i = 1; i < m; i++) {
			for (int j = 1; j < n; j++) {
				Point p1 = tr1.getPoint(i-1), p2 = tr2.getPoint(j-1);
				if ((Math.abs(p1.getXPos() - p2.getXPos()) <= m_threshold) && 
					(Math.abs(p1.getYPos() - p2.getYPos()) <= m_threshold)) {
					s1 = HIT;
				}
				else {
					s1 = MISS;
				}
				
				s1 = df[j-1][i-1] + s1;
				s2 = df[j-1][i] + GAP;
				s3 = df[j][i-1] + GAP;
				
				df[j][i] = Math.max(s1, s2);
				df[j][i] = Math.max(df[j][i], s3);
			}
		}
		
		return 0-df[n-1][m-1];
	}
	

	@Override
	public String toString() {
		String output = "EDR operator:\n" +
				"m_threshold:" + m_threshold + "\n" +
				"m_warpingwindow:" + m_warpingwindow;
		return output;
	}

	@Override
	public double computeLowerBound(Trajectory tr1, Trajectory tr2)
			throws TrajectoryException {
		return computeHistogramBound(tr1, tr2);
	}
	
	public double computeHistogramBound(Trajectory tr1, Trajectory tr2) {
		// assume all trajectories are normalized
		// then set up a grid for entries 
		
		/*
		 * set up a grid for entries between -1 and 1 in the normalized 
		 * distribution, i.e, say that m_threshold = 0.25.  
		 * So, we would have array entries for >1, 1-.75, .75-.5, .5-.25,  
		 * .25-0, 0 - -.25, -.25 - -.5, -.5 - -.75, -.75 - -1, < -1.
		 * 
		 */
		//double threshold = Math.max(2 * m_threshold, 0.02);
		double threshold = 2 * m_threshold;
		int cellsperdim = 2 * (int)Math.ceil(1.6/(1 * threshold));
		// playing with the numbers!
		
		int[][] histogram = new int[cellsperdim][cellsperdim];
		/*
		
		
		for (int i = 0; i < cellsperdim; i++) {
			for (int j = 0; j < cellsperdim; j++) {
				double[] pLow = 
							{base + 2*i*m_threshold, base + 2*j*m_threshold};
				double[] pHigh = 
							{pLow[0] + 2*m_threshold, pLow[1] + 2*m_threshold};
				grid[i][j] = new Cell(new Region(pLow, pHigh));
			}
		}
		//*/
		
		// count the histogram of the difference of trajectory points
		double base = - cellsperdim * threshold / 2;
		int m = tr1.getNumOfPoints(), n = tr2.getNumOfPoints();
		for (int i = 0; i < m; i++) {
			Point pt = tr1.getPoint(i);
			int xpos = (int)Math.floor((pt.getXPos() - base) / threshold);
			int ypos = (int)Math.floor((pt.getYPos() - base) / threshold);
			
			histogram[xpos][ypos]++;
		}
		
		for (int i = 0; i < n; i++) {
			Point pt = tr2.getPoint(i);
			int xpos = (int)Math.floor((pt.getXPos() - base) / threshold);
			int ypos = (int)Math.floor((pt.getYPos() - base) / threshold);
			
			histogram[xpos][ypos]--;
		}
		
		for (int i = 0; i < cellsperdim; i++) {
			for (int j = 0; j < cellsperdim; j++) {
				// for each bin, find the approximate matching bins and adjust
				int mini = i-1 > 0 ? i-1 : 0;
				int maxi = i+1 < cellsperdim ? i+1 : cellsperdim-1;
				int minj = j-1 > 0 ? j-1 : 0;
				int maxj = j+1 < cellsperdim ? j+1 : cellsperdim-1;
				
				for (int k = mini; k <= maxi; k++) {
					for (int l = minj; l <= maxj; l++) {
						if (histogram[i][j] * histogram[k][l] < 0) {
							if (Math.abs(histogram[i][j]) >= 
								Math.abs(histogram[k][l])) {
								histogram[i][j] += histogram[k][l];
								histogram[k][l] = 0;
							}
							else {
								histogram[k][l] += histogram[i][j];
								histogram[i][j] = 0;
							}
						}
					}
				}
			}
		}
		
		int posdist = 0, negdist = 0;
		for (int i = 0; i < cellsperdim; i++) {
			for (int j = 0; j < cellsperdim; j++) {
				if (histogram[i][j] > 0) {
					posdist += histogram[i][j];
				}
				else {
					negdist -= histogram[i][j];
				}
			}
		}
		
		return Math.max(posdist, negdist);
	}

	@Override
	public boolean hasLowerBound() {
		// TODO Auto-generated method stub
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
		
		double bestt = 0;
		int bestw = m_warpingwindow;
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
		
		// try different combinations of penalty and threshold
		for (int w = maxlength; w <= maxlength; w++) {
			lg.fine("tuning with warpingwindow size:" + w);
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
