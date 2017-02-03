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

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import db.Dataset;
import spatialindex.spatialindex.*;
import spatialindex.rtree.*;

public class QE {
	private RTree tree; // RTree object
	private TreeMap<Double,Point> T;
	private HashMap<String,String> trips;
	private Connection conn = null; 
	private Dataset ds = null;
	public long iotime;
	
	public QE(RTree tree, String file) throws SQLException{
		this.tree = tree;
		this.trips = this.readTrips(file);
		this.ds = new Dataset("root", "");
		this.conn = ds.Connect();	
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
	
	public String computeQE(Region query, Point []points) throws SQLException{
		int k = Settings.k;
		iotime = 0;
		String output = "";
		TreeSet<String> full = new TreeSet<String>();
		double []rad = new double[points.length];
		ArrayList<PriorityQueue<Element>> IH= new ArrayList<PriorityQueue<Element>>();
		for(int i = 0; i < points.length; i++){
			IH.add(new PriorityQueue<Element>());
		}
		
		HashMap<String, ArrayList<Point>> candidates = new HashMap<String, ArrayList<Point>>();
		Queue<Element> GH = new PriorityQueue<>();
		
		double inc = Settings.tree_inc;
		for (int i = 0; i < points.length; i++) {
			rad[i] = inc;
			ArrayList<Point> S = new ArrayList<Point>();
			long startTime = System.currentTimeMillis();
			this.getIntersectingPoints(S, rad[i], inc, points[i]);
			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;
			iotime += elapsedTime;
			
			//System.out.println("Points: " + S.size());
			for (Point point : S) {
				double dist = points[i].getMinimumDistance(point);
				IH.get(i).add(new Element(dist, point, i));
			}
		}
			
		
		for (int i = 0; i < points.length; i++) {
			GH.add(IH.get(i).poll());
		}
				
		int index = 0;
		int iteration=0;
		int counter = 0;
		int full_match = 0;
		int qualifier = 0;
		double miu = 0.01;
		Queue<Candidate> expectation = new PriorityQueue<>(Collections.reverseOrder());
		while (qualifier < k && iteration == 0){
			Element e = GH.poll();
			Point point = e.getPoint();
			index = e.getIndex();
			if(IH.get(index).size() == 0){
				rad[index] += inc;
				ArrayList<Point> S = new ArrayList<Point>();
				this.getIntersectingPoints(S, rad[index], inc, points[index]);
				for (Point p : S) {
					double dist = points[index].getMinimumDistance(p);
					IH.get(index).add(new Element(dist, p, index));
				}
			}
			GH.add(IH.get(index).poll());
			String ids = trips.get(point.getCoord(0)+","+point.getCoord(1));
			String []t = null;
			if(ids.contains(",")){
				t = ids.split(",");					
				for (String id : t) {
					if(!full.contains(id)){
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
							if(check == 1){
								full_match++;
								qualifier ++;
								full.add(id);
								//System.out.println(id);
								
							}
							candidates.put(id, pois);
						}
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
				
				
			}
			else{
				if(!full.contains(ids)){
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
						int check  = 1;
						for (int i = 0; i < pois.size(); i++) {
							if(pois.get(i) == null){
								check *= 0;
							}
						}
						if(check == 1){
							full_match++;
							qualifier++;
							full.add(ids);
							//System.out.println(ids+"asd");
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
			}
			
			//System.out.println("Candidates:" + candidates.size());
			double ratio = (double)full_match / candidates.size();
			double accum_dist = 0;
			for (Element el : GH) {
			    accum_dist += el.getDistance();
			    //System.out.println("GH: " + el.getIndex()+ " " + el.getDistance());
			}
			//System.out.println("Accumulated distance: "+ accum_dist);
			
			if(ratio < miu){
				for (Map.Entry<String, ArrayList<Point>> entry : candidates.entrySet()) {
					if(!full.contains(entry.getKey())){
						double d = computeExpectation(entry.getValue(), points, GH);
						expectation.add(new Candidate(d, entry.getKey()));
					}					
				}				
			}
			
			/*
			while(expectation.peek() != null){
				System.out.println(expectation.poll().toString());
			}
			*/
			//System.out.println(ratio);
			while(ratio < miu){
				Candidate high = expectation.poll();
				if(high != null && !full.contains(high.getID())){
					ArrayList<Point> current = makeUpCandidate(high.getID(), points);
					if(computeDistance(current, points) <= accum_dist){
						full_match ++;
						full.add(high.getID());
						candidates.put(high.getID(), current);
						qualifier++;
						//System.out.println(high.getID());
						ratio = (double)full_match / candidates.size();
					}				
				}
				else{
					break;
				}
			}
		} 		
		
		PriorityQueue<Candidate> resultSet = new PriorityQueue<>(Collections.reverseOrder());
		Iterator<String> iterator = full.iterator();
		while (iterator.hasNext()){
			String id = iterator.next();
			double total_dist = computeDistance(candidates.get(id), points);
			resultSet.add(new Candidate(total_dist, id));
			candidates.remove(id);
		}
		
		double best = resultSet.peek().getDistance();
		//System.out.println("Best dist: "+ best);
		Point[] elements = new Point[points.length];
		for (Element e : GH) {
		    elements[e.getIndex()] = e.getPoint();
		    //System.out.println("GH: " + e.getIndex()+ " " + e.getDistance());
		}
		
		counter = 0;
		for (Map.Entry<String, ArrayList<Point>> entry : candidates.entrySet()) {
		    
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
			
			double tmp_dist = computeDistance(tmp, points);
			if(tmp_dist < best){
				counter++;
				double distance = computeCandidateDistance(entry.getKey(), points);
				if(distance < best){
					resultSet.add(new Candidate(distance, entry.getKey()));
					resultSet.poll();
					best = resultSet.peek().getDistance();
					//System.out.println("Update:" + entry.getKey());
				}
			}
			
	
		}
		while( resultSet.peek() != null) {
			output += resultSet.poll().getID()+",";
		}
		output = output.substring(0, output.length()-1);
		
		System.out.println("Counter: "+counter+ " Candidates: "+candidates.size() + " Result set:" +resultSet.size());
		
		return output;
	}
	 
	public int getIntersectingPoints(ArrayList<Point> s, double r, double inc, Point p){
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
		 //tree.containmentQuery(new_query, v);

		 for (Map.Entry<Integer, IShape> entry : v.answers.entrySet()) {
		    IShape value = entry.getValue();
		    double[] coord = value.getCenter();
		    s.add(new Point(coord));
		 }
		 return 1;
	} 
	
	private double computeExpectation(ArrayList<Point> p, Point[] points, Queue<Element> GH){
		Point[] elements = new Point[points.length];
		for (Element e : GH) {
		    elements[e.getIndex()] = e.getPoint();
		}
		double result = 0;
		int matched = 0;
		for (int i = 0; i < points.length; i++) {
			if(p.get(i) != null){
				result += elements[i].getMinimumDistance(points[i]);
				result -= p.get(i).getMinimumDistance(points[i]);
				matched ++;
			}
		}
		
		return result/(points.length - matched);		
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
		ArrayList<Point> pois = ds.loadTrajectoryPoints(conn, id);
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		iotime += elapsedTime;
		
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
	public ArrayList<Point> makeUpCandidate(String id, Point[] points) throws SQLException{
		long startTime = System.currentTimeMillis();
		ArrayList<Point> pois = ds.loadTrajectoryPoints(conn, id);
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		iotime += elapsedTime;
		
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
		
		return matched;		
	}
}


