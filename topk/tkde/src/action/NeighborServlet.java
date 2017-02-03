package action;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import algorithm.ANN;
import algorithm.Settings;
import spatialindex.rtree.RTree;
import spatialindex.spatialindex.Point;
import spatialindex.spatialindex.Region;
import spatialindex.storagemanager.DiskStorageManager;
import spatialindex.storagemanager.IStorageManager;
import spatialindex.storagemanager.PropertySet;

/**
 * Servlet implementation class NeighborServlet
 */
@WebServlet("/NeighborServlet")
public class NeighborServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ANN alg = null;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public NeighborServlet() throws Exception {
        super();
        // TODO Auto-generated constructor stub
        String index_file = Settings.rtree_index_location;
		PropertySet ps1 = new PropertySet();
		ps1.setProperty("FileName", index_file + ".rtree");
		IStorageManager diskfile = new DiskStorageManager(ps1);

		PropertySet ps2 = new PropertySet();
		ps2.setProperty("IndexIdentifier", 1);
		RTree tree = new RTree(ps2, diskfile);
		
		alg = new ANN(tree);
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//doGet(request, response);
		// TODO Auto-generated method stub
		String output = "";
		String []pois = request.getParameter("locs").split(",");
		String locs = "";
		
   		int num = pois.length/2;
		double [][]arr = new double[num][2];
		int j = 0;
		for (int i = 0; i < arr.length; i++) {
			arr[i][0] = Double.parseDouble(pois[j]);
			arr[i][1] = Double.parseDouble(pois[j+1]);
			j+=2;
		}
		
		Point []points = new Point[num];
		for (int i = 0; i < points.length; i++) {
			points[i] = new Point(arr[i]);
		}
		long startTime = System.currentTimeMillis();
		Region query = calculateRegion(arr);
		output = alg.computeANN(query, points);
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		System.out.println("Query runtime: " + elapsedTime);
		response.setCharacterEncoding("UTF-8");
		response.getWriter().print(output);
	}

	private Region calculateRegion(double [][]arr){
		double min_lat = arr[0][0];
		double max_lat = arr[0][0];
		double min_lng = arr[0][1]; 
		double max_lng = arr[0][1];
		
		for (int i = 1; i < arr.length; i++) {
			if(min_lat > arr[i][0]){
				min_lat = arr[i][0];
			}
			if(max_lat < arr[i][0]){
				max_lat = arr[i][0];
			}
			if(min_lng > arr[i][1]){
				min_lng = arr[i][1];
			}
			if(max_lng < arr[i][1]){
				max_lng = arr[i][1];
			}
		}
		double []pLow = {min_lat, min_lng};
		double []pHigh = {max_lat, max_lng};
		Region tmp = new Region(pLow, pHigh);
		return tmp;
	}
}
