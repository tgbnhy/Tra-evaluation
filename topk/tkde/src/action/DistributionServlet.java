package action;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import algorithm.Settings;
import spatialindex.spatialindex.Point;
import spatialindex.spatialindex.Region;

/**
 * Servlet implementation class DistributionServlet
 */
@WebServlet("/DistributionServlet")
public class DistributionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DistributionServlet() {
        super();
        // TODO Auto-generated constructor stub
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
		String info_file = Settings.grid_info_location;
		
		LineNumberReader location_reader = new LineNumberReader(new FileReader(info_file));
		
		String output = "[";
		
		int count = 0;
		double min_lat = Settings.min_lat;
		double max_lat = Settings.max_lat;
		double min_lng = Settings.min_lng;
		double max_lng = Settings.max_lng;
		
		double x1, x2, y1, y2, lat, lng;
		double[] f1 = new double[2];
		double[] f2 = new double[2];
		String line;
		String[] temp;
		int size, key;
		
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
		
		
		//System.out.println(x1 + " " + y1 + " " + x2 + " " + y2 + " ");
		int c = 0;
		String color;
		
		while ((line = location_reader.readLine()) != null)
		{
			int check = 0;
			color = "#00ff00";
			temp = line.split(" ");
			key = Integer.parseInt(temp[0]);
			size = Integer.parseInt(temp[2]);
			
			if(size > 10 && size <= 100){
				color = "#FFFF00";
				
			}
			else if(size > 100 && size <= 1000){
				color = "#FFA500";
				
			}
			else if(size > 1000){
				check = 1;
				color = "#FF0000";
			}
			
			Region t = zordered_mbrs.get(key);
			x1 = t.getLow(1)*(max_lat-min_lat)+min_lat;
			x2 = t.getHigh(1)*(max_lat-min_lat)+min_lat;
			y1 = t.getLow(0)*(max_lng-min_lng)+min_lng;
			y2 = t.getHigh(0)*(max_lng-min_lng)+min_lng;
			
			if(check == 1){
				if(output.equals("[")){
					output += x1 + "," + y1 + ","+ x2 + "," + y2 + "," + "\"" + color + "\"";
				}
				else{
					output += "," + x1 + "," + y1 + ","+ x2 + "," + y2 + ","  + "\"" + color + "\"";
				}				
			}
			
			c++;
		}
		System.out.println("Row:" + c); 
		location_reader.close();
		output +="]";
		response.setCharacterEncoding("UTF-8");
		response.getWriter().print(output);
	}
	
	public void z_curve(ArrayList<Integer> curve, int offset, int i, int j, int size, int dimension){
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
