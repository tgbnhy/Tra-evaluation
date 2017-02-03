package algorithm;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;

import db.Dataset;
import spatialindex.rtree.RTree;
import spatialindex.spatialindex.Point;
import spatialindex.spatialindex.Region;
import spatialindex.storagemanager.PropertySet;



public class QueryGenerator {
	private static final Random random = new Random();

	public static void main(String[] args)throws Exception
	{
		String query_file = "E:\\University\\PhD\\Publication\\Dataset\\query\\newyork\\low";
		Dataset ds = new Dataset("root", "");
		Connection conn = ds.Connect();
		int q = Settings.q;
		String ids = ds.loadTrajectoryIds(conn, q);
		String []arr = ids.split(",");
		System.out.println("Result count: "+arr.length);
		TreeSet<Integer> trips = randomPicker(arr.length, 1000); 
		//System.out.println(trips.size());
		
		
		File file = new File(query_file+"\\"+q+"-locations.txt");
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		
		//System.out.println("Val: " + checkPoint("40.702758", "-74.012351"));
		int c = 0;
		Iterator<Integer> iterator = trips.iterator();		
		while (iterator.hasNext()){
			int index = Integer.valueOf(iterator.next());
			//System.out.println(index + " ");
			String trip = arr[index];
			String []points = ds.loadTrajectory(conn, trip).split(",");
			TreeSet<Integer> poi = randomPointPicker(points, (points.length/2), q);
			//System.out.println("index: "+poi.toString());
			Iterator<Integer> p = poi.iterator();
			int start = 1;
			if(poi.size() == q){
				while(p.hasNext()){
					int j = p.next() * 2;
					if(start == 1){
						bw.write(points[j]+"," + points[j+1]);
						start = 0;
					}
					else{
						bw.write(","+points[j]+"," + points[j+1]);
					}
					
					
					//System.out.println(points[j]+", " + points[j+1]);
				}
				bw.newLine();
				c++;
				System.out.println("counter: " + c);
			}			
			
		}
		bw.close();
		
		System.out.println("Result: " + arr.length);
	}
	
	private static TreeSet<Integer> randomPicker(int max, int k){
		TreeSet<Integer> was = new TreeSet<Integer>();
		int i = 0;
		while( i < k ) {
	        int c = random.nextInt(max-1);
	    	if(!was.contains(c)){
	    		was.add(c);
	    		i++;
	    	}
	    }
		return was;
	}
	
	private static TreeSet<Integer> randomPointPicker(String []points, int max, int k){
		TreeSet<Integer> was = new TreeSet<Integer>();
		int i = 0;
		int iteration = 0;
		while( i < k && iteration < 2000) {
	        int c = random.nextInt(max-1);
	        iteration ++;
	    	if(!was.contains(c)){
	    		/* Sweden High
	    		if(Double.parseDouble(points[c*2]) > 59.30707049688991 && Double.parseDouble(points[c*2]) < 59.34665271369069 ){
	    			if(Double.parseDouble(points[c*2 + 1]) > 18.0331821431173 && Double.parseDouble(points[c*2 + 1]) < 18.094465254689567){
	    				was.add(c);
	    	    		i++;
	    			}
	    		}*/
	    		/* Sweden Medium
	    		if(Double.parseDouble(points[c*2]) > 59.333189426592185 && Double.parseDouble(points[c*2]) < 59.3729161419073 ){
	    			if(Double.parseDouble(points[c*2 + 1]) > 17.952775955200195 && Double.parseDouble(points[c*2 + 1]) < 18.033113479614258){
	    				was.add(c);
	    	    		i++;
	    			}
	    		}
	    		
	    		if(Double.parseDouble(points[c*2]) > 59.369942623687486 && Double.parseDouble(points[c*2]) < 59.53188090142972 ){
	    			if(Double.parseDouble(points[c*2 + 1]) > 17.806262969970703 && Double.parseDouble(points[c*2 + 1]) < 18.09946060180664){
	    				was.add(c);
	    	    		i++;
	    			}
	    		}
	    		*/
	    		if(checkPoint(points[c*2], points[c*2+1]) == 1){
	    			was.add(c);
    	    		i++;
	    		}
	    	}
	    }
		return was;
	}
	
	/*
	public static int checkPoint(String lat, String lng){
		double []arr = {
			40.66287907717804, -73.99158096523024, 40.68735498212944, -73.96926498622634,
			40.68631363730748, -74.01664351578802, 40.709479716693785, -73.99192427750677,
			40.710780938624424, -74.01664351578802, 40.756203800169494, -73.9685611682944,
			40.709479712722725, -73.96892165299505, 40.73341819870074, -73.94523238297552, 
			40.75734807486438, -73.99261092301458, 40.780229464194626, -73.94523238297552
		};
		
		double x = Double.parseDouble(lat);
		double y = Double.parseDouble(lng);
		
		for (int i = 0; i < arr.length; i+=4) {
			if(arr[i] < x && x < arr[i+2]){
				if(arr[i+1] < y && y < arr[i+3]){
					return 1;
				}
			}
		}		
		return 0;
	}
	
	// medium
	public static int checkPoint(String lat, String lng){
		double []arr = {
			40.57038853086713, -73.99293708906043, 40.66261865088195, -73.9452323934529,
			40.592812759722676, -74.0157680521952, 40.686959194812076, -73.99236497284619,
			40.6635627050274, -73.96895384736126, 40.71040846888154, -73.92206776148669,
			40.7337986762366, -73.94551509605662, 40.780668166818415, -73.89863705648168,
			40.75726355790861, -73.87529325380456, 40.7806974131603, -73.75809144868981,
			40.71041660746546, -74.061601634603, 40.75711402947819, -74.01576804695651,
			40.80414250782446, -73.94591902848333, 40.87401270860208, -73.89922713395208,
			40.827478631259375, -73.87529110856121, 40.87440210001789, -73.85177349991864
		};
		
		double x = Double.parseDouble(lat);
		double y = Double.parseDouble(lng);
		
		for (int i = 0; i < arr.length; i+=4) {
			if(arr[i] < x && x < arr[i+2]){
				if(arr[i+1] < y && y < arr[i+3]){
					return 1;
				}
			}
		}		
		return 0;
	}
	*/
	
	public static int checkPoint(String lat, String lng){
		double []arr = {
			40.567232903127575, -73.74078372493386, 40.958330117321616, -73.14202884212136,
			40.82960560698237, -74.87237552180886, 41.499526775729265, -73.42767337337136,
			39.57520958922828, -75.06463626399636, 40.46283047290115, -73.95501712337136
		};
		
		double x = Double.parseDouble(lat);
		double y = Double.parseDouble(lng);
		
		for (int i = 0; i < arr.length; i+=4) {
			if(arr[i] < x && x < arr[i+2]){
				if(arr[i+1] < y && y < arr[i+3]){
					return 1;
				}
			}
		}		
		return 0;
	}
	
}
