/**
 * 
 */
package com.patsage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author dprakash
 *
 */
public class MYSQLConnector {
		
	/**
	 * Constructor
	 */
	 public MYSQLConnector() {
		// TODO Auto-generated constructor stub
	 }
	 
	 private String driver = null;
	 private String conStr = null;
	 private String user = null;
	 private String password = null;
	 
	 /*
	  * Method to read properties file
	  */
	 public void setJDBCProp() {
		 Properties props = new Properties();

		 try {
			 InputStream resourceStream = 
					 Thread.currentThread().getContextClassLoader().getResourceAsStream("jdbc.properties");
			 props.load(resourceStream);
			 
			 this.driver = (String) props.getProperty("jdbc.driver");
			 this.conStr = (String) props.getProperty("jdbc.url");
			 this.user = (String) props.getProperty("jdbc.user");
			 this.password = (String) props.getProperty("jdbc.password");

		 } catch (IOException e) {
			 // TODO Auto-generated catch block
			 e.printStackTrace();
		 }
	 }

	 /* 
	  * method to get Connection object 
	  */
	 public Connection getmysqlConn() {
		Connection conn = null;
		
		//call setJDBCProp to set up jdbc variables
		setJDBCProp();
		try {
			Class.forName(this.driver).newInstance();
		    conn = DriverManager.getConnection(this.conStr, this.user, this.password);
		    // Do something with the Connection

		} catch (InstantiationException e) {
		    System.err.println("Exception: " + e.getMessage());
		    e.printStackTrace();
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			 System.err.println("Exception: " + e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			 System.err.println("Exception: " + e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch ( SQLException ex) {
		    // handle any errors
		    System.err.println("Exception: " + ex.getMessage());
		    System.err.println("SQLState: " + ((SQLException) ex).getSQLState());
		    System.err.println("VendorError: " + ((SQLException) ex).getErrorCode());
		    ex.printStackTrace();
		} 
		return conn;
	 }
	 
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Statement stmt = null;
		ResultSet rs = null;
		MYSQLConnector mysql = new MYSQLConnector();
		//mysql.setJDBCProp();
		Connection conn = mysql.getmysqlConn();
		System.out.println("Connection object is created ==> " + conn);
		try {
		    stmt = conn.createStatement();
		    rs = stmt.executeQuery("SELECT * FROM patsage.uspto_patent");


		    if (rs != null ) {
		    	while(rs.next()) {
		    		String patentNum = (String)rs.getString("patent_number");
		    		System.out.print(patentNum);
		    	}

		    }

		    // Now do something with the ResultSet ....
		}catch (SQLException ex){
		    // handle any errors
		    System.err.println("SQLException: " + ex.getMessage());
		    System.err.println("SQLState: " + ex.getSQLState());
		    System.err.println("VendorError: " + ex.getErrorCode());
		}
		finally {

		    if (rs != null) {
		        try {
		            rs.close();
		        } catch (SQLException sqlEx) { } // ignore

		        rs = null;
		    }

		    if (stmt != null) {
		        try {
		            stmt.close();
		        } catch (SQLException sqlEx) { } // ignore

		        stmt = null;
		    }
		    if (conn != null) {
		        try {
		            conn.close();
		        } catch (SQLException sqlEx) { } // ignore
		        conn = null;
		    }
		}

	}

}
