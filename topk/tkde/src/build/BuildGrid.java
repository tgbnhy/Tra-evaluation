package build;

import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import algorithm.Settings;
import spatialindex.rtree.RTree;
import spatialindex.spatialindex.ISpatialIndex;
import spatialindex.spatialindex.Point;
import spatialindex.spatialindex.Region;
import spatialindex.storagemanager.DiskStorageManager;
import spatialindex.storagemanager.IBuffer;
import spatialindex.storagemanager.IStorageManager;
import spatialindex.storagemanager.PropertySet;
import spatialindex.storagemanager.RandomEvictionsBuffer;

public class BuildGrid {

	public static void main(String[] args)throws Exception
	{
		//location_file format:
		//one object per line,
		//each line: id,x,y
		//           integer,double,double
		
		String index_file = Settings.grid_index_location;
		int page_size = 4096;
		int fanout = Settings.size;
		String location_file = Settings.points_location;
		
		LineNumberReader location_reader = new LineNumberReader(new FileReader(location_file));
						
		int count = 0;
		int id = 1;
		HashMap<Integer, ArrayList<Point>> hash = new HashMap<Integer, ArrayList<Point>>();
		HashMap<Integer, Integer> grids = new HashMap<Integer, Integer>();
		double min_lat = Settings.min_lat;
		double max_lat = Settings.max_lat;
		double min_lng = Settings.min_lng;
		double max_lng = Settings.max_lng;
		
		double x1, x2, y1, y2, lat, lng;
		double[] f1 = new double[2];
		double[] f2 = new double[2];
		String line;
		String[] temp;
		
		ArrayList<Region> zordered_mbrs = new ArrayList<Region>();
		ArrayList<Region> mbrs = new ArrayList<Region>();
		ArrayList<Integer> order = new ArrayList<Integer>();
		
		int j = 0;
		int i = 0;
		double len = 1/(double)Settings.dimension;
		for (int k = 0; k < Settings.size; k++) {
			order.add(0);
			zordered_mbrs.add(null);
			double []pLow = {i*len, 1-(j+1)*len};
			double []pHigh = {(i+1)*len, 1-(j*len)};
			Region tmp = new Region(pLow, pHigh);
			mbrs.add(tmp);
			i++;
			if(i==Settings.dimension){
				i = 0;
				j++;
			}
		}
		
		z_curve(order, 0, 0, 0, Settings.size, Settings.dimension);
		
		for (int k = 0; k < order.size(); k++) {
			zordered_mbrs.set(order.get(k)-1, mbrs.get(k));
		}
		
		System.out.println(zordered_mbrs.get(4239).toString());
		Region t = zordered_mbrs.get(4239);
		x1 = t.getLow(1)*(max_lat-min_lat)+min_lat;
		x2 = t.getHigh(1)*(max_lat-min_lat)+min_lat;
		y1 = t.getLow(0)*(max_lng-min_lng)+min_lng;
		y2 = t.getHigh(0)*(max_lng-min_lng)+min_lng;
		
		System.out.println(x1 + " " + y1 + " " + x2 + " " + y2 + " ");
		/*
		int c = 0;
		while ((line = location_reader.readLine()) != null)
		{
			temp = line.split(",");
			lat = Double.parseDouble(temp[0]);
			lng = Double.parseDouble(temp[1]);
			
			y1 = (lat - min_lat)/(max_lat - min_lat);
			x1 = (lng - min_lng)/(max_lng - min_lng);
			
			f1[0] = x1; f1[1] = y1;
			f2[0] = lat; f2[1] = lng;
			Point p1 = new Point(f1);
			Point p2 = new Point(f2);
			
			for (int k = 0; k < zordered_mbrs.size(); k++) {
				if(zordered_mbrs.get(k).contains(p1)){
				    if(hash.containsKey(k)){
				    	ArrayList<Point> tmp = hash.get(k);
				    	tmp.add(p2);
				    	hash.put(k, tmp);
				    }
				    else{
				    	ArrayList<Point> tmp = new ArrayList<Point>();
				    	tmp.add(p2);
				    	hash.put(k, tmp);
				    }
					break;
				}
					
			}
			c++;
		}
		System.out.println("Counter: "+c + " Hash: "+ hash.size());
		*/
		location_reader.close();
	}
	
	public static void z_curve(ArrayList<Integer> curve, int offset, int i, int j, int size, int dimension){
		if(size == 4){
			curve.set(i*dimension+j, offset+1);
			curve.set(i*dimension+(j+1), offset+2);
			curve.set((i+1)*dimension+j , offset+3);
			curve.set((i+1)*dimension+(j+1), offset+4);
		}
		else{
			int inc = (int)Math.sqrt(size/4);
			z_curve(curve, offset, i, j, size/4, dimension);
			z_curve(curve, offset + size/4, i, j+inc, size/4, dimension);
			z_curve(curve, offset + size/2, i+inc, j, size/4, dimension);
			z_curve(curve, offset + size*3/4, i+inc, j+inc, size/4, dimension);
		}
		
	}
}
