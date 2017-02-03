/**
 * 
 */
package distanceRankers;

import java.util.Collection;

import core.Point;
import core.Trajectory;
import core.TrajectoryException;
import core.distance.DistanceOperator;
import classifier.Classifier;

/**
 * @author Hui
 *
 */
public class ERPOperator extends DistanceOperator {
	/* the warping window for EDR */
	private static int m_warpingwindow = Integer.MAX_VALUE;
	
	/* the gap reference point */
	private Point m_gap = null;
	
	/**
	 * @param t
	 */
	public ERPOperator() {
		double[] coords = new double[Point.DIMENSION];
		for (int i = 0; i < Point.DIMENSION; i++) {
			coords[i] = 0.0;
		}
		m_gap = new Point(coords);
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
		double s1 = 0.0, s2 = 0.0, s3 = 0.0;

		double[][] df = new double[n][m];
		for (int i = 0; i < m; i++) {
			df[0][i] = 0.0;
		}
		for (int j = 0; j < n; j++) {
			df[j][0] = 0.0;
		}
		
		for (int i = 1; i < m; i++) {
			for (int k = 0; k < i; k++) {
				df[0][i] -= tr1.getPoint(k).distance(m_gap); 
			}
		}
		
		for (int j = 1; j < n; j++) {
			for (int k = 0; k < j; k++) {
				df[j][0] -= tr2.getPoint(k).distance(m_gap);
			}
		}
		
		for (int i = 1; i < m; i++) {
			for (int j = 1; j < n; j++) {
				Point p1 = tr1.getPoint(i-1), p2 = tr2.getPoint(j-1);
				double diff_d = p1.distance(p2);
				double diff_h = p1.distance(m_gap);
				double diff_v = p2.distance(m_gap);
				
				s1 = df[j-1][i-1] - diff_d;
				s2 = df[j-1][i] - diff_v;
				s3 = df[j][i-1] - diff_h;
				
				df[j][i] = Math.max(s1, s2);
				df[j][i] = Math.max(df[j][i], s3);
			}
		}
		
		return 0 - df[n-1][m-1];
	}
	
	/**
	 * a recursive method, not working!!!
	 * @param tr1
	 * @param tr2
	 * @param u
	 * @return
	 * @throws TrajectoryException
	 */
	public double computeDistance(Trajectory tr1, Trajectory tr2, int u)
			throws TrajectoryException {
		// TODO: get the recursive method to work!
		int m = tr1.getNumOfPoints();
		int n = tr2.getNumOfPoints();

		double[][] df = new double[m][n]; // warping matrix
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++)
				df[i][j] = 0.0;

		return Math.sqrt(ERPHelper(df, tr1, tr2, m - 1, n - 1));
	}

	private double ERPHelper(double[][] df, Trajectory tr1, Trajectory tr2,
			int i, int j) {
		double tdiff = Math.abs(tr1.getPoint(i).getTime()
				- tr2.getPoint(j).getTime());

		if (df[i][j] > -1.0) {
			return df[i][j];
		} else if (tdiff > m_warpingwindow) {
			df[i][j] = Double.MAX_VALUE;
		} else if (i == 0 && j == 0) {
			df[i][j] = tr1.getFirstPoint().distancesquare(tr2.getFirstPoint());
		} else if (i > 0 && j == 0) {
			df[i][j] = tr1.getPoint(i).distancesquare(tr2.getFirstPoint())
					+ ERPHelper(df, tr1, tr2, i - 1, 0);
		} else if (i == 0 && j > 0) {
			df[i][j] = tr1.getFirstPoint().distancesquare(tr2.getPoint(j))
					+ ERPHelper(df, tr1, tr2, 0, j - 1);
		} else if (i > 0 && j > 0) {
			double min1 = Math.min(ERPHelper(df, tr1, tr2, i - 1, j),
					ERPHelper(df, tr1, tr2, i - 1, j - 1));
			double min2 = Math.min(min1, ERPHelper(df, tr1, tr2, i, j - 1));
			df[i][j] = tr1.getPoint(i).distancesquare(tr2.getPoint(j)) + min2;
		} else {// how come there is still an else?
			assert (false);
		}

		return df[i][j];
	}
	
	

	@Override
	public String toString() {
		String output = "ERP operator:\n";
		return output;
	}

	@Override
	public double computeLowerBound(Trajectory tr1, Trajectory tr2)
			throws TrajectoryException {
		// according to the original paper, the lower bound is 
		// sum(Q) - sum(S)
		double sum1 = 0.0, sum2 = 0.0;
		int n = tr1.getNumOfPoints(), m = tr2.getNumOfPoints();
		for (int i = 0; i < n; i++) {
			sum1 += tr1.getPoint(i).getXPos();
		}
		
		for (int i = 0; i < m; i++) {
			sum2 += tr2.getPoint(i).getXPos();
		}
		
		return Math.abs(sum1 - sum2);
	}

	@Override
	public boolean hasLowerBound() {
		return true;
	}

	@Override
	public boolean needTuning() {
		return false;
	}

	@Override
	public void tuneOperator(Collection<Trajectory> trainset,
			Collection<Integer> labelset, Classifier classifier) {
		return;
	}

}
