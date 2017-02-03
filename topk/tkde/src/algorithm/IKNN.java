package algorithm;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeMap;
import java.util.TreeSet;

import db.Dataset;
import spatialindex.spatialindex.*;
import spatialindex.rtree.*;

public class IKNN {
	private RTree tree;
	private TreeMap<Double,Point> T;
	private HashMap<String,String> trips;
	private Connection conn = null; 
	private Dataset ds = null;
	public long iotime;
	public long candis;
	
	public IKNN(RTree tree, String file, Dataset d, Connection c) throws SQLException{
		// Initializing values
		this.tree = tree;
		this.trips = this.readTrips(file);
		this.ds = d;
		this.conn = c;
	}
	
	private HashMap<String, String> readTrips(String FilePath){
	  // Hash to store POI and corresponding trajectory IDs
	  HashMap<String, String> trips = new HashMap<String, String>();
	  try {
	   InputStreamReader read = new InputStreamReader(new FileInputStream(FilePath), "utf-8");
	   BufferedReader reader = new BufferedReader(read);
	   String line;
	   while ((line = reader.readLine()) != null) {
	    String []arr = line.split(",");
	    // Parsing value and adding into hash map
	    String tmp = Double.parseDouble(arr[1])+","+Double.parseDouble(arr[2]);
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
	
	public String computeIKNN(Region query, Point []points) throws SQLException{
		int k = Settings.k;
		iotime = 0;
		String output = "";
		int lambda = k;
		// stores candidate trajectories
		HashMap<String, ArrayList<Point>> candidates = new HashMap<String, ArrayList<Point>>();
		// Stores upper bound values
		ArrayList<Point> UB_points = new ArrayList<Point>();
		for (int i = 0; i < points.length; i++) {
			UB_points.add(null);
		}
		int index = 0;
		int iteration=0;
		int counter = 0;
		int check = 0;
		while (check == 0){
			
			for (int i = 0; i < points.length; i++) {
				// Starts search from first query points and iterates
				index = i;
				ArrayList<Point> S = new ArrayList<Point>();
				long startTime = System.currentTimeMillis();
				// finds lambda nearest points to given query point
				this.getIntersectingPoints(S, lambda, points[i]);
				long stopTime = System.currentTimeMillis();
				long elapsedTime = stopTime - startTime;
				iotime += elapsedTime;
				counter = S.size();
				//System.out.println("Points: " + S.size());
				// stores the farthest point as an upper bound 
				for (Point point : S) {
					if(UB_points.get(index) == null){
						UB_points.set(index, point);
					}
					else{
						if(compareDistance(points[index], UB_points.get(index), point) == 0){
							UB_points.set(index, point);
						}
					}
					// finds trajectory ids for each point and stores trajectories in candidate set
					String ids = trips.get(point.getCoord(0)+","+point.getCoord(1));
					String []t = null;
					if(ids.contains(",")){
						t = ids.split(",");					
						for (String id : t) {
							if(candidates.containsKey(id)){
								
								ArrayList<Point> pois = candidates.get(id);
								if(pois.get(index) != null){
									// if we find closer point to corresponding query point we updates slot
									if(compareDistance(points[index], pois.get(index), point) == 1){
										pois.set(index, point);
									}
								}
								else{
									pois.set(index, point);
								}
								candidates.put(id, pois);
							}
							else{
								ArrayList<Point> pois = new ArrayList<Point>();
								for (int j = 0; j < points.length; j++) {
									pois.add(j, null);
								}
								pois.set(index, point);
								candidates.put(id, pois);
							}
						}
					}
					else{
						if(candidates.containsKey(ids)){
							ArrayList<Point> pois = candidates.get(ids);
							//System.out.println(pois.get(index));
							if(pois.get(index) != null){
								if(compareDistance(points[index], pois.get(index), point) == 1){
									pois.set(index, point);
								}
							}
							else{								
								pois.set(index, point);
							}
							
							candidates.put(ids, pois);
						}
						else{
							ArrayList<Point> pois = new ArrayList<Point>();
							for (int j = 0; j < points.length; j++) {
								pois.add(j, null);
							}
							pois.set(index, point);
							candidates.put(ids, pois);
						}
					}
				}
			}
			
			if(candidates.size() >= k){
				PriorityQueue<Double> LB = new PriorityQueue<>(Collections.reverseOrder());
				// add candidate trajectory distance to query in descending order
				for (Map.Entry<String, ArrayList<Point>> entry : candidates.entrySet()) {
					double tmp_dist = computeLowerBound(entry.getValue(), points);
					LB.add(tmp_dist);
				}
				
				for (int i = 1; i < k; i++) {
					LB.poll();
				}
				// Choose k-th lower bound value
				double k_LB = LB.peek();
				// computes upper bound value using farthest point we found so far for each query point.
				double UB = computeUpperBound(UB_points, points);
				if(k_LB >= UB){
					check = 1;
				}
			}
			// increase lambda value by 50 and continues
			lambda += 50;
			iteration++;
			//System.out.println("Candidates:" + candidates.size());
		} 
		
		// stores top-k results
		PriorityQueue<Candidate> resultSet = new PriorityQueue<>();
		// candidates are stored in descending order of distance
		PriorityQueue<Candidate> sorted_candidates = new PriorityQueue<>(Collections.reverseOrder());
		for (Map.Entry<String, ArrayList<Point>> entry : candidates.entrySet()) {
			double candidate_ub = computeCandidateUpperBound(entry.getValue(), points, UB_points);
			sorted_candidates.add(new Candidate(candidate_ub, entry.getKey()));
		}
		int c = 0;
		while( sorted_candidates.peek() != null) {
		    Candidate tmp = sorted_candidates.poll();
			String tmp_id = tmp.getID();
			// computes actual distance reading trajectory points from database;
			double tmp_dist = computeCandidateDistance(tmp_id, points);
			// adds first k candidates to resultset
			if(c < k){
				resultSet.add(new Candidate(tmp_dist, tmp_id));				
			}
			else{
				Candidate top = resultSet.peek();
				// updates result set if distance is shorter
				if(tmp_dist > top.getDistance()){
					resultSet.poll();
					resultSet.add(new Candidate(tmp_dist, tmp_id));
				}
				if(sorted_candidates.peek()!= null){
					if(top.getDistance() >= sorted_candidates.peek().getDistance()){
						break;
					}
				}				
			}
			c++;
		}
		
		
		// format top-k results
		while( resultSet.peek() != null) {
			output += resultSet.poll().getID()+",";
		}
		output = output.substring(0, output.length()-1);
		this.candis = candidates.size();
		System.out.println("Iteration: "+iteration+ " Candidates: "+candidates.size() + " Points:" +counter);
		return output;
	}
	 
	public int getIntersectingPoints(ArrayList<Point> s, int lambda, Point p){
		 MyVisitor v =  new MyVisitor();
		 // finds the nearest lambda points for given query point p
		 tree.nearestNeighborQuery(lambda, p, v);
		 
		 for (Map.Entry<Integer, IShape> entry : v.answers.entrySet()) {
		    IShape value = entry.getValue();
		    double[] coord = value.getCenter();
		    s.add(new Point(coord));
		 }
		 return 1;
	} 
	
	private double computeLowerBound(ArrayList<Point> p, Point[] points){
		double dist=0;
		for (int i = 0; i < points.length; i++) {
			if(p.get(i) != null){
				double d = p.get(i).getMinimumDistance(points[i]);
				d = d * (-1);
				dist += Math.exp(d);
			}			
		}
		return dist;		
	}
	// computes upper bound using corresponding points 
	private double computeUpperBound(ArrayList<Point> p, Point[] points){
		double dist=0;
		for (int i = 0; i < points.length; i++) {
			double d = p.get(i).getMinimumDistance(points[i]);
			d = d * (-1);
			dist += Math.exp(d);			
		}
		return dist;		
	}
	
	private double computeCandidateUpperBound(ArrayList<Point> p, Point[] points, ArrayList<Point> UB_points){
		double dist=0;
		for (int i = 0; i < points.length; i++) {
			// if we did not find matched point we use current search radius for given query point
			if(p.get(i) == null){
				double d = UB_points.get(i).getMinimumDistance(points[i]);
				d = d * (-1);
				dist += Math.exp(d);
			}
			else{
				double d = p.get(i).getMinimumDistance(points[i]);
				d = d * (-1);
				dist += Math.exp(d);
			}			
		}
		return dist;		
	}
	
	// Compute actual distance of the trajectory to query points
	public double computeCandidateDistance(String id, Point[] points) throws SQLException{
		double dist=0;
		long startTime = System.currentTimeMillis();
		// loading all points of the trajectory from database using id 
		ArrayList<Point> pois = ds.loadTrajectoryPoints(conn, id);
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		iotime += elapsedTime;
		// stores the nearest point for each query point
		ArrayList<Point> matched = new ArrayList<Point>();
		for (int j = 0; j < points.length; j++) {
			matched.add(j, null);
		}
		for (int i = 0; i < pois.size(); i++) {
			for (int j = 0; j < points.length; j++) {
				if(matched.get(j) == null){
					matched.set(j, pois.get(i));
				}
				else{
					// updates corresponding matched point if the distance is shorter 
					if(compareDistance(points[j], matched.get(j), pois.get(i)) == 1){
						matched.set(j, pois.get(i));
					}
				}
			}
			
		}
		
		dist = computeUpperBound(matched, points);
		return dist;		
	}
	// compare the distance of two points to given point
	private double compareDistance(Point a, Point b, Point c){
		double dist_ab = a.getMinimumDistance(b);
		double dist_ac = a.getMinimumDistance(c);
		
		if(dist_ab > dist_ac){
			return 1;
		}
		return 0;		
	}
}


