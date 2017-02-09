package db;

import java.sql.SQLException;
import java.util.ArrayList;

import spatialindex.spatialindex.Point;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Dataset {
	// JDBC driver name and database URL
    private static String JDBC_DRIVER = "com.mysql.jdbc.Driver"; 
    private static String DB_URL = "jdbc:mysql://localhost/triphobo";

    //  Database credentials
    private String USER;
    private String PASS;
   
    /* Creating local database connection object */
    public Dataset(String username, String password)
    {
        this.USER = username;
        this.PASS = password;        
    }
   
    public Connection Connect() throws SQLException{

        try{
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException exception){
            System.out.println("Database Driver Class Not found Exception: " + exception.toString());
            return null;
        }

        // Set connection timeout. Make sure you set this correctly as per your need
        DriverManager.setLoginTimeout(5);
        System.out.println("JDBC Driver Successfully Registered ...");

        try{
            System.out.println("Connecting Database ...");
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            return conn;
        } catch (SQLException e){
            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
            return null;
        }
    }
    
    public String loadTrajectory(Connection conn, String ids) throws SQLException{
        
    	String result = "";
    	String sql = "SELECT distinct latitude, longitude, trip_id, id from tb_newyork_dataset where trip_id in ("+ids+") order by id";
        //System.out.println(sql);
        PreparedStatement st = conn.prepareStatement(sql);
        String current = "";
    	try{
    		ResultSet rs = st.executeQuery();
    		int c= 0;
    		while(rs.next()){
            	if(current.equals(rs.getString("trip_id"))){
            		result += rs.getString("latitude")+","+rs.getString("longitude")+",";
            	}
            	else{
            		if(!current.equals("")){
            			result = result.substring(0, result.length()-1);
            			result +="#";
            		}
            		result += rs.getString("latitude")+","+rs.getString("longitude")+",";
            		current = rs.getString("trip_id");
            	}
            	c++;
            }
    		//System.out.println(c);
        }
        catch(SQLException e){
            e.printStackTrace();
            return null;
        }
        st.close();
        return result.substring(0, result.length()-1);
        
    }
    
    public ArrayList<Point> loadTrajectoryPoints(Connection conn, String id) throws SQLException{
        
    	String result = "";
    	String sql = "SELECT distinct latitude, longitude from tb_newyork_dataset where trip_id = "+id;
        //System.out.println(sql);
        PreparedStatement st = conn.prepareStatement(sql);
        ArrayList<Point> pois = new ArrayList<>();
    	try{
    		ResultSet rs = st.executeQuery();
    		int c= 0;
    		while(rs.next()){
    			double[] f1 = new double[2];
    			f1[0] = Double.parseDouble(rs.getString("latitude")); f1[1] = Double.parseDouble(rs.getString("longitude"));
            	pois.add(new Point(f1));    			
            }
    		//System.out.println(c);
        }
        catch(SQLException e){
            e.printStackTrace();
            return null;
        }
        st.close();
        return pois;
        
    }
    
    public String loadPlaces(Connection conn) throws SQLException{
        
        String sql = "SELECT DISTINCT longitude, latitude from tb_newyork_dataset limit 10";
        String result = "[";
        PreparedStatement st = conn.prepareStatement(sql);
        int c = 0;
        try{
            ResultSet rs = st.executeQuery();
            while(rs.next()){
                if(c==0){
                	result += "["+rs.getString("latitude")+","+rs.getString("longitude")+"]";
                }
                else{
                	result += ",["+rs.getString("latitude")+","+rs.getString("longitude")+"]";
                }
                c++;
            }
            st.close();
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return result+"]";
    }
    
    public String loadTrajectoryIds(Connection conn, int k) throws SQLException{
        
        //String sql = " select trip_id from tb_sweden_dataset group by trip_id having count(trip_id) > "+k;
        //String sql = "select distinct trip_id from tb_sweden_dataset where trip_id in (select trip_id from tb_sweden_dataset group by trip_id having count(trip_id) > "+k+") and latitude between 59.30707049688991 and 59.34665271369069 and longitude between 18.0331821431173 and 18.094465254689567";
    	//String sql = "select distinct trip_id from tb_sweden_dataset where trip_id in (select trip_id from tb_sweden_dataset group by trip_id having count(trip_id) > "+k+") and latitude between  59.333189426592185 and 59.3729161419073 and longitude between 17.952775955200195 and 18.033113479614258";
    	//String sql = "select distinct trip_id from tb_sweden_dataset where trip_id in (select trip_id from tb_sweden_dataset group by trip_id having count(trip_id) > "+k+") and latitude between  59.369942623687486  and 59.53188090142972 and longitude between 17.806262969970703 and 18.09946060180664 ";
    	
    	//String sql = "select distinct trip_id from tb_newyork_dataset where trip_id in (select trip_id from tb_newyork_dataset group by trip_id having count(trip_id) > "+k+") and latitude between 40.66584791674222 and 40.77825366139996 and longitude between -74.01228333357722 and -73.94499207381159";
    	//String sql = "select distinct trip_id from tb_newyork_dataset where trip_id in (select trip_id from tb_newyork_dataset group by trip_id having count(trip_id) > "+k+") and latitude between 40.56691991987486 and 40.91081428019627 and longitude between -74.08362577669322 and -73.75334928743541";
    	String sql = "select distinct trip_id from tb_newyork_dataset group by trip_id having count(trip_id) > "+k;
    	
    	String result = "";
        PreparedStatement st = conn.prepareStatement(sql);
        int c = 0;
        try{
            ResultSet rs = st.executeQuery();
            while(rs.next()){
                result += rs.getString("trip_id")+",";
                c++;
            }
            st.close();
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return result.substring(0, result.length()-1);
    }
    
    /* Closing database connection */
    public void DisConnect(Connection conn) throws SQLException{
        try{
            if(conn!=null){
                conn.close();
                System.out.println("Closing connection ...");
            }                            
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }
}
