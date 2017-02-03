package algorithm;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

import spatialindex.spatialindex.*;
import spatialindex.rtree.*;

public class SRA {
	private RTree tree;
	private TreeMap<Double,Point> T;
	private HashMap<String,String> trips;
	public long iotime;
	public long ann;
	public long candis;
	
	public SRA(RTree tree, String file){
		this.tree = tree; // creating RTree object
		this.trips = this.readTrips(file); // read trip ids for each unique point.
	}
	
	private HashMap<String, String> readTrips(String FilePath){
	  HashMap<String, String> trips = new HashMap<String, String>(); // hash for storing trips
	  try {
		  // reading trip ID information from file
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
	
	public String computeSRA(Region query, Point []points){
		double []rad = new double[points.length]; // stores current search radius for each query point
		double []theta = new double[points.length]; // stores maximum search range for each query point
		double best_dist = 0; // stores distance of top k-th trajectory to all query points
		iotime = 0; // stores input/output time
		ann = 0; // stores time to compute aggregated nearest neighbor
		long start = System.currentTimeMillis();
		int []result = new int[points.length]; 
		HashMap<String, ArrayList<Point>> candidates = new HashMap<String, ArrayList<Point>>(); // stores candidates
		
		for (int i = 0; i < points.length; i++) {
			// stores number of results for each query point
			result[i]= 0;
		}
		
		PriorityQueue<Candidate> topk = computeUBk(query, points); // compute k-th upper bound value
		best_dist = topk.peek().getDistance();
		
		for (int i = 0; i < points.length; i++) {
			rad[i] = 0;
			theta[i] = best_dist;
		}
		
		long end = System.currentTimeMillis();
		ann = (end-start);
				
		double inc = Settings.tree_inc;
		int check = 1;
		int min = 0;
		int index = 0;
		int iteration=0;
		int counter = 0;
		// iterate until check value is changed to 1
		while (check == 1){
			// choose first query point as the point to search 
			min = result[0];
			index = 0;
			// finds query point index that should be search at next iteration
			for (int i = 0; i < result.length; i++) {
				if(result[i] < min){
					//System.out.println("Index: "+ i + " " + result[i] + " " + min);
					min = result[i];
					index = i;
				}
			}
			//System.out.println("i: " + index+ " size: " + result[index] +" "+ result[0] +" "+ result[1]);
			rad[index] += inc; // increment query range
			ArrayList<Point> S = new ArrayList<Point>(); // stores points found by search for given query point
			long startTime = System.currentTimeMillis();
			this.getIntersectingPoints(S, rad[index], inc, points[index]); // finds all points intersect with query region
			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;
			iotime += elapsedTime;
			counter += S.size(); // increments number of points found so far
			for (Point point : S) {
				String ids = trips.get(point.getCoord(0)+","+point.getCoord(1));
				//System.out.println(ids+" " + point.getCoord(0) + " "+point.getCoord(1) );
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
						theta[i] -= rad[j];
					}
				}
				if(theta[i] < rad[i]){
					check = 0;
				}
			}
			// increment iteration by one
			iteration++;
		}
		
		String output = "";
		
		// returning topk value separated by comma
		while( topk.peek() != null) {
			output += topk.poll().getID()+",";
			//System.out.println("Distance: "+topk.poll().getDistance());
		}
		output = output.substring(0, output.length()-1);
		this.candis = candidates.size();
		System.out.println("Iteration: "+iteration+ " Candidates:"+candidates.size() + " Points:" +counter);
		return output;
	}
	 
	
	private PriorityQueue<Candidate> updateUBk(Point []q, HashMap <String, ArrayList<Point>> c, PriorityQueue<Candidate> topk){
		
		for (Map.Entry<String, ArrayList<Point>> entry : c.entrySet()) {
			// compute aggregated distance of candidate to query points
			double tmp = computeCandidateDistance(entry.getValue(), q);
			// if we found shorter distance topk is updated
			if(topk.peek().getDistance() > tmp){
				topk.poll();
				topk.add(new Candidate(tmp, entry.getKey()));
			}			
		    
		}
		
		return topk;
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
			
			//System.out.println("ANN: " + entry.getValue() + " " + entry.getKey().getCoord(0) + "," +entry.getKey().getCoord(1) );
			// if queue size is greater than k value we remove maximum distance
			if(topk.size() == (Settings.k+1)){
				topk.poll();
			}
		}
		
		return topk;
	}
	
	public int getANN(HashMap<Point, String> k_ANN, int k, Region r){
		
		MyVisitor v = new MyVisitor();
		// finds nearest neighbors and stores in MyVisitor object
		tree.nearestNeighborQuery(k, r, v); 
		
		for (Map.Entry<Integer, IShape> entry : v.answers.entrySet()) {
		    //int key = entry.getKey();
		    IShape value = entry.getValue();
		    // finds corresponding trajectory IDs for each point
		    String []ids = trips.get(value.getCenter()[0]+","+value.getCenter()[1]).split(",");
		    for (String id : ids) {
		    	double[] p = {value.getCenter()[0], value.getCenter()[1]};	
		    	k_ANN.put(new Point(p), id);
		    	// stops if we found k results
		    	if(k_ANN.size() == Settings.k){
		    		return 1;
		    	}
		    }	
		}
		return 0;
	 } 
	 
	 public int getIntersectingPoints(ArrayList<Point> s, double r, double inc, Point p){
		 MyVisitor v =  new MyVisitor();
		 double o = r - inc;
		 double[] f1 = new double[2];
		 double[] f2 = new double[2];
		 f1[0] = p.getCoord(0) - r; f1[1] = p.getCoord(1) - r;
		 f2[0] = p.getCoord(0) + r; f2[1] = p.getCoord(1) + r;
		 // Computes old and new search region to do incremental search
		 Region new_query = new Region(f1, f2);
		 Region old_query = null;
		 if(r != inc){
			 f1[0] = p.getCoord(0) - o; f1[1] = p.getCoord(1) - o;
			 f2[0] = p.getCoord(0) + o; f2[1] = p.getCoord(1) + o;
			 
			 old_query = new Region(f1, f2);
		 }
		 //System.out.println("SRA Region: " + query.toString());
		 tree.rangeIncrementalQuery(new_query, old_query, v);
		 // Stores each point in ArrayList of points
		 for (Map.Entry<Integer, IShape> entry : v.answers.entrySet()) {
		    IShape value = entry.getValue();
		    double[] coord = value.getCenter();
		    s.add(new Point(coord));
		 }
		 return 1;
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


