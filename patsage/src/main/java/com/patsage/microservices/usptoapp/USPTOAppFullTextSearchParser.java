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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
public class USPTOAppFullTextSearchParser {
	
	// Define the logger object for this class
		private final Logger logger = LoggerFactory.getLogger(this.getClass());
		private String usptoAppHTMLUrl;
		private String usptoAppSearchUrl;

	/*
	 * Method to read properties file
	 */
	public void setPatentProp() {
		
		try {
			Properties props = new Properties();
			InputStream resourceStream = 
				 Thread.currentThread().getContextClassLoader().getResourceAsStream("pat-config.properties");
			props.load(resourceStream);
			this.usptoAppHTMLUrl = (String) props.getProperty("uspto.app.url");
			this.usptoAppSearchUrl = (String) props.getProperty("uspto.app.mar.search.url");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//Constructor
	public USPTOAppFullTextSearchParser() {
		setPatentProp();
	}
	
	
	
	/*
	 * method to parse USPTO grant Patent html
	 * 
	 */
	public int parseAndStoreUSPTOPatentApp(String url) {
		System.out.println("<=============== Inside parseAndStoreUSPTOPatentApp()===============>");
		int result = 0;
		Document doc = null;
		Map<String, String> patent = new HashMap<String,String>();
		// need http protocol
		//doc = Jsoup.connect("http://circ.ahajournals.org/content/131/4/e29.full.pdf+html").userAgent("Mozilla").ignoreHttpErrors(true).timeout(0).get();
		try {
			System.out.println("URL to be parsed ==> " + url);
			doc = Jsoup.connect(url).userAgent("Mozilla").ignoreHttpErrors(true).timeout(15000).get();
			//doc = Jsoup.parse(file, null);
			if(doc == null) {
				System.err.println("Document is null ...exiting...");
				return 0;
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
		//fetch tbody from html body
		Elements tableItems = doc.select("tbody");
		System.out.println("count of tbody =>" + tableItems.size());
		//get tbody with patent Org/patent number/ grant date/inventor group
		Element tbody1 = tableItems.get(1);
		Elements tableCols = tbody1.select("b");
		System.out.println("PatentOrg =>" + tableCols.get(0).text());
		patent.put("PatentOrg", tableCols.get(0).text());
		System.out.println("PatentNum =>" + tableCols.get(1).text());
		patent.put("PatentNum", tableCols.get(2).text());
		System.out.println("InventorGrp =>" + tableCols.get(5).text());
		patent.put("InventorGrp", tableCols.get(5).text());
		System.out.println("PublishDate =>" + tableCols.get(6).text());
		patent.put("PublishDate", tableCols.get(6).text());

		//get tbody with inventors
		Element tbody2 = tableItems.get(2);	
		Elements tableRows = tbody2.getElementsByTag("tr");
		System.out.println("count of tableRows =>" + tableRows.text());
		for(Element tempRow : tableRows) {
			Elements tdItems = tempRow.getElementsByTag("td");
			for(Element tempTd : tdItems ) {
				String headerTxt = tempTd.text();
				if(headerTxt != null && headerTxt.equals("Inventors:")) {
					Elements inventors = tempRow.getElementsByTag("b");
					System.out.println("Inventors are =>" + inventors.text());
					patent.put("Inventors", inventors.text());
				} else if (headerTxt != null && headerTxt.equals("Assignee:")) {
					Elements assignees = tempRow.getElementsByTag("b");
					System.out.println("assignees are =>" + assignees.text());
					patent.put("Assignee", assignees.text());
				}else if (headerTxt != null && headerTxt.equals("Filed:")) {
					Elements filed = tempRow.getElementsByTag("b");
					System.out.println("File Date is =>" + filed.text());
					patent.put("FilingDate", filed.text());
				}
			}
		}
		//get tbody with Assignee/Applicant & file date
		Element tbody3 = tableItems.get(3);	
		Elements tbody3Rows = tbody3.getElementsByTag("tr");
		System.out.println("count of tbody3Rows =>" + tbody3Rows.text());
		for(Element temp3Row : tbody3Rows) {
			Elements thItems = temp3Row.getElementsByTag("th");
			if(thItems != null & thItems.size() >0) {
				String thText = thItems.get(0).text();
				if(thText != null && thText.equals("Applicant:")) {
					Elements applicantTable = temp3Row.getElementsByTag("tbody");
					String applicant = applicantTable.get(0).getElementsByTag("b").text();
					System.out.println("Applicant is =>" + applicant);
					patent.put("Applicant", applicant);
				}
			}else {
				Elements tdItems = temp3Row.getElementsByTag("td");
				if(tdItems != null && tdItems.size() >0) {
					String tdText = tdItems.get(0).text();
					if(tdText.equals("Filed:")) {
						String fileDate = temp3Row.getElementsByTag("b").text();
						System.out.println("Filing Date is =>" + fileDate);
						patent.put("FilingDate", fileDate);
					}
				}
			}
		}
		
		// Fetch patent abstract
		Elements items = doc.select("p");
		String htmlText = null;
		if(items != null && items.size() != 0) {
			System.out.println("count of P tags =>" + items.size());
			for(Element tempAbs : items ) {
				htmlText = tempAbs.text().trim();
				if(htmlText != null && htmlText.length() >0 ) {
					System.out.println("Abstract text is =>" + htmlText);
					patent.put("Abstract", htmlText);
				}
			}
		}
		
		// Get Top Claims
		StringBuffer claims = new StringBuffer();
		Elements bodyItems = doc.select("body");
		String bodyText = null;
		if(bodyItems != null) {
			bodyText = bodyItems.get(0).html();
			boolean flag = false ;
			String[] textSplitResult = bodyText.split("<br>");
			for (String temp: textSplitResult) {
				if(temp != null) {
					if(temp.trim().startsWith("1.")) {
						flag = true;
						System.out.println("claims Text=>"+ temp + "\n");
						//claims.append(temp);
					}else if(temp.trim().startsWith("2.")) {
						flag = false;
						System.out.println("Text=>"+ temp + "\n");
						break;
					}				
					if(flag) {
						claims.append(temp.trim());
						break;
					}
				}
			}
		}
		//System.out.println(" top claims  =>" + claims.toString());
		patent.put("TopClaim", claims.toString());
		result = saveUSPTOPatent(patent);
		return result;
	}
	
	
	/*
	 * Populate Claims for all Patents
	 */
	public int saveUSPTOPatent(Map<String, String> patent) {
		System.out.println("<==============inside saveUSPTOPatent()============>: ");
		//instantiate MySQL connection
    	MYSQLConnector mysql = new MYSQLConnector();
    	Connection conn = mysql.getmysqlConn();
    	Statement stmt = null;
    	PreparedStatement preparedStmt = null;
    	ResultSet rs = null;
    	try {
	   		String claimsql = "update patsage.ps_uspto_patent " +
	   								"SET patentsource = ?, patentstatus = ?, assignee = ?, inventors = ?,"
	   								+ " filingdate = ?, publishdate = ?, patentabstract = ?, topclaims = ?, claimdone = ?"
	   								+ " where patentnumber = ?"; 

	   		String pat = patent.get("PatentNum");
	   		String usptoURL = usptoAppHTMLUrl + pat + "%22.PGNR.&OS=DN/"+ pat + "&RS=DN/" + pat;
	   		System.out.println("uspto url==>: " + usptoURL);
	   		preparedStmt = conn.prepareStatement(claimsql);
	   		preparedStmt.setString (1, "USPTO");
	   		preparedStmt.setString (2, "Application");
	   		String assignee = patent.get("Assignee");
	   		if(assignee == null || assignee.equals("")) {
	   			assignee = patent.get("Applicant");
	   		}
	   		preparedStmt.setString (3, assignee);
	   		preparedStmt.setString (4, patent.get("Inventors"));
	   		preparedStmt.setString (5, patent.get("FilingDate"));
	   		preparedStmt.setString (6, patent.get("PublishDate"));
	   		preparedStmt.setString (7, patent.get("Abstract"));
	   		preparedStmt.setString (8, patent.get("TopClaim"));
	   		preparedStmt.setString (9, "Y");
	   		preparedStmt.setString (10, patent.get("PatentNum"));
	   		System.out.println("Update query ==>: " +preparedStmt.toString());
	   		preparedStmt.execute();

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
    			} catch (SQLException sqlEx) {return 0; } // ignore
    			stmt = null;
    		}
    		if (preparedStmt != null) {
    			try {
    				preparedStmt.close();
    			} catch (SQLException sqlEx) {return 0; } // ignore
    				preparedStmt = null;
    				
    		}
    		if (rs != null) {
    			try {
    				rs.close();
    		    } catch (SQLException sqlEx) { return 0; } // ignore
    		        rs = null;
    		       
    		}
    		if (conn != null) {
    			try {
    				conn.close();
    		    } catch (SQLException sqlEx) { return 0; } // ignore
    		        conn = null;
    		}
    	}
		
		return 1;
	}
	
	/*
	 * Search and save USPTO search Results
	 */
	public int fullTextSearchUSPTOApplication() {
		System.out.println("<==============inside fullTextSearchUSPTOApplication()============>: ");
		int result = 0;
		Document doc = null;
		Map<String, String> patent = new HashMap<String, String>();
		patent.put("patentstatus", "Application");
		// First search for Granted patents using Grant patent URL
		try {
			doc = Jsoup.connect(usptoAppSearchUrl).userAgent("Mozilla").ignoreHttpErrors(true).timeout(15000).get();
			if(doc == null) {
				System.err.println("Document is null ...exiting...");
				return 0;
			} else {
				System.out.println("Document is valid ...proceeding with parsing the page...");
			}
			// get page title
			String pageTitle = doc.title();
			System.out.println("page title : " + pageTitle);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
		// Get total search result count
		Elements iItems = doc.getElementsByTag("i");
		String totalResultCount = iItems.get(1).getElementsByTag("strong").get(2).text();
		System.out.println("total patent count: " + totalResultCount);
		
		// get search criteria
		Element formElement = doc.getElementsByTag("form").get(0); 
		Elements inputElements = formElement.getElementsByTag("input"); 
		for (Element inputElement : inputElements) {  
	        String key = inputElement.attr("name");  
	        String value = inputElement.attr("value");  
	        //System.out.println("Param name: "+key+" \nParam value: "+value);
	        if(key.equals("Query")) {
	        	patent.put("keyword", value);
	        }
	    }
		Element tbody = doc.select("tbody").get(0);
		Elements trItems = tbody.getElementsByTag("tr");
		for(Element tempTr : trItems) {
			Elements tdItems = tempTr.getElementsByTag("td");
			
			if(tdItems != null && tdItems.get(0).text() !=null && !tdItems.get(0).text().trim().equals("")) {
				String patNum = tdItems.get(1).text();
				String title = tdItems.get(2).text();
				System.out.println("Patent num : "+ patNum +"  title : "+ title);
				patent.put("patentnumber", patNum);
				patent.put("title", title);
				result = saveUSPTOSearchResults(patent);
				System.out.println(" Patent inserted in DB? : " + result);
			}
		}
		return result;
	}
	/*
	 *    insert USPTO search results into Database 
	 */
	public int saveUSPTOSearchResults(Map<String, String> patent) {
		//insert link to  MySQL database
    	MYSQLConnector mysql = new MYSQLConnector();
    	Connection conn = mysql.getmysqlConn();
    	PreparedStatement preparedStmt = null;
   	
    	try {
	   		String insertsql = "insert into patsage.ps_uspto_patent " +
	   								" (keyword, patentsource, patentcountry, patentstatus, patentnumber,  title, resultlink) "
	   								+ " VALUES (?, ?, ?, ?, ?, ?, ?)"; 
	   		String patNum = patent.get("patentnumber");
	   		String usptoURL = usptoAppHTMLUrl + patNum + "%22.PGNR.&OS=DN/"+ patNum + "&RS=DN/" + patNum;
	   		preparedStmt = conn.prepareStatement(insertsql);
	   		preparedStmt.setString (1, patent.get("keyword"));
	   		preparedStmt.setString (2, "USPTO");
	   		preparedStmt.setString (3, "US");
   			preparedStmt.setString (4, patent.get("patentstatus"));
   			preparedStmt.setString (5, patNum);
   			preparedStmt.setString (6, patent.get("title"));
   			preparedStmt.setString (7, usptoURL);
   			System.out.println(" insert query : " + preparedStmt.toString());
	   		preparedStmt.execute();
	   		
    	}catch (SQLException  ex) {
			ex.printStackTrace();
    	    // handle any errors
    	    System.err.println("SQLException: " + ex.getMessage());
    	    System.err.println("SQLState: " + ((SQLException) ex).getSQLState());
    	    System.err.println("VendorError: " + ((SQLException) ex).getErrorCode());
    	    return 0;
		} finally {
    		if (preparedStmt != null) {
    			try {
    				preparedStmt.close();
    			} catch (SQLException sqlEx) { } // ignore
    				preparedStmt = null;
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
	 *  select patent links from result set, parse and store it in DB
	 */
	public int parseUSPTOSearchResults() {
		System.out.println("<=============== Inside parseUSPTOSearchResults()===============>");
		//select from search results
		//instantiate MySQL connection
    	MYSQLConnector mysql = new MYSQLConnector();
    	Connection conn = mysql.getmysqlConn();
    	PreparedStatement stmt = null;
    	ResultSet rs = null;
    	int result = 0;
    	try {
	   		String patsql = "select resultlink from patsage.ps_uspto_patent where patentstatus = ? and claimdone = ? "
	   						+ " and patentabstract is null";
	   		System.out.println("query :" + patsql);
	   		stmt = conn.prepareStatement(patsql);
	   	    stmt.setString(1, "Application");
	   	    stmt.setString(2, "N");
	   	    rs = stmt.executeQuery();

	   	    while (rs.next()) {
	   	    	String usptoURL = rs.getString("resultlink");
	   	    	System.out.println("USPTO Link :" + usptoURL);
	   	    	result = parseAndStoreUSPTOPatentApp(usptoURL);
	   	    	System.out.println("is patent extraction succeeded? :" + result);
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
    			} catch (SQLException sqlEx) { return 0;} // ignore
    			stmt = null;
    		}
    		if (rs != null) {
    			try {
    				rs.close();
    		    } catch (SQLException sqlEx) {return 0; } // ignore
    		        rs = null;
    		}
    		if (conn != null) {
    			try {
    				conn.close();
    		    } catch (SQLException sqlEx) {return 0; } // ignore
    		        conn = null;
    		}
    	}
		
		return 1;
	}
	
	/*
	 * Master method to loop through uspto patent htmls and populate patent content
	 */
	public void USPTOPatentMaster() {
		
		int finalResult = 0;
		//search USPTO using keywords
		int result = fullTextSearchUSPTOApplication();
		
		
		if(result == 1) {
			finalResult = parseUSPTOSearchResults();
		}
		System.out.println("final result ==>: " + finalResult);
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		USPTOAppFullTextSearchParser parser = new USPTOAppFullTextSearchParser();
		String appUrl = "http://appft.uspto.gov/netacgi/nph-Parser?Sect1=PTO1&Sect2=HITOFF&d=PG01&p=1&u=%2Fnetahtml%2FPTO%2Fsrchnum.html&r=1&f=G&l=50&s1=%2220160038772%22.PGNR.&OS=DN/20160038772&RS=DN/20160038772";
		//int result = parser.parseAndStoreUSPTOPatentApp(appUrl);
		//int searchResult = parser.fullTextSearchUSPTOApplication();
		//int finalResult = parser.parseUSPTOSearchResults();

		parser.USPTOPatentMaster();
		
	}

}
