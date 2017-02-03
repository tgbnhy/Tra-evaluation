package backup;

import java.io.FileReader;
import java.io.LineNumberReader;

import algorithm.Settings;
import spatialindex.rtree.RTree;
import spatialindex.spatialindex.ISpatialIndex;
import spatialindex.spatialindex.Region;
import spatialindex.storagemanager.DiskStorageManager;
import spatialindex.storagemanager.IBuffer;
import spatialindex.storagemanager.IStorageManager;
import spatialindex.storagemanager.PropertySet;
import spatialindex.storagemanager.RandomEvictionsBuffer;

public class BuildGridRtreeBackup {

	public static void main(String[] args)throws Exception
	{
		//location_file format:
		//one object per line,
		//each line: id,x,y
		//           integer,double,double
		
		String index_file = Settings.rtree_index_location;
		int page_size = 4096;
		int fanout = Settings.size;
		String location_file = Settings.points_location;
		
		LineNumberReader location_reader = new LineNumberReader(new FileReader(location_file));
		
		// Create a disk based storage manager.
		PropertySet ps = new PropertySet();

		Boolean b = new Boolean(true);
		ps.setProperty("Overwrite", b);
			//overwrite the file if it exists.

		ps.setProperty("FileName", index_file + ".test");
			// .idx and .dat extensions will be added.

		Integer i = new Integer(page_size);
		ps.setProperty("PageSize", i);
			// specify the page size. Since the index may also contain user defined data
			// there is no way to know how big a single node may become. The storage manager
			// will use multiple pages per node if needed. Off course this will slow down performance.

		IStorageManager diskfile = new DiskStorageManager(ps);

		// Create a new, empty, RTree with dimensionality 2, minimum load 70%
		PropertySet ps2 = new PropertySet();

		Double f = new Double(0.7);
		ps2.setProperty("FillFactor", f);

		i = new Integer(fanout);
		ps2.setProperty("IndexCapacity", i);
		ps2.setProperty("LeafCapacity", i);
			// Index capacity and leaf capacity may be different.

		i = new Integer(2);
		ps2.setProperty("Dimension", i);

		RTree tree = new RTree(ps2, diskfile);
		
		int count = 0;
		int id = 1;
		
		double min_lat = Settings.min_lat;
		double max_lat = Settings.max_lat;
		double min_lng = Settings.min_lng;
		double max_lng = Settings.max_lng;
		
		double x1, x2, y1, y2;
		double[] f1 = new double[2];
		double[] f2 = new double[2];
		String line;
		String[] temp;
		
		double inc = 1/(double)Settings.dimension;
		long start = System.currentTimeMillis();
		for(int k=0; k< Settings.dimension; k++){
			for (int j = 0; j < Settings.dimension; j++) {
				x1 = j/(double)Settings.dimension;
				y1 = k/(double)Settings.dimension;
				
				f1[0] = x1; f1[1] = y1;
				f2[0] = x1 + inc; f2[1] = y1 + inc;
				Region r = new Region(f1, f2);
				//System.out.println(r.toString());
				tree.insertData(null, r, id);
				id++;
			}
			System.out.println(k);
		}
		
		/*
		while ((line = location_reader.readLine()) != null)
		{
			temp = line.split(",");
			
			y1 = (Double.parseDouble(temp[0]) - min_lat)/(max_lat - min_lat);
			x1 = (Double.parseDouble(temp[1]) - min_lng)/(max_lng - min_lng);
			
			f1[0] = x1; f1[1] = y1;
			f2[0] = x1; f2[1] = y1;
			Region r = new Region(f1, f2);
			
			tree.insertData(null, r, id);
			id++;
			if ((count % 1000) == 0) System.err.println(count);

			count++;
		}
		*/
		long end = System.currentTimeMillis();
		System.err.println("Operations: " + count);
		System.err.println(tree);
		System.err.println("Minutes: " + ((end - start) / 1000.0f) / 60.0f);

		// since we created a new RTree, the PropertySet that was used to initialize the structure
		// now contains the IndexIdentifier property, which can be used later to reuse the index.
		// (Remember that multiple indices may reside in the same storage manager at the same time
		//  and every one is accessed using its unique IndexIdentifier).
		Integer indexID = (Integer) ps2.getProperty("IndexIdentifier");
		System.err.println("Index ID: " + indexID);

		boolean ret = tree.isIndexValid();
		if (ret == false) System.err.println("Structure is INVALID!");

		// flush all pending changes to persistent storage (needed since Java might not call finalize when JVM exits).
		tree.flush();
		location_reader.close();
	}
}
