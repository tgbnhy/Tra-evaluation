package algorithm;

public class Settings {
	
	 //Newyork dataset
	public static String grid_info_location = "E:\\University\\PhD\\Publication\\Dataset\\index\\grid-info.txt"; 
	public static String grid_index_location = "E:\\University\\PhD\\Publication\\Dataset\\index\\newyork\\grid";
	public static String rtree_index_location = "E:\\University\\PhD\\Publication\\Dataset\\index\\newyork\\poi";
	public static String trip_ids = "E:\\University\\PhD\\Publication\\Dataset\\files\\newyork_trips.txt";
	public static String points_location = "E:\\University\\PhD\\Publication\\Dataset\\files\\newyork_points.txt";
	/*
	public static String grid_info_location = "E:\\University\\PhD\\Publication\\Dataset\\index\\la-grid-info.txt";
	public static String grid_index_location = "E:\\University\\PhD\\Publication\\Dataset\\index\\la\\grid";
	public static String rtree_index_location = "E:\\University\\PhD\\Publication\\Dataset\\index\\la\\poi";
	public static String trip_ids = "E:\\University\\PhD\\Publication\\Dataset\\files\\la_trips.txt";
	public static String points_location = "E:\\University\\PhD\\Publication\\Dataset\\files\\la_points.txt";
	*/
	/*
	public static String grid_info_location = "E:\\University\\PhD\\Publication\\Dataset\\index\\sweden-info.txt";
	public static String grid_index_location = "E:\\University\\PhD\\Publication\\Dataset\\index\\sweden\\grid";
	public static String rtree_index_location = "E:\\University\\PhD\\Publication\\Dataset\\index\\sweden\\poi";
	public static String trip_ids = "E:\\University\\PhD\\Publication\\Dataset\\files\\sweden_trips.txt";
	public static String points_location = "E:\\University\\PhD\\Publication\\Dataset\\files\\sweden_points.txt";
	*/
	public static int dimension = 128; // grid dimension
	public static int size = 16384; // number of grid cells
			
	/*
	public static double min_lat = -42.9013617;
	public static double max_lat = -12.4065;
	public static double min_lng = 113.878163;
	public static double max_lng = 153.6361834;
	*/
	//Newyork Dataset
	public static double min_lat = 39.00009536743164; // dataset range
	public static double max_lat = 41.99896240234375; 
	public static double min_lng = -74.99986267089844;
	public static double max_lng = -72.00079345703125;
	
	/*
	 LA Dataset
	public static double min_lat = 32.355403900146484;
	public static double max_lat = 34.9921760559082;
	public static double min_lng = -119.97689819335938;
	public static double max_lng = -117.00012969970703;
	*/
	/*
	 * Gowalla
	public static double min_lat = -44.95363068;
	public static double max_lat = 69.983928515;
	public static double min_lng = -159.7767291;
	public static double max_lng = 177.462490797;
	
	*/
	/* Sweden
	public static double min_lat = 58.711479817;
	public static double max_lat = 60.4055786133;
	public static double min_lng = 16.74703717;
	public static double max_lng = 19.360138917;
	*/
	public static double range_inc = 0.01; // increment value for grid index
	public static double tree_inc = 0.02; // increment value for rtree index
	public static int k = 10; // top-k value
	public static int q = 6; // number of query points
}
