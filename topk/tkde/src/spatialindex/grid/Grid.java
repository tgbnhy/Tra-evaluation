package spatialindex.grid;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;

import spatialindex.spatialindex.*;

public class Grid {
	public ArrayList<Region> zordered_mbrs;
	
	public Grid(int d, int s) throws NumberFormatException, IOException{
		initializeMBR(d, s); 
	}
	
	private void initializeMBR(int d, int s) throws NumberFormatException, IOException{
		double[] f1 = new double[2];
		double[] f2 = new double[2];
		int count = 0;
		this.zordered_mbrs = new ArrayList<Region>();
		ArrayList<Region> mbrs = new ArrayList<Region>();
		ArrayList<Integer> order = new ArrayList<Integer>();
				
		int j = 0;
		int i = 0;
		double len = 1/(double)d;
		for (int k = 0; k < s; k++) {
			order.add(0);
			zordered_mbrs.add(null);
			//System.out.println(len);
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
		/*
		for (int k = 0; k < order.size(); k++) {
			
			System.out.print(String.format("%2d ", order.get(k)));
			if(count == (d-1)){
				System.out.println();
				count = 0;
			}	
			else{
				count ++;
			}
		
		}
		System.out.println();
		*/
		for (int k = 0; k < order.size(); k++) {
			zordered_mbrs.set(order.get(k)-1, mbrs.get(k));
		}
	}
	/*
	public static void z_curve(ArrayList<MBR> curve, int offset, int i, int j, int size, int dimension){
		if(size == 4){
			double len = 1/dimension;
			double []pLow = {i, j};
			double []pHigh = {i+len, j+len};
			MBR tmp = new MBR(pLow, pHigh);
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
	*/
	
	public String getIntersection(ArrayList<Region> mbrs, Region q){
		String start="";
		String end="";
		for (int i = 0; i < mbrs.size(); i++) {
			if(q.intersects(mbrs.get(i))){
				if(start.equals("")){
					start = String.valueOf(i+1);
				}
				end = String.valueOf(i+1);
			}
		}
		return start+","+end;
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
	/*
	public static void insert_data(HashMap<Integer, ArrayList<Point>> points, ArrayList<Region> mbrs, Point p, Point p1){
		for (int k = 0; k < mbrs.size(); k++) {
			if(mbrs.get(k).contains(p)){	
				ArrayList<Point> tmp =  points.get(k);
				tmp.add(p1);
				points.put(k, tmp);
				break;
			}
				
		}
	}
	*/
	public static void print_data(HashMap<Integer, ArrayList<Point>> points, ArrayList<Region> mbrs, int d){
		int count = 0;
		/*
		for (int k = 0; k < order.size(); k++) {
			
			System.out.print(String.format("%2d ", order.get(k)));
			if(count == (d-1)){
				System.out.println();
				count = 0;
			}	
			else{
				count ++;
			}
		
		}
		System.out.println();
		*/
		for (int k = 0; k < mbrs.size(); k++) {
			System.out.print(mbrs.get(k).toString());
			if(count == (d-1)){
				System.out.println();
				count = 0;
			}	
			else{
				count ++;
			}
		
		}
		
		for (int k = 0; k < points.size(); k++) {
			//System.out.println(points.get(k).size() + " ");
		}
	}
	
}
