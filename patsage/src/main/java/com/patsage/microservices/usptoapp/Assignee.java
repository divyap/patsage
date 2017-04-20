package com.patsage.microservices.usptoapp;

public class Assignee {
	
    /**
	 * Default constructor for JPA only.
	 */
    protected Assignee( ){
		
	}
	  
	protected String assignee_organization;
    /**
     * @return the assignee
    */
    public String getAssignee_organization() {
    	return assignee_organization;
    }
    	
    /**
     * @param assignee the assignee to set
     */
    public void setAssignee_organization(String assignee) {
    	this.assignee_organization = assignee;
    }
}
