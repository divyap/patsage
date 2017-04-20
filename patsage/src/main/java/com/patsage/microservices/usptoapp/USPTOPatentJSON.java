package com.patsage.microservices.usptoapp;

import java.util.Date;

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
@JsonRootName("USPTOPatentJSON")

public class USPTOPatentJSON {

    protected Patent[] patents;
    protected int count;
    protected int total_patent_count;
  
    /**
	 * Default constructor for JPA only.
	 */
    protected USPTOPatentJSON( ){
		
	}
    
    public int getCount(){ 
    	return this.count;
    }
    
    public void setCount(int count) {
    	this.count = count;
    }
    
    public int getTotal_patent_count(){ 
    	return this.total_patent_count;
    }
    
    public void setTotal_patent_count(int patentcount) {
    	this.total_patent_count = patentcount;
    }
    
    public Patent[] getPatents(){ 
    	return this.patents;
    }
    
    public void setPatents(Patent[] patents) {
    	this.patents = patents;
    }

}
