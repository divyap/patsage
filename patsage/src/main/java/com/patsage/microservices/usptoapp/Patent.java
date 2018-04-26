package com.patsage.microservices.usptoapp;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.patsage.microservices.services.web.USPTOPatentWebService;

/**
 * Account DTO - used to interact with the {@link USPTOPatentWebService}.
 * 
 * @author Divya Prakash
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonRootName("Patent")

public class Patent {

    protected String patent_number;
    protected String patent_title;
    protected String patent_date;
    protected String patent_abstract;
    protected String patent_type;
    protected String patent_kind;
    protected String patent_num_claims;
    protected Inventor[] inventors;
    protected Assignee[] assignees;
    protected Applications[] applications;
    protected IPCs[] IPCs;

    /**
	 * Default constructor for JPA only.
	 */
    protected Patent( ){
		
	}
	/**
	 * @return the patentNumber
	 */
	public String getPatent_number() {
		return patent_number;
	}

	/**
	 * @param patentNumber the patentNumber to set
	 */
	public void setPatent_number(String patent_number) {
		this.patent_number = patent_number;
	}
	/**
	 * @return the patentTitle
	 */
	public String getPatent_title() {
		return patent_title;
	}
	
	/**
	 * @param patentTitle the patentTitle to set
	 */
	public void setPatent_title(String patent_title) {
		this.patent_title = patent_title;
	}
	
	/**
	 * @return the patentAbstract
	 */
	public String getPatent_abstract() {
		return patent_abstract;
	}
	
	/**
	 * @param patentAbstract the patentAbstract to set
	 */
	public void setPatentAbstract(String patent_abstract) {
		this.patent_abstract = patent_abstract;
	}
	/**
	 * @return the grantDate
	 */
	public String getPatent_date() {
		return patent_date;
	}
	
	/**
	 * @param grantDate the grantDate to set
	 */
	public void setPatent_date(String patent_date) {
		this.patent_date = patent_date;
	}
	/**
	 * @return the patentType
	 */
	public String getPatent_type() {
		return patent_type;
	}
	
	/**
	 * @param patentType the patentType to set
	 */
	public void setPatent_type(String patentType) {
		this.patent_type = patentType;
	}
	/**
	 * @return the patentKind
	 */
	public String getPatentkind() {
		return patent_kind;
	}
	
	/**
	 * @param patentKind the patentKind to set
	 */
	public void setPatent_kind(String patentKind) {
		this.patent_kind = patentKind;
	}
	
	/**
	 * @return the claimsNum
	 */
	public String getPatent_num_claims() {
		return patent_num_claims;
	}
	
	/**
	 * set the claimsNum
	 */
	public void setPatent_num_claims(String numclaims) {
		this.patent_num_claims = numclaims;
	}
	
	/**
	 * @return the inventors
	 */
	public Inventor[] getInventors() {
		return inventors;
	}
	
	/**
	 * set the inventors
	 */
	public void setInventors(Inventor[] inventors) {
		this.inventors = inventors;
	}
	/**
	 * @return the assignees
	 */
	public Assignee[] getAssignees() {
		return assignees;
	}
	
	/**
	 * set the assignees
	 */
	public void setAssignees(Assignee[] assignees) {
		this.assignees = assignees;
	}
    
	/**
	 * @return the applications
	 */
	public Applications[] getApplications() {
		return applications;
	}
	
	/**
	 * set the applications
	 */
	public void setApplications(Applications[] applications) {
		this.applications = applications;
	}
	
	/**
	 * @return the IPCs
	 */
	public IPCs[] getIPCs() {
		return IPCs;
	}
	
	/**
	 * set the IPCs
	 */
	public void setIPCs(IPCs[] IPCs) {
		this.IPCs = IPCs;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	public String getApp_number() {
		// TODO Auto-generated method stub
		return null;
	}
	public String getApp_date() {
		// TODO Auto-generated method stub
		return null;
	}
	public String getInventor_first_name() {
		// TODO Auto-generated method stub
		return null;
	}
	public String getInventor_last_name() {
		// TODO Auto-generated method stub
		return null;
	}
	public String getAssignee_organization() {
		// TODO Auto-generated method stub
		return null;
	}
	public String getIpc_action_date() {
		// TODO Auto-generated method stub
		return null;
	}
	public String getApp_country() {
		// TODO Auto-generated method stub
		return null;
	}
}
