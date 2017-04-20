/**
 * 
 */
package com.patsage.microservices.patent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.patsage.microservices.usptoapp.USPTOSearchApp;

/**
 * @author dprakash
 *
 */

@RestController
@RequestMapping(value="/rest/uspto")
public class USPTOPatentController {
	
	// Define the logger object for this class
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected USPTOPatentDao patentDao;
	
	@Autowired
	public USPTOPatentController(USPTOPatentDao patentDao) {
		this.patentDao = patentDao;

		logger.info("USPTOPatentDao has been initiated ");
	}

	
	/*
	 * method to fetch all patents for a given search id
	 */
	@RequestMapping(value="/search",method = RequestMethod.GET)
	@ResponseBody
	public List <USPTOPatent> getPatentsBySearchId(@RequestParam(value="searchid") String searchId) {
		
		List patList = null;
		try {
	    	patList = patentDao.findBySearchId(Integer.parseInt(searchId));
	    }
	    catch (Exception ex) {
	    	ex.printStackTrace();
	    	return null;
	    }
	    return patList;
	}
	
	/*
	 * method to fetch by given patent number
	 */
	@RequestMapping(value="/patentnum/{patnum}",method = RequestMethod.GET)
	@ResponseBody
	public USPTOPatent getByPatentNumber(@PathVariable("patnum") String patnum) {
		
		USPTOPatent patent = null;
		try {
			patent = patentDao.findByPatentnumber(patnum);
	    }
	    catch (Exception ex) {
	    	ex.printStackTrace();
	    	return null;
	    }
	    return patent;
	}
	
	/*
	 * method to fetch all patents for a given assignee
	 */
	@RequestMapping(value="/assignee",method = RequestMethod.GET)
	@ResponseBody
	public String getPatentsByAssignee(@RequestParam(value="assignee") String assignee) {
		
		List patList = null;
		USPTOPatent pat = null;
		try {
	    	patList = patentDao.findByAssigneeContaining(assignee);
	    	pat = (USPTOPatent)patList.get(0);
	    }
	    catch (Exception ex) {
	    	ex.printStackTrace();
	    	return null;
	    }
	    return pat.getPatentnumber();
	}
	
	/*
	 * method to fetch all patents for a given keyword in title
	 */
	@RequestMapping(value="/titlesearch",method = RequestMethod.GET)
	@ResponseBody
	public List <USPTOPatent> getPatentsByTitleKeyword(@RequestParam(value="titlekey") String titlekey) {
		
		List patList = null;
		try {
	    	patList = patentDao.findByTitleContaining(titlekey);
	    }
	    catch (Exception ex) {
	    	ex.printStackTrace();
	    	return null;
	    }
	    return patList;
	}
	
	/*
	 * method to fetch all patents for a given keyword in abstract
	 */
	@RequestMapping(value="/abstract",method = RequestMethod.GET)
	@ResponseBody
	public List <USPTOPatent> getPatentsByAbstractKeyword(@RequestParam(value="abstractkey") String abstractkey) {
		
		List patList = null;
		try {
	    	patList = patentDao.findByPatentabstractContaining(abstractkey);
	    }
	    catch (Exception ex) {
	    	ex.printStackTrace();
	    	return null;
	    }
	    return patList;
	}
	
	/*
	 * method to fetch all patents for a given keyword in Inventors field
	 */
	@RequestMapping(value="/inventors",method = RequestMethod.GET)
	@ResponseBody
	public List <USPTOPatent> getPatentsByInventors(@RequestParam(value="inventors") String inventors) {
		
		List patList = null;
		try {
	    	patList = patentDao.findByInventorsContaining(inventors);
	    }
	    catch (Exception ex) {
	    	ex.printStackTrace();
	    	return null;
	    }
	    return patList;
	}
	
	/*
	 * method to fetch all patents for a given keyword in using USPTO API
	 */
	@RequestMapping(value="/batchsearch",method = RequestMethod.GET)
	@ResponseBody
	public List <USPTOPatent> batchSearch() {
		
		List patList = null;
		USPTOSearchApp searchApp = new USPTOSearchApp();
		try {
	    	//patList = patentDao.findByInventorsContaining();
	    }
	    catch (Exception ex) {
	    	ex.printStackTrace();
	    	return null;
	    }
	    return patList;
	}
	
	/*
	
	@RequestMapping(value="/",method = RequestMethod.GET)
	@ResponseBody
	public ArrayList<USPTOPatent> getAllPatents(@RequestParam(value="searchid") String searchid, 
			@RequestParam(value="keyword", defaultValue = "unknown") String keyword, 
			@RequestParam(value="inventors",defaultValue = "unknown") String inventors){
			
		try {
  			// Fetch all patent infos stored in the system
  			if(inventors == null && keyword == null) {
  				sql = "Select uspto_patentId, patent_number, app_number, filing_date, patent_title, grant_date,"
  	  	    		   	+ " patent_abstract, inventors, assignee_organization, ipc_action_date,"
  						+ " searchId, patent_type, country, patent_kind, num_claims, filename"
  						+ " from patsage.uspto_patent where searchId =?";
  				log.debug(sql);
  	  			// create the mysql insert preparedstatement
  	    		preparedStmt = conn.prepareStatement(sql);
  	    		preparedStmt.setInt (1, Integer.parseInt(searchid));
  				
  			} else if(inventors != null && keyword == null) {
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
		return patlist;
	}
	*/
	
	
	public static void main(String[] args) {
       

    }
}