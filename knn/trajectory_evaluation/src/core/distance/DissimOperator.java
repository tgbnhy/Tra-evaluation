/**
 * 
 */
package core.distance;

import java.util.Collection;

import core.*;

import classifier.Classifier;

/**
 * @author Hui
 *
 */
public class DissimOperator extends DistanceOperator {
	public static enum Variations {
		EXACT, APPROXIMATE
	};
	
	/* determine whether use the exact distance or the approximate distance */
	private static Variations m_variant = Variations.APPROXIMATE;
	
	/* the error bound associated with using approximate distance */
	private double m_error = 0.0;

	/**
	 * @param t
	 */
	public DissimOperator() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see core.DistanceOperator#computeDistance(core.Trajectory)
	 */
	@Override
	public double computeDistance(Trajectory tr1, Trajectory tr2) 
									throws TrajectoryException {
		if ( !checkTimeConsistency(tr1, tr2)) {
			throw new TrajectoryException(
					"Trajectories incompatible for Dissim distance,"
							+ "durations not the same!");
		}
		
		int itor1 = 1, itor2 = 1; // start from the second point on each traj
		Point pt1 = null, pt2 = null;
		double dist = 0.0, dissim = 0.0;
		int numofpoints1 = tr1.getNumOfPoints();
		int numofpoints2 = tr2.getNumOfPoints();
		Point last1 = tr1.getFirstPoint(), last2 = tr2.getFirstPoint(); 
		
		// reset error
		m_error = 0.0;
		
		while ( (itor1 < numofpoints1) && (itor2 < numofpoints2) ) {
	      /* 
	       * get next point, find its counter-part on the other trajectory, and
	       * calculate the distance between the two points, then update the
	       * euclidean distance
	       */ 			
	      pt1 = tr1.getPoint(itor1);
	      pt2 = tr2.getPoint(itor2);
	      
	      if ( pt1.getTime() == pt2.getTime() ) {  
	        dist = computeSegmentDissim(last1, pt1, last2, pt2, m_variant);
	        dissim += dist;
	        last1 = pt1;
	        last2 = pt2;
	        itor1++;
	        itor2++;
	      }
	      else if (pt1.getTime() < pt2.getTime()) {
	        Point last = tr2.getPoint(itor2-1);
	        // interpolate between last and pt2
	        double portion = (pt1.getTime() - last.getTime()) / 
	        					(pt2.getTime() - last.getTime());
	        double[] coords = new double[Point.DIMENSION];
	        coords[Point.X_DIM] = 
	        	last.getXPos() + (pt2.getXPos()-last.getXPos())* portion;
	        coords[Point.Y_DIM] = 
	        	last.getYPos() + (pt2.getYPos()-last.getYPos())* portion;
	        coords[Point.TIME_DIM] = pt1.getTime();
	        // compute the Dissim distance between the two corresponding portion
	        pt2 = new Point(coords);
	        dist = computeSegmentDissim(last1, pt1, last2, pt2, m_variant);
	        dissim += dist;
	        last1 = pt1;
	        last2 = pt2;
	        itor1++;
	      }
	      else if (pt1.getTime() > pt2.getTime()) {
	        Point last = tr1.getPoint(itor1-1);
	        // interpolate between last and pt2
	        double portion = (pt2.getTime() - last.getTime()) / 
	        					(pt1.getTime() - last.getTime());
	        double[] coords = new double[Point.DIMENSION];
	        coords[Point.X_DIM] = 
	        	last.getXPos() + (pt1.getXPos()-last.getXPos())* portion;
	        coords[Point.Y_DIM] = 
	        	last.getYPos() + (pt1.getYPos()-last.getYPos())* portion;
	        coords[Point.TIME_DIM] = pt2.getTime();
	        // compute the Dissim distance between the two corresponding portion
	        pt1 = new Point(coords);
	        dist = computeSegmentDissim(last1, pt1, last2, pt2, m_variant);
	        dissim += dist;
	        last1 = pt1;
	        last2 = pt2;
	        itor2++;        
	      }
	    }
		//System.out.println("dissim v:" + dissim);
	    return dissim;
	}

	public double getError() {
		return m_error;
	}
	
	private double computeSegmentDissim(Point p1, Point p2, 
										Point q1, Point q2,
										DissimOperator.Variations v) {
		double dissim = 0.0;
		if (v == Variations.APPROXIMATE) {
			double d1 = p1.distance(q1), d2 = p2.distance(q2);
			double a = q2.getXPos() - p2.getXPos();
			double b = q1.getXPos() - p1.getXPos();
			double c = q2.getYPos() - p2.getYPos();
			double d = q1.getYPos() - p1.getYPos();
			
			double ab = a - b, cd = c - d;
			double bfactor = 2 * (ab * b + cd * d);
			
			double ddt = 0.0;
			if (bfactor >= 0) {
				ddt = d1;
			} 
			else {
				double afactor = ab * ab + cd * cd;
				if (afactor == 0) {
					// error = 0
				}
				else {
					if (bfactor <= -2 * afactor) {
						ddt = d2;
					}
					else {
						ddt = Math.sqrt(-bfactor / (4 * afactor) + (b*b + d*d));
					}
				}
			}
			double adcb = a * d - c * b;
			if (ddt != 0) {
				m_error += (p2.getTime() - p1.getTime()) / (12 * adcb * adcb) / 
										(ddt * ddt * ddt);
			}
			dissim = (d1 + d2) * (p2.getTime() - p1.getTime()) / 2;			
		}
		else {
			double xtemp 
					= q2.getXPos() - q1.getXPos() - p2.getXPos() + p1.getXPos();
			double ytemp 
					= q2.getYPos() - q1.getYPos() - p2.getYPos() + p1.getYPos();
			double xdiff = q1.getXPos() - p1.getXPos();
			double ydiff = q1.getYPos() - p1.getYPos();
			double tdiff = p2.getTime() - p1.getTime();
			
			double a = xtemp * xtemp + ytemp * ytemp;
			double b = 2 * (xtemp * xdiff + ytemp * ydiff);
			double c = xdiff * xdiff + ydiff * ydiff;
			
			double afactor = a / (tdiff * tdiff);
			double bfactor = b / tdiff - 2 * a * p1.getTime() / (tdiff * tdiff);
			double cfactor = a * p1.getTime() * p1.getTime() / (tdiff * tdiff) 
							- b * p1.getTime() / tdiff + c;
			if (afactor == 0) {
				dissim = Math.sqrt(cfactor) / tdiff;
			}
			else {
				dissim = calcIntegral(afactor, bfactor, cfactor, p2.getTime()); 
				dissim -= calcIntegral(afactor, bfactor, cfactor, p1.getTime());
			}
			//System.out.println("case value:" + dissim + " " + out1 + " " + afactor);
		}		
		return dissim;
	}
	
	private double calcIntegral(double a, double b, double c, double t) {
		double parta = ( 2 * a * t + b ) / (4 * a);
		parta = parta * Math.sqrt(a * t * t + b * t + c);
		
		
		double partb = (b * b - 4 * a * c) / (8 * a * Math.sqrt(a));
		if ( partb != 0 && (4 * a * c - b * b > 0) ) {
			//System.out.println(4 * a * c - b * b);
			double x = ( 2 * a * t + b ) / Math.sqrt(4 * a * c - b * b);
			double arcsinh = Math.log(x + Math.sqrt(x * x + 1));
			partb = partb * arcsinh;
		}
		
		return parta - partb;// - partb;
	}
	
	@Override
	public String toString() {
		String output = "DISSIM operator:\n";
		output += "Variant: " + 
					(m_variant == Variations.APPROXIMATE?"approximate":"exact");
		return output;
	}

	@Override
	public boolean needTuning() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void tuneOperator(Collection<Trajectory> trainset,
			Collection<Integer> labelset, Classifier classifier) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double computeLowerBound(Trajectory tr1, Trajectory tr2)
			throws TrajectoryException {
		// TODO Auto-generated method stub
		return Double.MIN_VALUE;
	}

	@Override
	public boolean hasLowerBound() {
		// TODO Auto-generated method stub
		return false;
	}

}
