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
public class USPTOFullTextSearchParser {
	
	// Define the logger object for this class
		private final Logger logger = LoggerFactory.getLogger(this.getClass());
		private String usptoBulkdataPath;
		private String usptoPatentHTMLUrl;
		private String usptoGrantPageUrl;
		private String usptoGrantPageUrl2;
		private String usptoGrantPageUrl3;
		private String usptoAppPageUrl;

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
			this.usptoGrantPageUrl = (String) props.getProperty("uspto.pat.apr3.search.url");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//Constructor
	public USPTOFullTextSearchParser() {
		setPatentProp();
	}
	
	
	
	/*
	 * method to parse USPTO grant Patent html
	 * 
	 */
	public int parseAndStoreUSPTOPatent(String url) {
		System.out.println("<=============== Inside parseUSPTOPatent()===============>");
		int result = 0;
		StringBuffer claims = new StringBuffer();
		Document doc = null;
		Map<String, String> patent = new HashMap<String,String>();
		// need http protocol
		//doc = Jsoup.connect("http://circ.ahajournals.org/content/131/4/e29.full.pdf+html").userAgent("Mozilla").ignoreHttpErrors(true).timeout(0).get();
		try {
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
		Element tbody2 = null;
		//get tbody with patent Org/patent number/ grant date/inventor group
		if(tableItems !=null && tableItems.size() > 0)
			tbody2 = tableItems.get(2);
		Elements tableCols = tbody2.getElementsByTag("b");
		System.out.println("PatentOrg =>" + tableCols.get(0).text());
		patent.put("PatentOrg", tableCols.get(0).text());
		System.out.println("PatentNum =>" + tableCols.get(1).text());
		patent.put("PatentNum", tableCols.get(1).text());
		System.out.println("InventorGrp =>" + tableCols.get(3).text());
		patent.put("InventorGrp", tableCols.get(3).text());
		System.out.println("GrantDate =>" + tableCols.get(4).text());
		patent.put("GrantDate", tableCols.get(4).text());

		//get tbody with inventors/Assignee/filedate
		Element tbody4 = tableItems.get(3);	
		Elements tableRows = tbody4.getElementsByTag("tr");
		for(Element tempRow : tableRows) {
			String headerTxt = tempRow.getElementsByTag("th").text();
			if(headerTxt != null && headerTxt.equals("Inventors:")) {
				Elements inventors = tempRow.getElementsByTag("b");
				System.out.println("Inventors are =>" + inventors.text());
				patent.put("Inventors", inventors.text());
			}else if(headerTxt != null && headerTxt.equals("Assignee:")) {
				Elements assignee = tempRow.getElementsByTag("b");
				System.out.println("Assignee is =>" + assignee.text());
				patent.put("Assignee", assignee.text());
			}else if(headerTxt != null && headerTxt.equals("Filed:")) {
				Elements fileDate = tempRow.getElementsByTag("b");
				System.out.println("Filing Date is =>" + fileDate.text());
				patent.put("FilingDate", fileDate.text());
			}
		}
		
		// Fetch patent abstract
		Elements items = doc.select("p");
		Element absItem =  null;
		String htmlText = null;
		if(items != null) {
			System.out.println("found table row =>" + items.size());
			if(items.size() != 0) {
				absItem = items.first();
				htmlText = absItem.html();
				
				System.out.println("Abstract text is =>" + htmlText);
				patent.put("Abstract", htmlText);
			}else {
				patent.put("Abstract", htmlText);
			}
		}else {
			patent.put("Abstract", htmlText);
		}
		if(htmlText == null) {
			patent.put("Abstract", htmlText);
		}
		
		// Get Top Claims
		Elements bodyItems = doc.select("body");
		String bodyText = null;
		if(bodyItems != null) {
			bodyText = bodyItems.get(0).html();
			boolean flag = false ;
			String[] textSplitResult = bodyText.split("<br>");
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
		}
		System.out.println(" top claims  =>" + claims.toString());
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
	   								+ " filingdate = ?, grantdate = ?, patentabstract = ?, topclaims = ? , claimdone = ? "
	   								+ " where patentnumber = ?"; 

	   		String pat = patent.get("PatentNum");
	   		String usptoURL = usptoPatentHTMLUrl + pat + ".PN.&OS=PN/"+ pat + "&RS=PN/" + pat;
	   		System.out.println("uspto url==>: " + usptoURL);
	   		preparedStmt = conn.prepareStatement(claimsql);
	   		preparedStmt.setString (1, "USPTO");
	   		preparedStmt.setString (2, "Grant");
	   		preparedStmt.setString (3, patent.get("Assignee"));
	   		preparedStmt.setString (4, patent.get("Inventors"));
	   		preparedStmt.setString (5, patent.get("FilingDate"));
	   		preparedStmt.setString (6, patent.get("GrantDate"));
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
	public int fullTextSearchUSPTOPatent(String keywords) {
		System.out.println("<==============inside fullTextSearchUSPTOPatent()============>: ");
		int totalCount = 0;
		int loopCt = 0;
		int result = 0;
		Document doc = null;
		Map<String, String> patent = new HashMap<String, String>();
		patent.put("patentstatus", "Grant");
		// First search for Granted patents using Grant patent URL
		System.out.println("USPTO URL is : \n" + usptoGrantPageUrl);
		try {
			doc = Jsoup.connect(usptoGrantPageUrl).userAgent("Mozilla").ignoreHttpErrors(true).timeout(15000).get();
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
		if(totalResultCount != null) {
			totalCount = Integer.parseInt(totalResultCount);
		}
		System.out.println("total patent count: " + totalCount);
		int divisor = 50;
		int quotient = totalCount/divisor;
		int remiander = totalCount%divisor;
		if(remiander != 0) {
			loopCt = quotient + 1;
		}else {
			loopCt = quotient;
		}
		System.out.println("total loop count: " + loopCt);
		for (int i=1; i <=loopCt; i++) {
			usptoGrantPageUrl = usptoGrantPageUrl.replaceAll("NextList[1-9]\\d*", 
																"NextList"+String.valueOf(i));
			System.out.println("new url is : \n" + usptoGrantPageUrl);

			try {
				doc = Jsoup.connect(usptoGrantPageUrl).userAgent("Mozilla").ignoreHttpErrors(true).timeout(15000).get();
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
			Element formElement = doc.getElementsByTag("form").get(2); 
			Elements inputElements = formElement.getElementsByTag("input"); 
			for (Element inputElement : inputElements) {  
		        String key = inputElement.attr("name");  
		        String value = inputElement.attr("value");  
		        //System.out.println("Param name: "+key+" \nParam value: "+value);
		        if(key.equals("Query")) {
		        	patent.put("keyword", value);
		        }
		    }
			Element tbody = doc.select("tbody").get(1);
			Elements trItems = tbody.getElementsByTag("tr");
			for(Element tempTr : trItems) {
				Elements tdItems = tempTr.getElementsByTag("td");
				if(tdItems.size() == 4) {
					String patNum = tdItems.get(1).text();
					String title = tdItems.get(3).text();
					System.out.println("Patent num : "+ patNum +"  title : "+ title);
					patent.put("patentnumber", patNum);
					patent.put("title", title);
					result = saveUSPTOSearchResults(patent);
					System.out.println(" Patent inserted in DB? : " + result);
				}else {
					continue;
				}
			}

		}
		return result;
	}
	
	
	/*
	 *    insert USPTO search results into Database 
	 */
	public int saveUSPTOSearchResults(Map<String, String> patent) {
		System.out.println("<==============inside saveUSPTOSearchResults()============>: ");
		//insert link to  MySQL database
    	MYSQLConnector mysql = new MYSQLConnector();
    	Connection conn = mysql.getmysqlConn();
    	PreparedStatement preparedStmt = null;
   	
    	try {
	   		String insertsql = "insert into patsage.ps_uspto_patent " +
	   								" (keyword, patentsource, patentcountry, patentstatus, patentnumber,  title, resultlink) "
	   								+ " VALUES (?, ?, ?, ?, ?, ?, ?)"; 
	   		String patNum = patent.get("patentnumber");
	   		String usptoURL = usptoPatentHTMLUrl + patNum + ".PN.&OS=PN/"+ patNum + "&RS=PN/" + patNum;
	   		preparedStmt = conn.prepareStatement(insertsql);
	   		preparedStmt.setString (1, patent.get("keyword"));
	   		preparedStmt.setString (2, "USPTO");
	   		preparedStmt.setString (3, "US");
   			preparedStmt.setString (4, patent.get("patentstatus"));
   			preparedStmt.setString (5, patNum);
   			preparedStmt.setString (6, patent.get("title"));
   			preparedStmt.setString (7, usptoURL);
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
		System.out.println("<==============inside parseUSPTOSearchResults()============>: ");
		//select from search results
		//instantiate MySQL connection
    	MYSQLConnector mysql = new MYSQLConnector();
    	Connection conn = mysql.getmysqlConn();
    	PreparedStatement stmt = null;
    	ResultSet rs = null;
    	try {
	   		String patsql = "select resultlink from patsage.ps_uspto_patent  where patentstatus = ? and claimdone = ? "
	   							+ " and patentabstract is null";
	   		System.out.println("query :" + patsql);
	   		stmt = conn.prepareStatement(patsql);
	   	    stmt.setString(1, "Grant");
	   	    stmt.setString(2, "N");
	   	    rs = stmt.executeQuery();
	   	    System.out.println("total patent rows to be parsed ==> :" + rs.getFetchSize());
	   	    while (rs.next()) {
	   	    	String usptoURL = rs.getString("resultlink");
	   	    	System.out.println("USPTO Link :" + usptoURL);
	   	    	int result = parseAndStoreUSPTOPatent(usptoURL);
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
	public void USPTOPatentMaster(String keywords) {
		System.out.println("<==============inside USPTOPatentMaster()============>: ");
		int finalResult = 0;
		//search USPTO using keywords
		int result = fullTextSearchUSPTOPatent(keywords);
		
		
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
		
		USPTOFullTextSearchParser parser = new USPTOFullTextSearchParser();
		String url = "http://patft.uspto.gov/netacgi/nph-Parser?Sect1=PTO2&Sect2=HITOFF&p=1&u=%2Fnetahtml%2FPTO%2Fsearch-bool.html&r=1&f=G&l=50&co1=AND&d=PTXT&s1=8758334.PN.&OS=PN/8758334&RS=PN/8758334";
		String keyword = "Cryo AND Ablation AND Catheter";
		//int result = parser.fullTextSearchUSPTOPatent(keyword);
		int finalResult = parser.parseUSPTOSearchResults();
		//int finalResult = parser.parseAndStoreUSPTOPatent(url);
		//parser.USPTOPatentMaster(keyword);
		
	}

}
