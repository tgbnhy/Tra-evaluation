package algorithm;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

import spatialindex.grid.Grid;
import spatialindex.rtree.RTree;
import spatialindex.spatialindex.*;
import spatialindex.storagemanager.DiskStorageManager;
import spatialindex.storagemanager.IStorageManager;
import spatialindex.storagemanager.PropertySet;

public class SGRA {
	private Grid grid;
	private RTree tree;
	private TreeMap<Double,Point> T;
	private HashMap<String,String> trips;
	private HashMap<Integer, Integer> grids;
	private IStorageManager diskfile;
	private PropertySet pset;
	public long iotime;
	public long ann;
	public long candis;
	
	public SGRA(Grid g, String file, RTree tree) throws IOException{
		this.grid = g;
		this.tree = tree;
		// reading trip ID information from file
		this.trips = this.readTrips(file);
		this.grids = new HashMap<Integer, Integer>();
		LineNumberReader location_reader = new LineNumberReader(new FileReader(Settings.grid_info_location));
		String line;
		String[] temp;
		while ((line = location_reader.readLine()) != null)
		{
			temp = line.split(" ");
			grids.put(Integer.valueOf(temp[0]), Integer.valueOf(temp[1]));
		}
		location_reader.close();
		PropertySet ps = new PropertySet();
		ps.setProperty("FileName", Settings.grid_index_location);
		diskfile = new DiskStorageManager(ps);
		pset = new PropertySet();
	}
	
	private HashMap<String, String> readTrips(String FilePath){
	  HashMap<String, String> trips = new HashMap<String, String>();
	  try {
	   InputStreamReader read = new InputStreamReader(new FileInputStream(FilePath), "utf-8");
	   BufferedReader reader = new BufferedReader(read);
	   String line;
	   while ((line = reader.readLine()) != null) {
	    String []arr = line.split(",");
	    String tmp = Double.parseDouble(arr[1])+","+Double.parseDouble(arr[2]);
	    // hash key is comma separated latitude and longitude
	    // hash value is trip IDs
	    if(trips.containsKey(tmp)){
	    	String ids = trips.get(tmp);
	    	if(!ids.contains(arr[0])){
	    		trips.put(tmp, trips.get(tmp)+","+arr[0]);
	    	}    	
	    }
	    else{
	    	trips.put(tmp, arr[0]);
	    }
	    
	   } reader.close();
	  } catch (Exception e) {
	   e.printStackTrace();
	  }
	  return trips;
	}
	
	public String computeSGRA(Region query, Point []points) throws IOException{
		double []rad = new double[points.length]; // stores current search radius for each query point
		double []theta = new double[points.length]; // stores maximum search range for each query point
		double best_dist = 0; // stores distance of top k-th trajectory to all query points
		iotime = 0; // stores input/output time
		ann = 0;
		int [][]range =  new int[points.length][2];
		// buffer stores false positives for each query point
		ArrayList<HashMap<Double,String>> buffers = new ArrayList<HashMap<Double,String>>();
		ArrayList<Integer> cells = new ArrayList<Integer>(); 
		
		
		int []result = new int[points.length];
		// hashmap stores candidates
		HashMap<String, ArrayList<Point>> candidates = new HashMap<String, ArrayList<Point>>();
		long start = System.currentTimeMillis();
		
		for (int i = 0; i < points.length; i++) {
			buffers.add(new HashMap<Double, String>());
			result[i]= 0;
			// initialize cell range for each query point 
			range[i][0] = Settings.size + 1;
			range[i][1] = 0;			
		}
		
		double min_lat = Settings.min_lat;
		double max_lat = Settings.max_lat;
		double min_lng = Settings.min_lng;
		double max_lng = Settings.max_lng;
		
		// normalize query points to range of [0, 1] using maximum and minimum latitude and longitude information 
		Point []norm_points = new Point[points.length];
		double[] f1 = new double[2];
		for (int i = 0; i < norm_points.length; i++) {
			f1[1] = (points[i].getCoord(0) - min_lat)/(max_lat - min_lat);
			f1[0] = (points[i].getCoord(1) - min_lng)/(max_lng - min_lng);
			
			Point p = new Point(f1);
			norm_points[i] = p;
		}
		
		PriorityQueue<Candidate> topk = computeUBk(query, points);
		
		best_dist = topk.peek().getDistance();
		
		long end = System.currentTimeMillis();
		this.ann = (end-start);
		//System.out.println("SGRA Start: " + (end-start));
		
		for (int i = 0; i < points.length; i++) {
			rad[i] = 0;
			theta[i] = best_dist;
		}
		
		//double inc = (double)1/Settings.dimension;
		double inc = Settings.range_inc;
		double gradius = (max_lat - min_lat);
		
		int check = 1;
		int min = 0;
		int index = 0;
		int iteration = 0;
		int counter = 0;
		
		while (check == 1){
			// choose first query point as the point to search
			min = result[0];
			index = 0;
			// finds query point index that should be search at next iteration
			for (int i = 0; i < result.length; i++) {
				if(result[i] < min){
					min = result[i];
					index = i;
				}
			}
			// increment query range for normalized value
			rad[index] += (inc / gradius);
			
			
			ArrayList<Point> S = new ArrayList<Point>(); // stores points found by search for given query point
			long startTime = System.currentTimeMillis();
			// finds intersecting cells with query region
			String rids = this.getIntersectingPoints(S, rad[index], norm_points[index], range[index][0], range[index][1], buffers.get(index), cells);
			counter += S.size();
			// stores intersecting cells start and end ID for current search
			range[index][0] = Integer.valueOf(rids.split(",")[0]);
			range[index][1] = Integer.valueOf(rids.split(",")[1]);
			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;
			iotime += elapsedTime;
			
			for (Point point : S) {
				String ids = trips.get(point.getCoord(0)+","+point.getCoord(1));
				String []t = null;
				if(ids.contains(",")){ // if multiple trajectories intersect with the point
					t = ids.split(",");
					// finding the nearest point to each query point for all candidate trajectories
					for (String id : t) {
						// candidate found already
						if(candidates.containsKey(id)){
							ArrayList<Point> pois = candidates.get(id);
							for (int i = 0; i < pois.size(); i++) {
								if(compareDistance(points[i], pois.get(i), point) == 1){
									pois.set(i, point);
								}
							}
							
							candidates.put(id, pois);
						}
						// new candidate
						else{
							ArrayList<Point> pois = new ArrayList<Point>();
							for (int i = 0; i < points.length; i++) {
								pois.add(i, point);
							}
							candidates.put(id, pois);
						}
					}
				}
				else{
					// setting nearest points for trajectory with unique POI
					if(candidates.containsKey(ids)){
						ArrayList<Point> pois = candidates.get(ids);
						for (int i = 0; i < pois.size(); i++) {
							if(compareDistance(points[i], pois.get(i), point) == 1){
								pois.set(i, point);
							}
						}
						
						candidates.put(ids, pois);
					}
					else{
						
						ArrayList<Point> pois = new ArrayList<Point>();
						for (int i = 0; i < points.length; i++) {
							pois.add(i, point);
						}
						candidates.put(ids, pois);
					}
				}
				
				
			}
			// update result count for current query point index
			result[index] = S.size();
			// update upper bound value using current candidates
			topk = updateUBk(points, candidates, topk);
			best_dist = topk.peek().getDistance();
			
			// decreasing maximum search range using current search range for each query point
			for (int i = 0; i < points.length; i++) {
				theta[i] = best_dist;
				for (int j = 0; j < points.length; j++) {
					if(j != i){
						theta[i] -= rad[j]*gradius;
					}
				}
				if(theta[i] < (rad[i]*gradius)){
					check = 0;
				}
			}
			
			iteration++;
		}
		
		// returning topk value separated by comma
		String output = "";
		while( topk.peek() != null) {
			output += topk.poll().getID()+",";			
		}
		output = output.substring(0, output.length()-1);
		this.candis = candidates.size();
		System.out.println("Iteration: "+iteration+ " Candidates: "+candidates.size() + " Points:" +counter);
		return output;
	}
	 
	
	private PriorityQueue<Candidate> updateUBk(Point []q, HashMap <String, ArrayList<Point>> c, PriorityQueue<Candidate> topk){
		
		HashSet<String> current = new HashSet<>();
		PriorityQueue<Candidate> new_topk = new PriorityQueue<>(Collections.reverseOrder());
		double tmp = 0;
		while( topk.peek() != null) {
			Candidate tmp_can= topk.poll();
			tmp = computeCandidateDistance(c.get(tmp_can.getID()), q);
			if(tmp < tmp_can.getDistance()){
				new_topk.add(new Candidate(tmp, tmp_can.getID()));
			}
			else{
				new_topk.add(tmp_can);
			}
			current.add(tmp_can.getID());
		}
		
		for (Map.Entry<String, ArrayList<Point>> entry : c.entrySet()) {
			// compute aggregated distance of candidate to query points
			tmp = computeCandidateDistance(entry.getValue(), q);
			// if we found shorter distance topk is updated
			if(new_topk.peek().getDistance() > tmp){
				if(!current.contains(entry.getKey())){
					new_topk.poll();
					new_topk.add(new Candidate(tmp, entry.getKey()));
				}
			}			
		    
		}
		
		return new_topk;
	}
	
private PriorityQueue<Candidate> computeUBk(Region query, Point []points){
		
		HashMap<Point, String> k_ANN = new HashMap<Point, String>();
		PriorityQueue<Candidate> topk = new PriorityQueue<>(Collections.reverseOrder());
		// finds k points where aggregated distance to query is minimum
		int result = this.getANN(k_ANN, Settings.k, query);
		
		for (Map.Entry<Point, String> entry : k_ANN.entrySet()) {
			// computing distance and adding in queue
			double tmp = computeTotalDistance(entry.getKey(), points);
			topk.add(new Candidate(tmp, entry.getValue()));
			//System.out.println("Top:" + entry.getValue());
			//System.out.println("ANN: " + entry.getValue() + " " + entry.getKey().getCoord(0) + "," +entry.getKey().getCoord(1) );
			// if queue size is greater than k value we remove maximum distance
			if(topk.size() == (Settings.k+1)){
				topk.poll();
			}
		}
		
		return topk;
	}
	
	public int getANN(HashMap<Point, String> k_ANN, int k, Region r){
		int check = 0;
		while(check == 0){
			MyVisitor v = new MyVisitor();
			// finds nearest neighbors and stores in MyVisitor object
			tree.nearestNeighborQuery(k, r, v);
			for (Map.Entry<Integer, IShape> entry : v.answers.entrySet()) {
			    //int key = entry.getKey();
			    IShape value = entry.getValue();
			    // finds corresponding trajectory IDs for each point
			    String []ids = trips.get(value.getCenter()[0]+","+value.getCenter()[1]).split(",");
			    for (String id : ids) {
			    	if(!k_ANN.containsValue(id)){
			    		double[] p = {value.getCenter()[0], value.getCenter()[1]};	
				    	k_ANN.put(new Point(p), id);
				    	// stops if we found k results
			    	}
			    	
			    	if(k_ANN.size() == Settings.k){
			    		check = 1;
			    		return 1;
			    	}
			    }	
			}
			k += k;
		}
		
		return 0;
	 } 
	 
	 public String getIntersectingPoints(ArrayList<Point> s, double r, Point p, int start, int end, HashMap<Double, String> buffer, ArrayList<Integer> scanned_cells){
		 long sTime = System.currentTimeMillis();
		 double[] f1 = new double[2];
		 double[] f2 = new double[2];
		 // computing query region using point and search radius
		 f1[0] = p.getCoord(0) - r; f1[1] = p.getCoord(1) + r;
		 f2[0] = p.getCoord(0) + r; f2[1] = p.getCoord(1) - r;
		 double[] t1 = new double[2];
		 double[] t2 = new double[2];
		 t1[0] = p.getCoord(0) - r; t1[1] = p.getCoord(1) - r;
		 t2[0] = p.getCoord(0) + r; t2[1] = p.getCoord(1) + r;
		 Region query = new Region(t1, t2);
		 Point ps = new Point(f1);
		 Point pe = new Point(f2);
		 // getting start and end cell IDs that intersect with query region
		 String ids =  grid.getIntersection(grid.zordered_mbrs, query);
		 
		 String []index = ids.split(",");
		 for (int k = Integer.valueOf(index[0]); k <= Integer.valueOf(index[1]); k++) {
			 // iterates all cells in the range and checks whether it has already processed 
			 if(k < start || k > end){
				 
				 Region tmp = grid.zordered_mbrs.get(k-1);
				 //System.out.println("Cell "+k+": " + tmp.toString(Settings.min_lat, Settings.max_lat, Settings.min_lng, Settings.max_lng));
				 // Checks whether cell is false positive
				 if(tmp.intersects(query)){
					 //System.out.println("K:"+k);
					 if(!scanned_cells.contains(k-1)){
						 // gets all points using cell id
						 ArrayList<Point> tmp_points = getPointsByCell(k-1);
						 scanned_cells.add(k-1);
						 // adding into array list of points
						 for (int i = 0; i < tmp_points.size(); i++) {
							s.add(tmp_points.get(i));
						 }
					 }					 
				 }
				 // the cell is false positive
				 else{
					 // finding appropriate buffer to insert cell ID
					 double exp = r*2;
					 int intersect=0;
					 while(intersect==0){
						 double[] p1 = new double[2];
						 double[] p2 = new double[2];
						 p1[0] = p.getCoord(0) - exp; p1[1] = p.getCoord(1) - exp;
						 p2[0] = p.getCoord(0) + exp; p2[1] = p.getCoord(1) + exp;
						 Region q = new Region(p1, p2);
						 //System.out.println(q.toString());
						 if(q.intersects(tmp)){
							 intersect = 1;
							 if(buffer.containsKey(exp)){
								 buffer.put(exp, buffer.get(exp)+","+k);
							 }
							 else{
								 buffer.put(exp, String.valueOf(k));
							 }						 
						 }
						 exp += r;
					 }
					 
				 }
			 }
		 }		
		 // computes cells that are already in the buffer
		 String cells = buffer.get(r);
		 if(cells != null){
			 String []cell = cells.split(",");
			 for (int i = 0; i < cell.length; i++) {
				 int current = Integer.parseInt(cell[i])-1;
				 if(!scanned_cells.contains(current)){
					 ArrayList<Point> tmp_points = getPointsByCell(current); 
					 //System.out.println(tmp_points.size()+" " + k);
					 for (int t = 0; t < tmp_points.size(); t++) {
						s.add(tmp_points.get(t));
					 }
					 scanned_cells.add(current);
				 }			 	 
			 }			 
		 }
		 /*
		 System.out.println("Buffer: " + buffer.size());
		 for (Map.Entry<Double, String> entry : buffer.entrySet()) {
		    System.out.println(entry.getKey()+" " + entry.getValue());
		 }
		 */
		 //System.out.println("S: "+s.size());
		 return ids;
	 } 
	 
	 private ArrayList<Point> getPointsByCell(int id){
		ArrayList<Point> r = new ArrayList<Point>();
		// returns all points in the cell
		if(grids.containsKey(id)){
			int index_id = grids.get(id);
			MyVisitor v = new MyVisitor();
			pset.setProperty("IndexIdentifier", index_id);
			RTree tree = new RTree(pset, diskfile);
			tree.rangeCellQuery(v);
			for (Map.Entry<Integer, IShape> entry : v.answers.entrySet()) {
			    IShape value = entry.getValue();
			    double[] coord = value.getCenter();
			    r.add(new Point(coord));
			}
		}
		
		return r;
	 }	
	 	
	 private double computeTotalDistance(Point p, Point[] points){
		double dist=0;
		for (int i = 0; i < points.length; i++) {
			dist += p.getMinimumDistance(points[i]);
		}
		return dist;		
	 }	
	
	 private double computeCandidateDistance(ArrayList<Point> p, Point[] points){
		double dist=0;
		for (int i = 0; i < points.length; i++) {
			dist += p.get(i).getMinimumDistance(points[i]);
		}
		return dist;		
	 }
	
	 private double compareDistance(Point a, Point b, Point c){
		double dist_ab = a.getMinimumDistance(b);
		double dist_ac = a.getMinimumDistance(c);
		
		if(dist_ab > dist_ac){
			return 1;
		}
		return 0;		
	}
}


