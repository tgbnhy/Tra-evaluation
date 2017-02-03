package algorithm;

import spatialindex.spatialindex.Point;

public class Element implements Comparable<Element> {
	private double distance; // stores distance from element to all query points
	private Point poi; // stores latitude and longitude information
	private int index; // stores the index for array of query points
	
	public Element(double d, Point p, int i){
		this.distance = d;
		this.poi = p;
		this.index = i;
	}
	
	public double getDistance(){
		return this.distance;
	}
	public Point getPoint(){
		return this.poi;
	}
	public int getIndex(){
		return this.index;
	}
	@Override
	public int compareTo(Element e) {
		
		// compares elements based on distance value
	    double d = e.getDistance();
	    
	    if (this.getDistance() <= d) {
	      return -1;
	    }
	
	    if (this.getDistance() > d) {
	      return 1;
	    }
	    
	    // Should not reach here 
	    return 0;
	}
	@Override
	public String toString() {
	    return "(" + distance + ", " + index + ", " + poi.toString() + ")";
	}
}