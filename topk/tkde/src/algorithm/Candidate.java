package algorithm;

public class Candidate implements Comparable<Candidate> {
	private double distance; // stores distance of the trajectory to query points
	private String id; // stores trajectory id
	
	public Candidate(double d, String id){
		this.distance = d;
		this.id = id;
	}
	
	public double getDistance(){
		return this.distance;
	}
	public String getID(){
		return this.id;
	}
	@Override
	public int compareTo(Candidate e) {
		// objects of Candidate class is compared based on distance value
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
		return "(" + distance + ", " + id + ")";
	}
}