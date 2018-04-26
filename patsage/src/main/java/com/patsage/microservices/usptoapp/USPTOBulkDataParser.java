/**
 * 
 */
package com.patsage.microservices.usptoapp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.patsage.MYSQLConnector;

/**
 * @author dprakash
 *
 */
public class USPTOBulkDataParser {
	
	// Define the logger object for this class
		private final Logger logger = LoggerFactory.getLogger(this.getClass());
		private String usptoBulkdataPath;
		private String usptoPatentHTMLUrl;
		private String usptoSearchPageUrl;

	/*
	 * Method to read properties file
	 */
	public void setPatentProp() {
		
		try {
			Properties props = new Properties();
			InputStream resourceStream = 
				 Thread.currentThread().getContextClassLoader().getResourceAsStream("pat-config.properties");
			props.load(resourceStream);
			this.usptoBulkdataPath = (String) props.getProperty("uspto.bulkdata.path");
			this.usptoPatentHTMLUrl = (String) props.getProperty("uspto.pat.url");
			this.usptoSearchPageUrl = (String) props.getProperty("uspto.search.url");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//Constructor
	public USPTOBulkDataParser() {
		setPatentProp();
	}
	
	/*
	 * Read patent html files from local folder
	 * 
	 */
	
	public int readPatentFiles (String path) {
    	//get the file counts, for every file, call the PDFTextExtractor
    	File[] files = new File(path).listFiles();
    	System.out.println("# of files found = " + files.length);
    	if(files.length == 0) {
    		System.out.println("No files found at ..: " + path);
    		return 1;
    	}
    	
    	for (File file : files) {
            if (!file.isDirectory()) {
            	String fileName = file.getName();
            	String baseName = FilenameUtils.getBaseName(fileName);
            	String extension = FilenameUtils.getExtension(fileName);
            	
            	System.out.println("File  base name & extension is : " + baseName + " " + extension);
            	if(extension.equals("html")) {
            		int result = parseUSPTOPatent(file);
	            	if(result == 1) {
	            		System.err.println("File parser failed to extract text! ...");
	            	} else {
	            		System.out.println(" USPTO Patent extraction is successful !");
	            	}
            }
           } else {
                System.out.println("This is not a file but a directory ....: " + file.getName());
                continue;
            }
        }
    	return 0;
	}
	
	/*
	 * method to parse USPTO Patent html file
	 * 
	 */
	public int parseUSPTOPatent(File file) {
		System.out.println("<=============== Inside parseUSPTOPatent()===============>");
		StringBuffer claims = new StringBuffer();
		Document doc = null;
		
		// need http protocol
		//doc = Jsoup.connect("http://circ.ahajournals.org/content/131/4/e29.full.pdf+html").userAgent("Mozilla").ignoreHttpErrors(true).timeout(0).get();
		try {
			//doc = Jsoup.connect(url).userAgent("Mozilla").ignoreHttpErrors(true).timeout(5000).get();
			doc = Jsoup.parse(file, null);
			if(doc == null) {
				System.err.println("Document is null ...exiting...");
				return 1;
			} else {
				System.out.println("Document is valid ...proceeding with parsing the page...");
			}
			// get page title
			String pageTitle = doc.title();
			System.out.println("page title : " + pageTitle);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Elements items = doc.select("coma");
		Element coma=  null;
		String htmlText = null;
		if(items != null) {
			System.out.println("found table row =>" + items.size());
			if(items.size() != 0) {
				coma = items.first();
				htmlText = coma.html();
			}else {
				return 1;
			}
		}else {
			return 1;
		}
		if(htmlText == null) {
			return 1;
		}
		boolean flag = false ;
		String[] textSplitResult = htmlText.split("<br>");
		for (String temp: textSplitResult) {
			//System.out.println("Text=>"+ temp + "\n");
			if(temp != null) {
				if(temp.trim().startsWith("1.")) {
					flag = true;
					claims.append(temp);
				}else if(temp.trim().startsWith("2.")) {
					flag = false;
					break;
				}				
				if(flag) {
					claims.append(temp.trim());
				}
			}
		}
		System.out.println(" top claims  =>" + claims.toString());
		return 0;
	}
	
	/*
	 * Populate Claims for all Patents
	 */
	public int populateClaims() {
		System.out.println("<==============inside populateClaims()============>: ");
		//instantiate MySQL connection
    	MYSQLConnector mysql = new MYSQLConnector();
    	Connection conn = mysql.getmysqlConn();
    	Statement stmt = null;
    	PreparedStatement preparedStmt = null;
    	ResultSet rs = null;
    	
    	try {
    		String patsql = "Select patentnum from  patsage.temp_patentclaims "
    							+ "where claims is null or claims = '' LIMIT 10;";

	   		String claimsql = "update patsage.temp_patentclaims " +
	   								"set claims = ? where patentnum = ?"; 
	   		stmt = conn.createStatement();
	   		rs = stmt.executeQuery(patsql);
	   		while (rs.next()) {
	   			String pat = rs.getString("patentnum");
	   			String usptoURL = usptoPatentHTMLUrl + pat + ".PN.&OS=PN/"+ pat + "&RS=PN/" + pat;
	   			System.out.println("uspto url==>: " + usptoURL);
	   			String result = getUSPTOAbstract(usptoURL);
	   			System.out.println("claims ==>: " + result);

	   			preparedStmt = conn.prepareStatement(claimsql);
	   			preparedStmt.setString (1, result);
	   			preparedStmt.setString (2, pat);
	   			preparedStmt.execute();
	   		}
	   		
    	}catch (SQLException  ex) {
			ex.printStackTrace();
    	    // handle any errors
    	    System.err.println("SQLException: " + ex.getMessage());
    	    System.err.println("SQLState: " + ((SQLException) ex).getSQLState());
    	    System.err.println("VendorError: " + ((SQLException) ex).getErrorCode());
    	    return 0;
		} finally {
    		if (stmt != null) {
    			try {
    				stmt.close();
    			} catch (SQLException sqlEx) { } // ignore
    			stmt = null;
    		}
    		if (preparedStmt != null) {
    			try {
    				preparedStmt.close();
    			} catch (SQLException sqlEx) { } // ignore
    				preparedStmt = null;
    		}
    		if (rs != null) {
    			try {
    				rs.close();
    		    } catch (SQLException sqlEx) { } // ignore
    		        rs = null;
    		}
    		if (conn != null) {
    			try {
    				conn.close();
    		    } catch (SQLException sqlEx) { } // ignore
    		        conn = null;
    		}
    	}
		
		return 1;
	}
	
	/*
	 * Populate Abstract for all Patents
	 */
	public int populateAbstract() {
		System.out.println("<==============inside populateAbstract()============>: ");
		//instantiate MySQL connection
    	MYSQLConnector mysql = new MYSQLConnector();
    	Connection conn = mysql.getmysqlConn();
    	Statement stmt = null;
    	PreparedStatement preparedStmt = null;
    	ResultSet rs = null;
    	
    	try {
    		String patsql = "Select patentnum, url from  patsage.temp_patentclaims";

	   		String abssql = "update patsage.temp_patentclaims " +
	   								"set abstract = ? where patentnum = ?"; 
	   		stmt = conn.createStatement();
	   		rs = stmt.executeQuery(patsql);
	   		while (rs.next()) {
	   			String pat = rs.getString("patentnum");
	   			String usptoURL = rs.getString("url");
	   			System.out.println("uspto url==>: " + usptoURL);
	   			//String result = getUSPTOClaims(usptoURL);
	   			//String result = getUSPTOAbstract(usptoURL);
	   			String result = getGPAbstract(usptoURL.trim());
	   			System.out.println("abstract ==>: " + result);
	   			if(result == null) {
	   				result = "NA";
	   			}
	   			preparedStmt = conn.prepareStatement(abssql);
	   			preparedStmt.setString (1, result);
	   			preparedStmt.setString (2, pat);
	   			preparedStmt.execute();
	   		}
	   		
    	}catch (SQLException  ex) {
			ex.printStackTrace();
    	    // handle any errors
    	    System.err.println("SQLException: " + ex.getMessage());
    	    System.err.println("SQLState: " + ((SQLException) ex).getSQLState());
    	    System.err.println("VendorError: " + ((SQLException) ex).getErrorCode());
    	    return 0;
		} finally {
    		if (stmt != null) {
    			try {
    				stmt.close();
    			} catch (SQLException sqlEx) { } // ignore
    			stmt = null;
    		}
    		if (preparedStmt != null) {
    			try {
    				preparedStmt.close();
    			} catch (SQLException sqlEx) { } // ignore
    				preparedStmt = null;
    		}
    		if (rs != null) {
    			try {
    				rs.close();
    		    } catch (SQLException sqlEx) { } // ignore
    		        rs = null;
    		}
    		if (conn != null) {
    			try {
    				conn.close();
    		    } catch (SQLException sqlEx) { } // ignore
    		        conn = null;
    		}
    	}
		
		return 1;
	}
	
	
	private String getUSPTOAbstract(String url) {
		// TODO Auto-generated method stub
		
		System.out.println("<=============== Inside getUSPTOAbstract()===============>");
		Document doc = null;
		
		// need http protocol
		//doc = Jsoup.connect("http://circ.ahajournals.org/content/131/4/e29.full.pdf+html").userAgent("Mozilla").ignoreHttpErrors(true).timeout(0).get();
		try {
			doc = Jsoup.connect(url).userAgent("Mozilla").ignoreHttpErrors(true).timeout(5000).get();
			if(doc == null) {
				System.err.println("Document is null ...exiting...");
				return null;
			} else {
				System.out.println("Document is valid ...proceeding with parsing the page...");
			}
			// get page title
			String pageTitle = doc.title();
			System.out.println("page title : " + pageTitle);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Elements items = doc.select("p");
		Element absItem =  null;
		String htmlText = null;
		if(items != null) {
			System.out.println("found table row =>" + items.size());
			if(items.size() != 0) {
				absItem = items.first();
				htmlText = absItem.html();
				
				System.out.println("Abstract text is =>" + htmlText);
			}else {
				return null;
			}
		}else {
			return null;
		}
		if(htmlText == null) {
			return null;
		}
		//String[] textSplitResult = htmlText.split("<br>");
		/*
		for (String temp: textSplitResult) {
			//System.out.println("Text=>"+ temp + "\n");
			if(temp != null) {
				if(temp.trim().startsWith("1.")) {
					abs = temp;
					break;
				}
			}
		}
		*/
		System.out.println(" abstract  =>" + htmlText);
		return htmlText.trim();
	}

	/*
	 * get abstract from google patent site
	 */
	
	private String getGPAbstract(String url) {
		// TODO Auto-generated method stub
		
		System.out.println("<=============== Inside getGPAbstract()===============>");
		Document doc = null;
		String abs = null;
		// need http protocol
		//doc = Jsoup.connect("http://circ.ahajournals.org/content/131/4/e29.full.pdf+html").userAgent("Mozilla").ignoreHttpErrors(true).timeout(0).get();
		try {
			doc = Jsoup.connect(url).userAgent("Mozilla").ignoreHttpErrors(true).timeout(5000).get();
			if(doc == null) {
				System.err.println("Document is null ...exiting...");
				return null;
			} else {
				System.out.println("Document is valid ...proceeding with parsing the page...");
			}
			// get page title
			String pageTitle = doc.title();
			System.out.println("page title : " + pageTitle);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(!doc.select("meta[name=DC.description]").isEmpty()) {
			abs = doc.select("meta[name=DC.description]").get(0).attr("content");  
			System.out.println("Abstract : " + abs  + "\n");
		}
		System.out.println(" abstract  =>" + abs);
		return abs.trim();
	}
	
	
	/*
	 * Master method to loop through uspto patent htmls and populate patent content
	 */
	public void parseUSPTOPatentMaster() {
		
		//loop through files and parse patent
		readPatentFiles(usptoBulkdataPath);
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		USPTOBulkDataParser parser = new USPTOBulkDataParser();
		//String url = "http://patft.uspto.gov/netacgi/nph-Parser?Sect1=PTO2&Sect2=HITOFF&p=1&u=%2Fnetahtml%2FPTO%2Fsearch-bool.html&r=1&f=G&l=50&co1=AND&d=PTXT&s1=6241718.PN.&OS=PN/6241718&RS=PN/6241718";
		//String url = "https://patents.google.com/patent/US20100179526A1/en";
		String url = "C:\\DP\\PatSage\\USPTO Patent downloads\\1434-1\\OG\\html\\1434-1\\US05882856-20170103.html";
		File file = new File(url);
		parser.parseUSPTOPatent(file);
		//parser.parseUSPTOPatentMaster();
	}

}
