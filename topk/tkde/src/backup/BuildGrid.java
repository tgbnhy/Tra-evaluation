package backup;

import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import algorithm.Settings;
//import spatialindex.grid.MyLucene5;
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
		String index_file = Settings.grid_index_location;
		String location_file = Settings.points_location;
		HashMap<Integer, String> hash = new HashMap<Integer, String>();
		double min_lat = Settings.min_lat;
		double max_lat = Settings.max_lat;
		double min_lng = Settings.min_lng;
		double max_lng = Settings.max_lng;
		int d=Settings.dimension;
		int s=Settings.size;
		LineNumberReader location_reader = new LineNumberReader(new FileReader(location_file));
		
		double[] f1 = new double[2];
		double[] f2 = new double[2];
		double x1, y1, lat, lng;
		String line;
		String[] temp;
		int count = 0;
		ArrayList<Region> zordered_mbrs = new ArrayList<Region>();
		ArrayList<Region> mbrs = new ArrayList<Region>();
		ArrayList<Integer> order = new ArrayList<Integer>();
				
		//MyLucene5 lucene = new MyLucene5();
		//lucene.openIndex(index_file, true);
		
		int j = 0;
		int i = 0;
		double len = 1/(double)d;
		for (int k = 0; k < s; k++) {
			order.add(0);
			zordered_mbrs.add(null);
			double []pLow = {i*len, 1-(j+1)*len};
			double []pHigh = {(i+1)*len, 1-(j*len)};
			Region tmp = new Region(pLow, pHigh);
			mbrs.add(tmp);
			i++;
			if(i==d){
				i = 0;
				j++;
			}
		}
		
		z_curve(order, 0, 0, 0, s, d);
		
		for (int k = 0; k < order.size(); k++) {
			zordered_mbrs.set(order.get(k)-1, mbrs.get(k));
		}
		
		/*
		f1[1] = 0.9296875; f1[0] = 0.9140625;
		f2[1] = 0.9375; f2[0] = 0.921875;
		Region r = new Region(f1, f2);
		for (int k = 0; k < zordered_mbrs.size(); k++) {
			if(zordered_mbrs.get(k).contains(r)){
			    System.out.println("k:" + k);
			}
				
		}
		*/
		
		
		while ((line = location_reader.readLine()) != null)
		{
			temp = line.split(",");
			//id = Integer.parseInt(temp[0]);
			lat = Double.parseDouble(temp[0]);
			lng = Double.parseDouble(temp[1]);
			
			y1 = (lat - min_lat)/(max_lat - min_lat);
			x1 = (lng - min_lng)/(max_lng - min_lng);
			
			f1[0] = x1; f1[1] = y1;
			f2[0] = lat; f2[1] = lng;
			Point p = new Point(f1);
			Point p1 = new Point(f2);
			
			
			for (int k = 0; k < zordered_mbrs.size(); k++) {
				if(zordered_mbrs.get(k).contains(p)){
				    if(hash.containsKey(k)){
				    	String tmp = hash.get(k);
				    	hash.put(k, tmp+","+lat+","+lng);
				    }
				    else{
				    	hash.put(k, lat+","+lng);
				    }
					break;
				}
					
			}
			//if ((count % 100) == 0) System.err.println(count);

			count++;
		}
		
		for (Map.Entry<Integer, String> entry : hash.entrySet()) {
			//lucene.addDoc(String.valueOf(entry.getKey()), entry.getValue());
			if(entry.getKey().equals(5521)){
				System.out.println(entry.getValue());
			}
		}
		
		
		//lucene.closeIndex();
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
