package build;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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

public class BuildGridRtree {

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
		
		PropertySet ps = new PropertySet();
		Boolean b = new Boolean(true);
		ps.setProperty("Overwrite", b);
		ps.setProperty("FileName", index_file);
		Integer p = new Integer(page_size);
		ps.setProperty("PageSize", p);
		IStorageManager diskfile = new DiskStorageManager(ps);
		
		File file = new File(Settings.grid_info_location);
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		for (Map.Entry<Integer, ArrayList<Point>> entry : hash.entrySet()) {
			id = 1;
			grids.put(entry.getKey(), entry.getValue().size());
			System.out.println(entry.getKey()+ " " + entry.getValue().size());
			
			
			PropertySet ps1 = new PropertySet();

			Double f = new Double(0.7);
			ps1.setProperty("FillFactor", f);
			fanout = 3;
			if(entry.getValue().size() > 3){
				fanout = entry.getValue().size();
			}
			p = new Integer(fanout);
			ps1.setProperty("IndexCapacity", p);
			ps1.setProperty("LeafCapacity", p);
			p = new Integer(2);
			ps1.setProperty("Dimension", p);
			RTree tree = new RTree(ps1, diskfile);
			
			
			int node = 0;
			for (Point poi : entry.getValue()) {
				Region r = new Region(poi, poi);
				tree.insertData(null, r, id);
				
				id++;
			}			
			
			
			count ++;
			if ((count % 100) == 0) System.err.println(count);
			
			Integer indexID = (Integer) ps1.getProperty("IndexIdentifier");
			//Integer indexID = 1;
			bw.write(entry.getKey() +" "+ indexID +" " + entry.getValue().size());
			bw.newLine();
			//System.err.println("Index ID: " + indexID);
			
			boolean ret = tree.isIndexValid();
			if (ret == false) System.err.println("Structure is INVALID!");
			tree.flush();
			
		}
		
		
		// since we created a new RTree, the PropertySet that was used to initialize the structure
		// now contains the IndexIdentifier property, which can be used later to reuse the index.
		// (Remember that multiple indices may reside in the same storage manager at the same time
		//  and every one is accessed using its unique IndexIdentifier).
		

		// flush all pending changes to persistent storage (needed since Java might not call finalize when JVM exits).
		bw.close();
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
