package action;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeSet;
import java.util.Vector;

//import org.apache.lucene.index.DirectoryReader;
//import org.apache.lucene.index.IndexReader;
//import org.apache.lucene.store.FSDirectory;

import algorithm.ANN;
import algorithm.Element;
import algorithm.GH;
import algorithm.IKNN;
import algorithm.QE;
import algorithm.SGRA;
import algorithm.SRA;
import algorithm.Settings;
import db.Dataset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

import spatialindex.grid.Grid;
import spatialindex.rtree.RTree;
import spatialindex.spatialindex.DataVisitor;
import spatialindex.spatialindex.IShape;
import spatialindex.spatialindex.MyVisitor;
import spatialindex.spatialindex.Point;
import spatialindex.spatialindex.Region;
import spatialindex.storagemanager.DiskStorageManager;
import spatialindex.storagemanager.IStorageManager;
import spatialindex.storagemanager.PropertySet;


public class Test {

	public static void main(String[] args)throws Exception
	{
		File ressult_file = new File("/Users/marco/Documents/Document-Marcos-MacBook-Pro/Australia/RMIT/Code/Code/result.txt");
		FileWriter fw = new FileWriter(ressult_file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		
		Dataset ds = new Dataset("root", "rmit12345");
		Connection conn = ds.Connect();
		
		String index_file = Settings.rtree_index_location;
		PropertySet ps1 = new PropertySet();//ps1 is hash-map stores file info
		ps1.setProperty("FileName", index_file + ".rtree");
		IStorageManager diskfile = new DiskStorageManager(ps1);

		PropertySet ps2 = new PropertySet();
		ps2.setProperty("IndexIdentifier", 1);
		RTree tree = new RTree(ps2, diskfile);
		
		String file = Settings.trip_ids;
		IKNN alg = new IKNN(tree, file, ds, conn);
		GH alg1 = new GH(tree, file, ds, conn);
		QE alg2 = new QE(tree, file);
		SRA alg3 =  new SRA(tree, file);
		
		Grid g = new Grid(Settings.dimension, Settings.size);
		SGRA alg4 =  new SGRA(g, file);
		//String locs = request.getParameter("locs");
		
		String FilePath = "/Users/marco/Documents/Document-Marcos-MacBook-Pro/Australia/RMIT/Code/Code/Dataset/newyork/low/4-locations.txt";
		InputStreamReader read = new InputStreamReader(new FileInputStream(FilePath), "utf-8");
		BufferedReader reader = new BufferedReader(read);
		String line = reader.readLine();
		
		String locs = line;
		//String locs = "40.728328704833984,-73.99295806884766,40.72578048706055,-73.99031829833984";
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
		
		Region query = calculateRegion(arr);
		
		
		/*
		while( queue.peek() != null) {
		    val = queue.poll();
			System.out.println(val);
		}
		*/
		
		//System.out.println("Value: " + Math.exp(1) + " " + Math.exp(-1) + " " + Math.exp(-0.5));
		//alg.computeIKNN(query, points, 10);
		//
		long startTime = System.currentTimeMillis();
		String ids = alg.computeIKNN(query, points);
		long stopTime = System.currentTimeMillis();
		bw.write("IKNN query runtime: " + (stopTime - startTime) + " \nIO: " + alg.iotime);
		bw.newLine();
		bw.write("ID: " + ids);
		bw.newLine();
		System.out.println("IKNN query runtime: " + (stopTime - startTime) + " \nIO: " + alg.iotime);
		System.out.println("ID: " + ids);
		
		startTime = System.currentTimeMillis();
		ids = alg1.computeGH(query, points);
		stopTime = System.currentTimeMillis();
		bw.write("GH query runtime: " + (stopTime - startTime) + " \nIO: " + alg1.iotime);
		bw.newLine();
		bw.write("ID: " + ids);
		bw.newLine();
		System.out.println("GH query runtime: " + (stopTime - startTime) + " \nIO: " + alg1.iotime);
		System.out.println("ID: " + ids);
		
		startTime = System.currentTimeMillis();
		ids = alg2.computeQE(query, points);
		stopTime = System.currentTimeMillis();
		bw.write("QE query runtime: " + (stopTime - startTime) + " \nIO: " + alg2.iotime);
		bw.newLine();
		bw.write("ID: " + ids);
		bw.newLine();
		System.out.println("QE query runtime: " + (stopTime - startTime) + " \nIO: " + alg2.iotime);
		System.out.println("ID: " + ids);		
		
		startTime = System.currentTimeMillis();
		ids = alg3.computeSRA(query, points);
		stopTime = System.currentTimeMillis();
		bw.write("SRA query runtime: " + (stopTime - startTime) + " \nIO: " + alg3.iotime);
		bw.newLine();
		bw.write("ID: " + ids);
		bw.newLine();
		System.out.println("SRA query runtime: " + (stopTime - startTime) + " \nIO: " + alg3.iotime);
		System.out.println("ID: " + ids);
		
		
		startTime = System.currentTimeMillis();
		ids = alg4.computeSGRA(query, points);
		stopTime = System.currentTimeMillis();
		bw.write("SGRA query runtime: " + (stopTime - startTime) + " \nIO: " + alg4.iotime);
		bw.newLine();
		bw.write("ID: " + ids);
		bw.newLine();
		bw.close();
		System.out.println("SGRA query runtime: " + (stopTime - startTime) + " \nIO: " + alg4.iotime);
		System.out.println("ID: " + ids);
		
		
		
		/*
		Dataset ds = new Dataset("root", "");
		Connection conn = ds.Connect();
		
		double distance = alg.computeCandidateDistance("33965", points);
		System.out.println("Distance:" + distance);
		*/
		/*
		String ids = alg.computeANN(query, points);
		System.out.println("Result: "+ids);
		*/
		
	}
	
	public static Region calculateRegion(double [][]arr){
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
