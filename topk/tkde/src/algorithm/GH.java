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

import com.sun.xml.internal.ws.api.config.management.policy.ManagementAssertion.Setting;

import db.Dataset;
import spatialindex.spatialindex.*;
import spatialindex.rtree.*;

public class GH {
	private RTree tree;
	private TreeMap<Double,Point> T;
	private HashMap<String,String> trips;
	private Connection conn = null; 
	private Dataset ds = null;
	public long iotime;
	public long candis;
	
	public GH(RTree tree, String file, Dataset d, Connection c) throws SQLException{
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
	
	public String computeGH(Region query, Point []points) throws SQLException{
		int k = Settings.k;
		iotime = 0;
		String output = "";
		int counter = 0;
		// Stores full-matching trajectory id
		TreeSet<String> full = new TreeSet<String>();
		// Stores current search radius of each query point 
		double []rad = new double[points.length];
		// Initializing individual heaps for all query points
		ArrayList<PriorityQueue<Element>> IH= new ArrayList<PriorityQueue<Element>>();
		for(int i = 0; i < points.length; i++){
			IH.add(new PriorityQueue<Element>());
		}
		
		// Stores candidate trajectories
		HashMap<String, ArrayList<Point>> candidates = new HashMap<String, ArrayList<Point>>();
		Queue<Element> GH = new PriorityQueue<>();
		
		double inc = Settings.tree_inc; 
		for (int i = 0; i < points.length; i++) {
			rad[i] = inc;
			ArrayList<Point> S = new ArrayList<Point>();
			long startTime = System.currentTimeMillis();
			// finds intersecting points with region of current query point
			this.getIntersectingPoints(S, rad[i], inc, points[i]);
			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;
			iotime += elapsedTime;
			counter += S.size();
			//System.out.println("Points: " + S.size());
			// adds each point in individual heap with corresponding index
			for (Point point : S) {
				double dist = points[i].getMinimumDistance(point);
				IH.get(i).add(new Element(dist, point, i));
			}
		}
			
		// Adds first point of each individual heap to global heap
		for (int i = 0; i < points.length; i++) {
			GH.add(IH.get(i).poll());
		}
				
		int index = 0;
		int iteration=0;
		int full_match = 0;
		while (full_match < k){
			
			Element e = GH.poll();
			Point point = e.getPoint();
			index = e.getIndex();
			// checks and finds more point if individual heap is empty by adding search radius
			while(IH.get(index).size() == 0){
				rad[index] += inc;
				ArrayList<Point> S = new ArrayList<Point>();
				this.getIntersectingPoints(S, rad[index], inc, points[index]);
				counter += S.size();
				for (Point p : S) {
					double dist = points[index].getMinimumDistance(p);
					IH.get(index).add(new Element(dist, p, index));
				}
			}
			GH.add(IH.get(index).poll());
			// finds trajectory ids for given point
			String ids = trips.get(point.getCoord(0)+","+point.getCoord(1));
			String []t = null;
			if(ids.contains(",")){
				t = ids.split(",");					
				for (String id : t) {
					// candidate found already
					if(candidates.containsKey(id)){
						
						ArrayList<Point> pois = candidates.get(id);
						if(pois.get(index) != null){
							if(compareDistance(points[index], pois.get(index), point) == 1){
								pois.set(index, point);
							}
						}
						else{
							pois.set(index, point);
						}
						int check  = 1;
						for (int i = 0; i < pois.size(); i++) {
							if(pois.get(i) == null){
								check *= 0;
							}
						}
						// checks whether we found full matching candidates
						if(check == 1){
							full_match++;
							full.add(id);
							//System.out.println("Full match: " + full_match);
						}
						candidates.put(id, pois);
					}
					// new candidate
					else{
						ArrayList<Point> pois = new ArrayList<Point>();
						for (int i = 0; i < points.length; i++) {
							pois.add(i, null);
						}
						pois.set(index, point);
						candidates.put(id, pois);
					}
				}
			}
			else{
				if(candidates.containsKey(ids)){
					ArrayList<Point> pois = candidates.get(ids);
					//checks whether current point is closer than the point found so far 
					if(pois.get(index) != null){
						if(compareDistance(points[index], pois.get(index), point) == 1){
							pois.set(index, point);
						}
					}
					else{
						// if slot is empty we add point directly
						pois.set(index, point);
					}
					int check  = 1;
					for (int i = 0; i < pois.size(); i++) {
						if(pois.get(i) == null){
							check *= 0;
						}
					}
					if(check == 1){
						full_match++;
						full.add(ids);
						//System.out.println("Full match: " + full_match);
					}
					candidates.put(ids, pois);
				}
				else{
					ArrayList<Point> pois = new ArrayList<Point>();
					for (int i = 0; i < points.length; i++) {
						pois.add(i, null);
					}
					pois.set(index, point);
					candidates.put(ids, pois);
				}
			}
			//System.out.println("Candidates:" + candidates.size());
			iteration++;
		} 
		// stores top-k results
		PriorityQueue<Candidate> resultSet = new PriorityQueue<>(Collections.reverseOrder());
		
		Iterator<String> iterator = full.iterator();
		// computes distance of full matching candidates and adds in result set     
		while (iterator.hasNext()){
			String id = iterator.next().toString();
			double total_dist = computeDistance(candidates.get(id), points);
			resultSet.add(new Candidate(total_dist, id));
			candidates.remove(id);
		}
		
		// stores k-th value
		double best = resultSet.peek().getDistance();
		//System.out.println("Best dist: "+ best);
		Point[] elements = new Point[points.length];
		//Stores each point in global heap to elements array
		for (Element e : GH) {
		    elements[e.getIndex()] = e.getPoint();
		    //System.out.println("GH: " + e.getIndex()+ " " + e.getDistance());
		}
		
		counter = 0;
		
		for (Map.Entry<String, ArrayList<Point>> entry : candidates.entrySet()) {
		    // fills empty slots of partial matching candidates with corresponding points in global heap
			ArrayList<Point> tmp = new ArrayList<Point>();
			int j = 0;
			for (Point p : entry.getValue()) {
				if(p != null){
					tmp.add(p);
				}
				else{
					tmp.add(elements[j]);
				}
				j++;
			}
			
			// computes distance as we filled all empty slots
			double tmp_dist = computeDistance(tmp, points);
			if(tmp_dist < best){
				counter++;
				// read actual points from database if computed distance is shorted than k-th value
				double distance = computeCandidateDistance(entry.getKey(), points);
				if(distance < best){
					// updates result set if distance is shorter that k-th value
					resultSet.add(new Candidate(distance, entry.getKey()));
					resultSet.poll();
					best = resultSet.peek().getDistance();
					//System.out.println("Update:" + entry.getKey());
				}
			}
			
	
		}
		// format result set as an output
		while( resultSet.peek() != null) {
			output += resultSet.poll().getID()+",";
		}
		output = output.substring(0, output.length()-1);
		this.candis = candidates.size();
		System.out.println("Iteration: "+iteration+ " Candidates: "+candidates.size() + " Points:" +counter);
		return output;
	}
	 
	public int getIntersectingPoints(ArrayList<Point> s, double r, double inc, Point p){
		 // compute current and previous query range to do incremental search
		 MyVisitor v =  new MyVisitor();
		 double o = r - inc;
		 double[] f1 = new double[2];
		 double[] f2 = new double[2];
		 f1[0] = p.getCoord(0) - r; f1[1] = p.getCoord(1) - r;
		 f2[0] = p.getCoord(0) + r; f2[1] = p.getCoord(1) + r;
		 Region new_query = new Region(f1, f2);
		 Region old_query = null;
		 if(r != inc){
			 f1[0] = p.getCoord(0) - o; f1[1] = p.getCoord(1) - o;
			 f2[0] = p.getCoord(0) + o; f2[1] = p.getCoord(1) + o;
			 
			 old_query = new Region(f1, f2);
		 }
		 tree.rangeIncrementalQuery(new_query, old_query, v);
		 // read points found in current search
		 for (Map.Entry<Integer, IShape> entry : v.answers.entrySet()) {
		    IShape value = entry.getValue();
		    double[] coord = value.getCenter();
		    s.add(new Point(coord));
		 }
		 return 1;
	} 
	
	private double computeDistance(ArrayList<Point> p, Point[] points){
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

	public double computeCandidateDistance(String id, Point[] points) throws SQLException{
		double dist=0;
		long startTime = System.currentTimeMillis();
		// load trajectory points from database
		ArrayList<Point> pois = ds.loadTrajectoryPoints(conn, id);
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		iotime += elapsedTime;
		
		// matching the nearest point of the trajectory with query point
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
					if(compareDistance(points[j], matched.get(j), pois.get(i)) == 1){
						matched.set(j, pois.get(i));
					}
				}
			}
			
		}
		
		dist = computeDistance(matched, points);
		return dist;		
	}
}


