package action;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import algorithm.GH;
import algorithm.IKNN;
import algorithm.QE;
import algorithm.SGRA;
import algorithm.SRA;
import algorithm.Settings;
import db.Dataset;
import spatialindex.grid.Grid;
import spatialindex.rtree.RTree;
import spatialindex.spatialindex.*;
import spatialindex.storagemanager.DiskStorageManager;
import spatialindex.storagemanager.IStorageManager;
import spatialindex.storagemanager.PropertySet;

/**
 * Servlet implementation class PlaceServlet
 */
@WebServlet("/PlaceServlet")
public class PlaceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection conn = null; 
	private Dataset ds = null;
    private RTree tree = null;
    private HashMap<String, String> Trajectories = null;
    private SRA alg4 = null;
    private SGRA alg5 = null;
    private IKNN alg1 = null;
    private GH alg2 = null;
    private QE alg3 = null;
    
    /**
     * @throws SQLException 
     * @see HttpServlet#HttpServlet()
     */
    public PlaceServlet() throws Exception {
        //super();
        // TODO Auto-generated constructor stub
    	ds = new Dataset("root", "");
		conn = ds.Connect();
		
		String index_file = Settings.rtree_index_location;
		String file = Settings.trip_ids;
		PropertySet ps = new PropertySet();
		ps.setProperty("FileName", index_file + ".rtree");
		IStorageManager diskfile = new DiskStorageManager(ps);

		PropertySet ps2 = new PropertySet();
		ps2.setProperty("IndexIdentifier", 1);
		tree = new RTree(ps2, diskfile);
		alg4 = new SRA(tree, file);
		
		alg1 = new IKNN(tree, file, ds, conn);
		alg2 = new GH(tree, file, ds, conn);
		//alg5 = new QE(tree, file);
		
		Grid g = new Grid(Settings.dimension, Settings.size);
		alg5 = new SGRA(g, file);
		
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());
		
		
		String id = request.getParameter("trip");
				
		String output = "";
		try {
			output = ds.loadTrajectory(conn, id);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//alg2.getIntersectingPoints(s, r, p, start, end, buffer, scanned_cells, lucene);
		
		response.setCharacterEncoding("UTF-8");
		response.getWriter().print(output);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String output = "";
		String ids = "";
		//int algorithm =  Integer.parseInt(request.getParameter("algorithm"));
		//String []pois = request.getParameter("locs").split(",");
		long sra_avg_iotime = 0;
		long sra_avg_querytime = 0;
		long sra_avg_ann = 0;
		long sra_avg_candis = 0;
		
		long sgra_avg_iotime = 0;
		long sgra_avg_querytime = 0;
		long sgra_avg_ann = 0;
		long sgra_avg_candis = 0;
		
		long iknn_avg_iotime = 0;
		long iknn_avg_querytime = 0;
		long iknn_avg_candis = 0;
		
		long gh_avg_iotime = 0;
		long gh_avg_querytime = 0;
		long gh_avg_candis = 0;
		
		long qe_avg_iotime = 0;
		long qe_avg_querytime = 0;
		long qe_avg_candis = 0;
		
		int count = 0;
		int q = 6;
		String locs = "";
		try {
		   InputStreamReader read = new InputStreamReader(new FileInputStream("E:\\University\\PhD\\Publication\\Dataset\\query\\newyork\\low\\"+q+"-locations.txt"), "utf-8");
		   BufferedReader reader = new BufferedReader(read);
		   String line;
		   int limit = 0;
		   while ((line = reader.readLine()) != null && limit != 1) {
			   //limit = 1;
		   	String []pois = line.split(",");
		   	if(pois.length == (q*2)){
		   		count ++;
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
				Region query = calculateRegion(arr);
				
				long startTime = System.currentTimeMillis();
				ids = alg1.computeIKNN(query, points);
				long stopTime = System.currentTimeMillis();
				iknn_avg_iotime += alg1.iotime;
				iknn_avg_querytime += (stopTime - startTime);
				iknn_avg_candis += alg1.candis;
				
				startTime = System.currentTimeMillis();
				ids = alg2.computeGH(query, points);
				stopTime = System.currentTimeMillis();
				gh_avg_iotime += alg2.iotime;
				gh_avg_querytime += (stopTime - startTime);
				gh_avg_candis += alg2.candis;
				
				startTime = System.currentTimeMillis();
				ids = alg4.computeSRA(query, points);
				stopTime = System.currentTimeMillis();
				sra_avg_ann += alg4.ann;
				sra_avg_iotime += alg4.iotime;
				sra_avg_querytime += (stopTime - startTime);
				sra_avg_candis += alg4.candis;
				
				startTime = System.currentTimeMillis();
				ids = alg5.computeSGRA(query, points);
				stopTime = System.currentTimeMillis();
				sgra_avg_ann += alg5.ann;
				sgra_avg_iotime += alg5.iotime;
				sgra_avg_querytime += (stopTime - startTime);
				sgra_avg_candis += alg5.candis;
				/*
				startTime = System.currentTimeMillis();
				ids = alg5.computeQE(query, points);
				stopTime = System.currentTimeMillis();
				qe_avg_iotime += alg5.iotime;
				qe_avg_querytime += (stopTime - startTime);
				System.out.println("QE query runtime: " + (stopTime - startTime));
				*/
				
		   	}
			
//			if(alg1.iotime < alg2.iotime){
//				System.out.println("IO Performance: "+alg1.iotime + " " + alg2.iotime);
//			}
			   
		   } reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println();
		System.out.println("Query: IKNN - " + (iknn_avg_querytime/count) + " GH - " + (gh_avg_querytime/count) + " SRA - " + (sra_avg_querytime/count) + " SGRA - " + (sgra_avg_querytime/count));
		System.out.println("IO: IKNN - " + (iknn_avg_iotime/count) + " GH - " + (gh_avg_iotime/count)  + " SRA - " + (sra_avg_iotime/count) + " SGRA - " + (sgra_avg_iotime/count));
		System.out.println("Candidates: IKNN - " + (iknn_avg_candis/count) + " GH - " + (gh_avg_candis/count)  + " SRA - " + (sra_avg_candis/count) + " SGRA - " + (sgra_avg_candis/count));
		System.out.println("Counter: " + count);
		for (String id : ids.split(",")) {
			output += "<img src=\"images/4.jpg\" trip=\""+id+"\" class=\"img-thumbnail\" width=\"100%\">";
		}		
		
		response.setCharacterEncoding("UTF-8");
		response.getWriter().print(output);
	}
	
	/*
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String output = "";
		
		//int algorithm =  Integer.parseInt(request.getParameter("algorithm"));
		//String []pois = request.getParameter("locs").split(",");
		String locs = "";
		if(request.getParameter("locs").equals("")){
			try {
			   InputStreamReader read = new InputStreamReader(new FileInputStream("E:\\University\\PhD\\Publication\\Dataset\\query\\4-locations.txt"), "utf-8");
			   BufferedReader reader = new BufferedReader(read);
			   String line;
			   while ((line = reader.readLine()) != null) {
				   locs += line+",";
			   } reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else{
			locs = request.getParameter("locs");
		}
		
		String []pois = locs.split(",");
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
		
//		for (int i = 0; i < points.length; i++) {
//			System.out.println(arr[i][0]+" "+ arr[i][1]);
//			System.out.println(points[i].toString());
//		}
		Region query = calculateRegion(arr);
		
		
		
		long startTime = System.currentTimeMillis();
		String text = "SRA";
		String ids = "";
		ids = alg1.computeSRA(query, points);
		long pauseTime = System.currentTimeMillis();	
		text = "SGRA";
		ids = alg2.computeSGRA(query, points);
		
		//System.out.println(ids);
		long stopTime = System.currentTimeMillis();
		for (String id : ids.split(",")) {
			output += "<img src=\"images/4.jpg\" trip=\""+id+"\" class=\"img-thumbnail\" width=\"100%\">";
		}
		
		
		long elapsedTime = pauseTime - startTime;
		System.out.println("SRA query runtime: " + elapsedTime);
		elapsedTime = stopTime - pauseTime;
		System.out.println("SGRA query runtime: " + elapsedTime);
		response.setCharacterEncoding("UTF-8");
		response.getWriter().print(output);
	}
	*/
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
	
	/*
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String output = "[";
		try {
		   InputStreamReader read = new InputStreamReader(new FileInputStream("C:\\Users\\Home\\workspace\\tkde\\WebContent\\images\\points.txt"), "utf-8");
		   BufferedReader reader = new BufferedReader(read);
		   String line;
		   int c = 0;
		   while ((line = reader.readLine()) != null) {
		    output += "["+line+"],";
		    c++;
		   } 
		   reader.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		output = output.substring(0, output.length()-1)+ "]";
		response.setCharacterEncoding("UTF-8");
		response.getWriter().print(output);
	}
	*/
	
	

}
