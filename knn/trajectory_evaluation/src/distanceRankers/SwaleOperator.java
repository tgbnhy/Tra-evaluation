/**
 * 
 */
package distanceRankers;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Logger;

import core.Point;
import core.Trajectory;
import core.TrajectoryException;
import core.distance.DistanceOperator;
import spatialindex.spatialindex.Region;
import classifier.Classifier;
import classifier.ClassifierManager;

/**
 * @author Hui
 *
 */
public class SwaleOperator extends DistanceOperator {
	public static double m_threshold = 0.00809622488455851;
;
	
	public static double m_matchreward = 50;
	
	public static double m_gappenalty = 0;

	/**
	 * @param t
	 */
	public SwaleOperator() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see core.DistanceOperator#computeDistance(core.Trajectory)
	 */
	@Override
	public double computeDistance(Trajectory tr1, Trajectory tr2) 
									throws TrajectoryException {
		double score = 0.0;
		// assume all trajectories are normalized
		// then set up a grid for entries
		
		/*
		 * set up a grid for entries between -2 and 2 in the normalized 
		 * distribution, i.e, say that m_threshold = 0.25.  Then, 2dt = .5.  
		 * So, we would have array entries for >2, 2-1.5, 1.5-1, 1-.5, .5-0, 
		 * 0- -.5, -.5 - -1, -1, - -1.5, -1.5 - -2, < -2.
		 * 
		 * NOTE!!! different from the paper, the grid cells has a edge length
		 * of 2 * m_threshold instead of 1 * m_threshold
		 */
		double gridcellsize = 2 * m_threshold;
		/* must ensure no data item value is greater than 2.0, otherwise 
		 * this is not going to work
		 */
		int cellsperdim = 2 * (int)Math.ceil(1.5/gridcellsize);
		
		Cell[][] grid = new Cell[cellsperdim][cellsperdim];
		double base = - cellsperdim * gridcellsize / 2;
		
		/* compute cell boundary, seems not necessary */
		for (int i = 0; i < cellsperdim; i++) {
			for (int j = 0; j < cellsperdim; j++) {
				double[] pLow = 
							{base + i*gridcellsize, base + j*gridcellsize};
				double[] pHigh = 
							{pLow[0] + gridcellsize, pLow[1] + gridcellsize};
				grid[i][j] = new Cell(new Region(pLow, pHigh));
			}
		}
		
		// construct the mbr, insert the point into all the cells mbr intersects
		int m = tr1.getNumOfPoints();
		for (int i = 0; i < m; i++) {
			int index = m - i - 1;
			Point pt = tr1.getPoint(index);
			Point lowerleft = new Point(pt.m_pCoords), 
					upright = new Point(pt.m_pCoords);
			lowerleft.addOffset(-m_threshold, -m_threshold, 0);
			upright.addOffset(m_threshold, m_threshold, 0);
			int lowx = 
			(int)Math.floor((lowerleft.getXPos() - base) / gridcellsize);
			int lowy = 
			(int)Math.floor((lowerleft.getYPos() - base) / gridcellsize);
			int highx = 
				(int)Math.floor((upright.getXPos() - base) / gridcellsize);
			int highy =
				(int)Math.floor((upright.getYPos() - base) / gridcellsize);
			
			for (int k = lowx; k <= highx; k++) {
				for (int l = lowy; l <= highy; l++) {
					// insert into grid cell [k][l]
					//double[] pLow = {lowx, lowy}, pHigh = {highx, highy};
					double[] pLow = {lowerleft.getXPos(), lowerleft.getYPos()}, 
							pHigh = {upright.getXPos(), upright.getYPos()};
					//System.err.println("k:" + k + "  l:" + l);
					grid[k][l].insert(
								new CellEntry(index, new Region(pLow,pHigh)));
				}
			}
		}
		
		/* 
		 * maintain the best score
		 * stores at position matches[i] the smallest value k for which i 
		 * matches exists between the elements of S and r1, ... rk
		 */
		int n = tr2.getNumOfPoints();
		// not sure why n+m+5, seems n+1 is enough?
		int[] matches = new int[n + m + 5]; 
		matches[0] = -1;
		for (int i = 1; i < m + n; i++)
			matches[i] = m + n + 2;
		int max = -1;
		
		// iterate through the elements of trajectory tr2
		for (int i = 0; i < n; i++) {
			// determine which grid cell it fits in
			Point pt = tr2.getPoint(i);
			int k =	(int)Math.floor((pt.getXPos() - base) / gridcellsize);
			int l =	(int)Math.floor((pt.getYPos() - base) / gridcellsize);
			
			if (grid[k][l].numentries == 0) {
				continue;
			}
			
			// if there are entries in that grid cell
			int temp = -1; // for overwritten
			int c = 0;
			int value = -1;
			Iterator<CellEntry> itor = grid[k][l].queue.iterator();
			for ( ; itor.hasNext(); ) {
				CellEntry e = itor.next();
				// compare with the mbr to see if it is a match
				double[] coords = {0.0,0.0};
				coords[0] = pt.getXPos();coords[1] = pt.getYPos();
				/*
				// currently just use the x dimension
				if (pt.getXPos() >= e.mbr.getLow(Point.X_DIM) &&
						pt.getXPos() <= e.mbr.getHigh(Point.X_DIM)) {
					value = e.id;
				}
				//*/
				
				if (e.mbr.contains(new Point(coords))) {
					value = e.id;
				}
				else {
					continue;
				}
				
				// try to increase the best possible score using these entries
				if (value > temp) {
					/*
					while (matches[c] < value && (c <= max || c <=1)) {
						c++;
					}
					//*/
					for ( ; matches[c] < value && (c <= max || c <= 1); c++)
						;
					temp = matches[c];
					if (matches[c] > value) {
						matches[c] = value;
					}
					if ( max < c) {
						max = c;
					}
				}
			}
		}
		score = max;
		
		/* this is for calculating LCSS sequence */
		//return 1.0 - ((double)score)/Math.min(m, n);
		
		// this is the real score formula
		score = max * m_matchreward + (m + n - 2 * max) * m_gappenalty;
		
		//System.err.println("max:" + max + " score:" + score + 
		//		"return:" + (1.0 - ((double)score)/Math.min(m, n)));
		
		//return 0 - score;
		return 1.0 - ((double)score)/Math.min(m, n);
		//*/
	}
	
	/* This method is depreciated */
	double Swaledist(Trajectory qx, Trajectory px) {
		int len1 = qx.getNumOfPoints(), len2 = px.getNumOfPoints();
		//int M, N, i, j, k;
		int numgrids;
		//float *bounds;
		double xloc, lowerx;
		double yloc, lowery;
		//struct grid_entry** grid, *entry;
		int xgrid_spot, ygrid_spot;
		//int** intersections;
		//int* r_mem, *total_score_mem;
		int new_score, score, val, tsm_ptr;
		int c_temp, c_k;
		//M=len1+1;
		//N=len2+1;

		//set up a grid for entries between -2 and 2 in the normalized distribution.
		//ie, say that difthreshold = 0.25.  Then, 2dt = .5.  So, we would have
		//array entries for >2, 2-1.5, 1.5-1, 1-.5, .5-0, 0- -.5, -.5 - -1, 
		//-1, - -1.5, -1.5 - -2, < -2.
		//numgrids = (int) ceil(2.0/(2*diffthreshold));
		numgrids = (int) Math.ceil(2.0/(2*m_threshold));

		//bounds = (float*) malloc(sizeof(float)*(2*numgrids+1));
		double[] bounds = new double[2*numgrids+1];
		
		//bounds[0] = -1*numgrids*2*diffthreshold;
		bounds[0] = -1*numgrids*2*m_threshold;
		
		for (int i =1; i <= numgrids*2; i++) {
			bounds[i] = bounds[i-1]+2*m_threshold;
		}

		//grid = (struct grid_entry**) malloc(sizeof(struct grid_entry*)*((numgrids+1)*2));
		LinkedList<GridEntry>[] grid = 
						(LinkedList<GridEntry>[]) new LinkedList[(numgrids+1)*2];
		for (int i=0; i < (((numgrids+1)*2)); i++) {
			//grid[i] = NULL;
			grid[i] = new LinkedList<GridEntry>();
		}

		for (int i=0; i < len1; i++) {
			//xloc = qx[len1-i-1];
			xloc = qx.getPoint(len1 - i - 1).getXPos();
			//lowerx = xloc - diffthreshold;
			lowerx = xloc - m_threshold;
			int j = 0;
			for (; j <(numgrids)*2+1; j++) {
				if (lowerx<bounds[j]) {
					break;
				}
			}
			xgrid_spot = j;

			//init_grid(xgrid_spot, grid, numgrids, diffthreshold, xloc, len1-i-1);
			GridEntry entry = new GridEntry();
			entry.low[0] = xloc - m_threshold;
			entry.high[0] = xloc + m_threshold;
			entry.series_id = len1-i-1;
			grid[xgrid_spot].addFirst(entry);

			if (xgrid_spot < (numgrids)*2+1)
				//init_grid(xgrid_spot+1, grid, numgrids, diffthreshold, xloc, len1-i-1);
			{
				GridEntry entry1 = new GridEntry();
				entry1.low[0] = xloc - m_threshold;
				entry1.high[0] = xloc + m_threshold;
				entry1.series_id = len1-i-1;
				grid[xgrid_spot+1].addFirst(entry1);				
			}
			
			if (xgrid_spot > 0)
				//init_grid(xgrid_spot-1, grid, numgrids, diffthreshold, xloc, len1-i-1);
			{
				GridEntry entry2 = new GridEntry();
				entry2.low[0] = xloc - m_threshold;
				entry2.high[0] = xloc + m_threshold;
				entry2.series_id = len1-i-1;
				grid[xgrid_spot-1].addFirst(entry2);	
			}
		}

		// maintain the best score in total_score_mem,
		// increment this tsm_ptr to correspond to the highest score.
		/*
		r_mem = (int*) malloc(sizeof(int)*len1+2);
		for (i=0; i<len1; i++) {
			r_mem[i] = -1;
		}
		//*/

		//total_score_mem = (int*) malloc(sizeof(int)*(len2+len1+5));
		int[] total_score_mem = new int[len2 + len1 + 5];
		total_score_mem[0] = -1;
		for (int i=1; i<len2+len1; i++) {
			total_score_mem[i] = len1+len2+2;
		}

		tsm_ptr =-1;

		//iterate through the elements of the 2nd time series.
		for (int i=0; i < len2; i++) {

			//determine which grid it fits in.
			//xloc = px[i];
			xloc = px.getPoint(i).getXPos();
			int j = 0;
			for (; j <(numgrids)*2+1; j++) {
				if (xloc<bounds[j]) {
					break;
				}
			}
			xgrid_spot = j;
			//entry = grid[xgrid_spot];
			//if (entry == NULL)
			//	continue;
			if (grid[xgrid_spot].size() == 0)
				continue;

			//if there are entries in that grid square,
			//k = entry->num_in_grid_square;
			int k = grid[xgrid_spot].size();
			c_temp=-1;
			c_k=0;
			for (j=0; j<k; j++) {
				//compare with the entries
				/*
				if (entry->low[0] <= xloc && xloc <= entry->high[0]) {
					val = entry->series_id;
					entry = entry->next;
				} else {
					entry = entry->next;
					continue;
				}
				//*/
				GridEntry entry = grid[xgrid_spot].get(j);
				if (entry.low[0] <= xloc && xloc <= entry.high[0]) {
					val = entry.series_id;
					//entry = entry->next;
				} else {
					//entry = entry->next;
					continue;
				}

				//try to increase the best possible score using these entries.
				if (val > c_temp) {
					for (; total_score_mem[c_k] < val
							&& (c_k <= tsm_ptr || c_k <=1); c_k++)
						;
					c_temp = total_score_mem[c_k];
					if (total_score_mem[c_k] > val) {
						total_score_mem[c_k] = val;
					}
					if (tsm_ptr < c_k) {
						tsm_ptr = c_k;
					}
				}

			}
		}
		score = tsm_ptr;

		//clean up
		/*
		free(total_score_mem);
		free(r_mem);
		for (i=0; i < (((numgrids+1)*2)); i++) {
			entry = grid[i];
			while (entry!= NULL) {
				grid[i] = entry->next;
				free(entry);
				entry = grid[i];
			}
		}
		free(bounds);
		//*/
		
		System.out.println("tsm_ptr:" + tsm_ptr);
		
		//update for Swale
		//score = score*MatchReward+GapPenalty*(len1+len2-2*score);
		double finalscore = score*m_matchreward+m_gappenalty*(len1+len2-2*score);
		return (double)(0-finalscore);
	}	

	/* (non-Javadoc)
	 * @see core.DistanceOperator#toString()
	 */
	@Override
	public String toString() {
		return "SwaleOperator:\n" +
				"m_threshold: " + m_threshold + "\n" +
				"m_matchreward: " + m_matchreward + "\n" +
				"m_gappenalty: " + m_gappenalty + "\n";
	}

	@Override
	public double computeLowerBound(Trajectory tr1, Trajectory tr2)
			throws TrajectoryException {
		// TODO Auto-generated method stub
		return Double.MIN_VALUE;
	}

	@Override
	public boolean hasLowerBound() {
		return false;
	}

	@Override
	public boolean needTuning() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void tuneOperator(Collection<Trajectory> trainset,
			Collection<Integer> labelset, Classifier classifier) {
		Logger lg = ClassifierManager.getLogger();
		
		double bestt = 0;
		double bestp = 0;
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
		for (double p = 0; p <= m_matchreward; p+=1) {
			p = -p;
			lg.fine("tuning with penalty:" + (p));
			m_gappenalty = p;
			for (int t = 1; t <= 50; t++) {
				lg.fine("tuning with threshold:" + t * tstep);
				m_threshold = t * tstep;
				double error = tuneByLeaveOneOut(vdata, vlabels, classifier);
				if (error < besterror) {
					bestp = p;
					bestt = t * tstep;
					besterror = error;
				}
			}
		}
		
		lg.info("best p:" + bestp + "\t best t:" + bestt);
		// set the parameter
		m_gappenalty = bestp;
		m_threshold = bestt;
		
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
		String targetfile = "swale//traj25.dat";
		
		SwaleOperator op = new SwaleOperator();
		
		try {
			Trajectory query = new Trajectory(1, read(queryfile), op);
			Trajectory target = new Trajectory(2, read(targetfile), op);
			//*
			SwaleOperator.m_gappenalty = 0;
			SwaleOperator.m_matchreward = 1;
			SwaleOperator.m_threshold = 0.50;
			//*/
			
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

class Cell {
	//PriorityQueue<CellEntry> queue = new PriorityQueue<CellEntry>();
	LinkedList<CellEntry> queue = new LinkedList<CellEntry>();
	int numentries = 0;
	Region r;
	
	Cell(Region r) {
		this.r = r;
	}
	
	void insert(CellEntry e) {
		queue.addFirst(e);
		numentries++;
	}
}

class CellEntry implements Comparable {
	int id;
	Region mbr;
	
	CellEntry(int id, Region r) {
		this.id = id;
		this.mbr = r;
	}

	public int compareTo(Object o) {
		if (this.id > ((CellEntry)o).id) {
			return 1;
		}
		else if (this.id == ((CellEntry)o).id) {
			return 0;
		}
		else {
			return -1;
		}
	}
	
	
}

class GridEntry {
	double[] low = new double[2];
	double[] high = new double[2];
	
	int series_id;
}
