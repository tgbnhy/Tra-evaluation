package action;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import algorithm.Settings;
import db.Dataset;
import spatialindex.spatialindex.Point;
import spatialindex.spatialindex.Region;

/**
 * Servlet implementation class PointServlet
 */
@WebServlet("/PointServlet")
public class PointServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection conn = null; 
	private Dataset ds = null;
    /**
     * @throws SQLException 
     * @see HttpServlet#HttpServlet()
     */
    public PointServlet() throws SQLException {
        //super();
    	ds = new Dataset("root", "");
		conn = ds.Connect();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());
		String output = "[";
		int q = Settings.q;
		q = 6;
		InputStreamReader read = new InputStreamReader(new FileInputStream("/Users/marco/Documents/Document-Marcos-MacBook-Pro/Australia/RMIT/Code/Code/Dataset/newyork/low/"+q+"-locations.txt"), "utf-8");
		BufferedReader reader = new BufferedReader(read);
		String line;
		
		int limit = 0;
		int counter = 0;
		while ((line = reader.readLine()) != null && limit != 1) {
			   //limit = 1;
			String []pois = line.split(",");
			if(pois.length == (q*2)){
				for (int i = 0; i < pois.length; i=i+2) {
					if(output.equals("[")){
						output += pois[i] + "," + pois[i+1];
					}
					else{
						output += "," + pois[i] + "," + pois[i+1];
					}
					
				}
				counter ++;
			}
			
		}	
		System.out.println("Counter: " + counter);
		output += "]";
		output = "[]";
		response.setCharacterEncoding("UTF-8");
		response.getWriter().print(output);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//doGet(request, response);
		String output="";
		try {
			output = ds.loadPlaces(conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		response.setCharacterEncoding("UTF-8");
		response.getWriter().print(output);
	}

}
