/**
 * 
 */
package com.patsage.microservices.usptoapp;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.patsage.MYSQLConnector;

/**
 * This class is for preparing USPTO API search URL and getting back the results in JSON
 * 
 * @author dprakash
 *
 */
public class USPTOSearchApp {
	
	// Define the logger object for this class
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public ArrayList fetchSearchkeyword() {
		logger.debug("--------inside fetchSearchkeyword()-------->");
		ArrayList<Map> searchList = new ArrayList<Map>();
		//instantiate MySQL connection
  		MYSQLConnector mysql = new MYSQLConnector();
  		Connection conn = mysql.getmysqlConn();
  		PreparedStatement preparedStmt = null;
  		String sql = null;
		ResultSet rs = null;
			
  		try {
  			// Fetch all searchkeywords rows
			sql = "Select searchId, userId, keyword, linknum, moduleId, assignee,"
  	    		   	+ " country, cpc, gte_grantdate from patsage.ps_searchkeyword where active = 1 and sourceId = 2";
  			logger.debug(sql);
  	  		// create the mysql insert preparedstatement
  	    	preparedStmt = conn.prepareStatement(sql);
  	    	rs = preparedStmt.executeQuery();
  	  		 if (rs != null ) {
  	 	    	while(rs.next()) {
  	 	    		//call method to process USPTOAPI call
  	 	    		int searchId = rs.getInt("searchId");
  	 	    		logger.debug("search ID ==>" + searchId);
  	 	    		Map search = new HashMap();
	 		    	search.put("searchId", searchId);
	 		    	int userId = rs.getInt("userId");
	 		    	search.put("userId", userId);
	 		    	String keyword = rs.getString("keyword");
	 		    	search.put("keyword", keyword);
	 		    	int linknum = rs.getInt("linknum");
	 		    	search.put("linknum", linknum);
	 		    	int moduleId = rs.getInt("moduleId");
	 		    	search.put("moduleId", moduleId);
	 		    	String assignee = rs.getString("assignee");
	 		    	search.put("assignee", assignee);
	 		    	String country = rs.getString("country");
	 		    	search.put("country", country);
	 		    	String cpc = rs.getString("cpc");
	 		    	search.put("cpc", cpc);
	 		    	Date gte_grantdate = rs.getDate("gte_grantdate");
	 		    	search.put("gte_grantdate", gte_grantdate);
	 		    	searchList.add(search);
  	 	    	}
  	  		 }else {
  	  			logger.debug("Result set from ps_searchkeyword is NULL...");
  	  		 }
  		} catch (SQLException ex){
  		    // handle any errors
  		    System.err.println("SQLException: " + ex.getMessage());
  		    System.err.println("SQLState: " + ex.getSQLState());
  		    System.err.println("VendorError: " + ex.getErrorCode());
  		} finally {
  		    if (rs != null) {
  		        try {
  		            rs.close();
  		        } catch (SQLException sqlEx) { } // ignore
  		        rs = null;
  		    }
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
  		return searchList;
	}

	public void usptoSearchBatchMode() {
		logger.debug("--------inside usptoSearchbatchMode()-------->");
		ArrayList<Map> searchList = fetchSearchkeyword();
		if(searchList != null) {
			for(int i=0; i <searchList.size(); i++) {
				
				Map search = searchList.get(i);
				logger.debug("Search Keyword is ==>" + search.get("keyword"));
				//call uspto api call
				buildUSPTOSearchUrl(search);
			}
		}
	}
	
	/*
	 * Call this method with Search Map to build USPTO Search Url 
	 */
	
	public String buildUSPTOSearchUrl(Map key) {
		logger.debug("--------inside searchbyUSPTOAPI()-------->");
		// uspto api query
		String apiURL = "http://www.patentsview.org/api/patents/query?q=";

		if(key != null) {
			//Get search keywords 
			int searchId = (int) key.get("searchId");
	    	logger.debug("search ID ==>" + searchId);
		    int userId = (int) key.get("userId");
			String keyword = (String) key.get("keyword");
		    int linknum = (int) key.get("linknum");
		    int moduleId = (int) key.get("moduleId");
		    String assignee = (String) key.get("assignee");
		    String country = (String) key.get("country");
		    String cpc = (String) key.get("cpc");
		    Date gte_grantdate = (Date) key.get("gte_grantdate");
		    
		    //form uspto query api
		    if(assignee != null || assignee != "") {
		    	if (country != null || country != "") {
		    		if (keyword != null || keyword != "") {
		    			apiURL = apiURL + "{\"_and\":[{\"_gte\":{\"app_date\":\"" + gte_grantdate +"\"}},"
		    						+ "{\"_contains\":{\"assignee_organization\":\"" + assignee + "\"}},"
		    						+ "{\"_or\":[{\"_text_any\":{\"patent_title\":\"" + keyword +"\"}},"
		    						+ "{\"_text_any\":{\"patent_abstract\":\"" + keyword + "\"}}]},"
		    						+ "{\"_eq\":{\"app_country\":\""+ country+"\"}}]}"
		    						+ "&f=[\"patent_number\",\"app_number\",\"app_date\",\"patent_title\",\"patent_date\","
		    						+ "\"patent_abstract\",\"inventor_first_name\",\"inventor_last_name\","
		    						+ "\"assignee_organization\",\"ipc_action_date\",\"patent_type\",\"app_country\","
		    						+ "\"patent_kind\",\"patent_num_claims\"]&o={\"per_page\":" + linknum + "}";
		    		}else {
		    			apiURL = apiURL + "{\"_and\":[{\"_gte\":{\"app_date\":\"" + gte_grantdate +"\"}},"
		    					+ "{\"_contains\":{\"assignee_organization\":\"" + assignee + "\"}},"
		    					+ "{\"_eq\":{\"app_country\":\""+ country+"\"}}]}"
	    						+ "&f=[\"patent_number\",\"app_number\",\"app_date\",\"patent_title\",\"patent_date\","
	    						+ "\"patent_abstract\",\"inventor_first_name\",\"inventor_last_name\","
	    						+ "\"assignee_organization\",\"ipc_action_date\",\"patent_type\",\"app_country\","
	    						+ "\"patent_kind\",\"patent_num_claims\"]&o={\"per_page\":" + linknum + "}";
		    		}
		    	}else if (keyword != null || keyword != ""){
		    		apiURL = apiURL + "{\"_and\":[{\"_gte\":{\"app_date\":\"" + gte_grantdate +"\"}},"
    						+ "{\"_contains\":{\"assignee_organization\":\"" + assignee + "\"}},"
    						+ "{\"_or\":[{\"_text_any\":{\"patent_title\":\"" + keyword +"\"}},"
    						+ "{\"_text_any\":{\"patent_abstract\":\"" + keyword + "\"}}]}]}"
    						+ "&f=[\"patent_number\",\"app_number\",\"app_date\",\"patent_title\",\"patent_date\","
    						+ "\"patent_abstract\",\"inventor_first_name\",\"inventor_last_name\","
    						+ "\"assignee_organization\",\"ipc_action_date\",\"patent_type\",\"app_country\","
    						+ "\"patent_kind\",\"patent_num_claims\"]&o={\"per_page\":" + linknum + "}";
		    	}else {
		    		apiURL = apiURL + "{\"_and\":[{\"_gte\":{\"app_date\":\"" + gte_grantdate +"\"}},"
    						+ "{\"_contains\":{\"assignee_organization\":\"" + assignee + "\"}}"
    						+ "&f=[\"patent_number\",\"app_number\",\"app_date\",\"patent_title\",\"patent_date\","
    						+ "\"patent_abstract\",\"inventor_first_name\",\"inventor_last_name\","
    						+ "\"assignee_organization\",\"ipc_action_date\",\"patent_type\",\"app_country\","
    						+ "\"patent_kind\",\"patent_num_claims\"]&o={\"per_page\":" + linknum + "}";
		    	}
		    }else if(keyword != null || keyword != "") {
		    	if(country != null | country != "") {
		    		apiURL = apiURL + "{\"_and\":[{\"_gte\":{\"app_date\":\"" + gte_grantdate +"\"}},"
    						+ "{\"_or\":[{\"_text_any\":{\"patent_title\":\"" + keyword +"\"}},"
    						+ "{\"_text_any\":{\"patent_abstract\":\"" + keyword + "\"}}]},"
    						+ "{\"_eq\":{\"app_country\":\""+ country+"\"}}]}"
    						+ "&f=[\"patent_number\",\"app_number\",\"app_date\",\"patent_title\",\"patent_date\","
    						+ "\"patent_abstract\",\"inventor_first_name\",\"inventor_last_name\","
    						+ "\"assignee_organization\",\"ipc_action_date\",\"patent_type\",\"app_country\","
    						+ "\"patent_kind\",\"patent_num_claims\"]&o={\"per_page\":" + linknum + "}";
		    	} else {
		    		apiURL = apiURL + "{\"_and\":[{\"_gte\":{\"app_date\":\"" + gte_grantdate +"\"}},"
    						+ "{\"_or\":[{\"_text_any\":{\"patent_title\":\"" + keyword +"\"}},"
    						+ "{\"_text_any\":{\"patent_abstract\":\"" + keyword + "\"}}]}]}"
    						+ "&f=[\"patent_number\",\"app_number\",\"app_date\",\"patent_title\",\"patent_date\","
    						+ "\"patent_abstract\",\"inventor_first_name\",\"inventor_last_name\","
    						+ "\"assignee_organization\",\"ipc_action_date\",\"patent_type\",\"app_country\","
    						+ "\"patent_kind\",\"patent_num_claims\"]&o={\"per_page\":" + linknum + "}";
		    	}
		    	
		    }
		logger.debug("uspto url is ===>" + apiURL);
		} else {
			logger.error("search object is null....");
			return null;
		}	
		return apiURL;
	}
	
	
	
	/*
	else if(inventors != null && keyword == null) {
					// fetch all patents for a given inventor
					sql = "Select uspto_patentId, patent_number, app_number, filing_date, patent_title, grant_date,"
			    		   	+ " patent_abstract, inventors, assignee_organization, ipc_action_date,"
							+ " searchId, patent_type, country, patent_kind, num_claims, filename"
							+ " from patsage.uspto_patent where searchId =?"
							+ " and inventors like '%?%'";
					// create the mysql insert preparedstatement
					preparedStmt = conn.prepareStatement(sql);
					preparedStmt.setInt (1, Integer.parseInt(searchid));
					preparedStmt.setString (2, inventors);
					log.debug(preparedStmt.toString());
		}else if (inventors != null && keyword != null) {
		// fetch all patents for a given inventor with given keyword
			sql = "Select A.uspto_patentId, A.patent_number, A.app_number, A.filing_date, A.patent_title, A.grant_date,"
	    		   	+ " A.patent_abstract, A.inventors, A.assignee_organization, A.ipc_action_date,"
					+ " A.searchId, A.patent_type, A.country, A.patent_kind, A.num_claims, A.filename"
					+ " from patsage.uspto_patent A join patsage.ps_searchkeyword B"
					+ " on A.searchId = B.searchId where A.searchId =?"
					+ " and A.inventors like '%" +inventors + "%'"
					+ "and B.keyword like '%" + keyword +"%'";
			// create the mysql insert preparedstatement
			preparedStmt = conn.prepareStatement(sql);
			log.debug(preparedStmt.toString());
			
			preparedStmt.setInt (1, Integer.parseInt(searchid));
			log.debug(preparedStmt.toString());
		}
	
		// execute the preparedstatement
		rs = preparedStmt.executeQuery();
		
		 if (rs != null ) {
	 	while(rs.next()) {
	 		USPTOPatent patInfo = new USPTOPatent();
	 		String patentNum = (String)rs.getString("patent_number");
	 		System.out.print(patentNum);
	 		patInfo.setPatentnumber(patentNum);
	 		patInfo.setAppnumber(rs.getString("app_number"));
	 		String PublishDate = (String)rs.getString("filing_date");
	 		patInfo.setFilingdate(PublishDate);
	 		String PatentTitle = (String)rs.getString("patent_title");
	 		patInfo.setTitle(PatentTitle);
	 		String grantDate = (String)rs.getString("grant_date");
	 		patInfo.setFilename(grantDate);
	 		String PatentAbstract = (String)rs.getString("patent_abstract");
	 		patInfo.setPatentAbstract(PatentAbstract);
	 		String Inventors = (String)rs.getString("inventors");
	 		patInfo.setInventors(Inventors);
	 		String assignee = (String)rs.getString("assignee_organization");
	 		patInfo.setAssignee(assignee);
	 		String priorityDate = (String)rs.getString("ipc_action_date");
	 		patInfo.setIpcactiondate(priorityDate);
	 		int searchId = rs.getInt("searchId");
	 		patInfo.setSearchId(searchId);
	 		String patentType = (String)rs.getString("patent_type");
	 		patInfo.setType(patentType);
	 		String country = (String)rs.getString("country");
	 		patInfo.setCountry(country);
	 		String patentKind = (String)rs.getString("patent_kind");
	 		patInfo.setPatentkind(patentKind);;
	 		int claimsNum = rs.getInt("num_claims");
	 		patInfo.setNumclaims(claimsNum); 		    		
	 		String fileName = (String)rs.getString("filename");
	 		patInfo.setFilename(fileName);
	 		patList.add(patInfo);
	 	}
	} 
*/


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
