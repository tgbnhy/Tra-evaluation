package algorithm;

import java.util.Map;
import java.util.TreeMap;

import spatialindex.spatialindex.*;
import spatialindex.rtree.*;

public class ANN {
	private RTree tree;
	private TreeMap<Double,Point> T;
	
	public ANN(RTree tree){
		this.tree = tree;
	}
	
	// Computing top 10 aggregate nearest neighbors 
	public String computeANN(Region query, Point []points){
		String ids = "[";
		MyVisitor v = new MyVisitor();
		TreeMap<Double, Point> topk = new TreeMap<Double, Point>();
		tree.nearestNeighborQuery(10, query, v);
		//System.out.println("Size:" +v.answers.size());
		for (Map.Entry<Integer, IShape> entry : v.answers.entrySet()) {
		    IShape value = entry.getValue();
		    topk.put(computeTotalDistance(value, points), new Point(value.getCenter()));
		    
		    if(topk.size()==11){
				topk.remove(topk.lastKey());
			}
		}
		
		for (Map.Entry<Double, Point> entry : topk.entrySet()) {
			if(ids.equals("[")){
		    	ids += entry.getValue().getCoord(0)+","+entry.getValue().getCoord(1);
		    }
		    else{
		    	ids += ","+entry.getValue().getCoord(0)+","+entry.getValue().getCoord(1);
		    }
			
			
		}
		
		return ids+="]";
	}
	// Computing aggregated distance from a point to all query points
	private double computeTotalDistance(IShape p, Point[] points){
		double dist=0;
		for (int i = 0; i < points.length; i++) {
			// adding distance to each query point from given point
			dist += p.getMinimumDistance(points[i]);
		}
		return dist;		
	}
}


