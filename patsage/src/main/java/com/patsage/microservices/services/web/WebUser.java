package com.patsage.microservices.services.web;

import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * @author dprakash
 *  * An entity User composed by three fields (username, firstname, lastname, password).
 * The Entity annotation indicates that this class is a JPA entity.
 * The Table annotation specifies the name for the table in the db.
 *
 */

@JsonRootName("WebUser")
public class WebUser {
	
	private long Id;
	private String username;
	private String firstname;
	  
	// The user's last name
	private String lastname;
	  
	// The user's password
	private String password;

	// ------------------------
	// PUBLIC METHODS
	// ------------------------
	  
	public WebUser() { }
	
	public WebUser(long id) { 
	    this.Id = id;
	}
	
	public WebUser(String username) { 
	    this.username = username;
	}
	
	public WebUser( String username, String firstname, String lastname, 
			  		String password) {
		    this.username = username;
		    this.firstname = firstname;
		    this.lastname = lastname;
		    this.password = password;
	}
	
	// Getter and setter methods

	public long getId() {
		return Id;
	}
	  
	public void setId(long value) {
		this.Id = value;
	}
	
	public String getUserName() {
		return username;
	}
	  
	public void setUserName(String value) {
		this.username = value;
	}
	  
	public String getFirstName() {
		return firstname;
	}
	  
	public void setFirstName(String value) {
		this.firstname = value;
	}
	
	public String getLastName() {
		return lastname;
	}
	  
	public void setLastName(String value) {
		this.lastname = value;
	}
	
	public String getPassword() {
		return password;
	}
	  
	public void setPassword(String value) {
		this.password = value;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
