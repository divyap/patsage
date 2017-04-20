package com.patsage.microservices.patent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.patsage.microservices.usptoapp.Patent;

/**
 * @author dprakash
 * The Entity annotation indicates that this class is a JPA entity.
 * The Table annotation specifies the name for the table in the db.
 */

@JsonIgnoreProperties(ignoreUnknown = true)

@Entity
@Table(name = "uspto_patent")
public class USPTOPatent {
	
	@Id
    //@GeneratedValue(strategy = GenerationType.AUTO)
    private int patentId;

	@NotNull
	private String patentnumber;
	
	private String appnumber;
	private Date filingdate;
	private String title;
	private Date grantdate;
	private String patentabstract;
	private String inventors;
	private String assignee;
	private Date ipcactiondate;
	private int searchId;
	private String type;
	private String country;
	private String patentkind;
	private int numclaims;
	private String filename;
	
	public USPTOPatent( ){
		
	}
	
	public USPTOPatent(Patent pat, int searchId, String usptourl ){
		this.setPatentnumber(pat.getPatent_number());
		this.setAppnumber(pat.getApp_number());
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		if(pat.getApp_date()!= null) {
			Date fileDate;
			try {
				fileDate = formatter.parse(pat.getApp_date());
				this.setFilingdate(fileDate);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}else {
			this.setFilingdate(null);
		}
		this.setTitle(pat.getPatent_title());
		if(pat.getPatent_date()!= null) {
			Date grantDate;
			try {
				grantDate = formatter.parse(pat.getPatent_date());
				this.setGrantdate(grantDate);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}else {
			this.setGrantdate(null);
		}
		this.setPatentAbstract(pat.getPatent_abstract());
		this.setInventors(pat.getInventor_first_name() + " " + pat.getInventor_last_name());
		this.setAssignee(pat.getAssignee_organization());
		if(pat.getIpc_action_date()!= null) {
			Date ipcDate;
			try {
				ipcDate = formatter.parse(pat.getIpc_action_date());
				this.setIpcactiondate(ipcDate);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else {
			this.setIpcactiondate(null);
		}
		this.setSearchId(searchId);
		this.setType(pat.getPatent_type());
		this.setCountry(pat.getApp_country());
		this.setPatentkind(pat.getPatentkind());
		String patnum = pat.getPatent_num_claims();
		if(patnum != null) {
			System.out.println("Patent_num_claims==>" + patnum.length());
			this.setNumclaims(Integer.parseInt(patnum));
		} else {
			this.setNumclaims(0);
		}
		this.setFilename(usptourl);
	}
   
	public USPTOPatent(String patentNumber, String appNumber, Date filingDate
		   , String patentTitle, Date grantDate, String patentAbstract, String inventors
		   , String assignee, Date ipcActionDate, int searchId, String patentType, String country 
		   , String patentKind, int claimsNum, String patentFileName){

		this.setPatentnumber(patentNumber);
		this.setAppnumber(appNumber);
		this.setFilingdate(filingDate);
		this.setTitle(patentTitle);
		this.setGrantdate(grantDate);
		this.setPatentAbstract(patentAbstract);
		this.setInventors(inventors);
		this.setAssignee(assignee);
		this.setIpcactiondate(ipcActionDate);
		this.setSearchId(searchId);
		this.setType(patentType);
		this.setCountry(country);
		this.setPatentkind(patentKind);
		this.setNumclaims(claimsNum);
		this.setFilename(patentFileName);
	}

	/**
	 * @return the patentId
	 */
	public int getPatentId() {
		return patentId;
	}

	/**
	 * @param patentNumber the patentNumber to set
	 */
	public void setPatentId(int patentId) {
		this.patentId = patentId;
	}

	
	/**
	 * @return the patentNumber
	 */
	public String getPatentnumber() {
		return patentnumber;
	}

	/**
	 * @param patentNumber the patentNumber to set
	 */
	public void setPatentnumber(String patentNumber) {
		this.patentnumber = patentNumber;
	}
	
	
	/**
	 * @return the inventors
	 */
	public String getInventors() {
		return inventors;
	}
	
	/**
	 * @param inventors the inventors to set
	 */
	public void setInventors(String inventors) {
		this.inventors = inventors;
	}
	
	/**
	 * @return the patentTitle
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * @param patentTitle the patentTitle to set
	 */
	public void setTitle(String patentTitle) {
		this.title = patentTitle;
	}
	
	/**
	 * @return the patentAbstract
	 */
	public String getPatentAbstract() {
		return patentabstract;
	}
	
	/**
	 * @param patentAbstract the patentAbstract to set
	 */
	public void setPatentAbstract(String patentAbstract) {
		this.patentabstract = patentAbstract;
	}
	
	/**
	 * @return the patentFileName
	 */
	public String getFilename() {
		return filename;
	}
	
	/**
	 * @param patentFileName the patentFileName to set
	 */
	public void setFilename(String patentFileName) {
		this.filename = patentFileName;
	}
	
	/**
	 * @return the appNumber
	 */
	public String getAppnumber() {
		return appnumber;
	}
	
	/**
	 * @param appNumber the appNumber to set
	 */
	public void setAppnumber(String appNumber) {
		this.appnumber = appNumber;
	}
	
	/**
	 * @return the filingDate
	 */
	public Date getFilingdate() {
		return filingdate;
	}
	
	/**
	 * @param filingDate the filingDate to set
	 */
	public void setFilingdate(Date filingDate) {
		this.filingdate = filingDate;
	}
	
	/**
	 * @return the grantDate
	 */
	public Date getGrantdate() {
		return grantdate;
	}
	
	/**
	 * @param grantDate the grantDate to set
	 */
	public void setGrantdate(Date grantDate) {
		this.grantdate = grantDate;
	}
	
	/**
	 * @return the ipcActionDate
	 */
	public Date getIpcactiondate() {
		return ipcactiondate;
	}
	
	/**
	 * @param ipcActionDate the ipcActionDate to set
	 */
	public void setIpcactiondate(Date ipcActionDate) {
		this.ipcactiondate = ipcActionDate;
	}
	
	/**
	 * @return the assignee
	 */
	public String getAssignee() {
		return assignee;
	}
	
	/**
	 * @param assignee the assignee to set
	 */
	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}
	
	/**
	 * @return the searchId
	 */
	public int getSearchId() {
		return searchId;
	}

	/**
	 * @param searchId the searchId to set
	 */
	public void setSearchId(int searchId) {
		this.searchId = searchId;
	}
	
	/**
	 * @return the patentType
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * @param patentType the patentType to set
	 */
	public void setType(String patentType) {
		this.type = patentType;
	}
	
	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}
	
	/**
	 * @param country the country to set
	 */
	public void setCountry(String country) {
		this.country = country;
	}
	
	/**
	 * @return the patentKind
	 */
	public String getPatentkind() {
		return patentkind;
	}
	
	/**
	 * @param patentKind the patentKind to set
	 */
	public void setPatentkind(String patentKind) {
		this.patentkind = patentKind;
	}
	
	/**
	 * @return the claimsNum
	 */
	public int getNumclaims() {
		return numclaims;
	}
	
	/**
	 * @param claimsNum the claimsNum to set
	 */
	public void setNumclaims(int claimsNum) {
		this.numclaims = claimsNum;
	}

   		
}