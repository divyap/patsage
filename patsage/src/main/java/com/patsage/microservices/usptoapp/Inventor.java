package com.patsage.microservices.usptoapp;

public class Inventor {
	
	protected Inventor() {
   		
   	}
    protected String inventor_first_name;
    protected String inventor_last_name;
    /**
 	 * @return the inventors
   	 */
  	public String getInventor_first_name() {
   		return inventor_first_name;
   	}
   	
   	/**
   	 * @param inventors the inventors to set
   	 */
   	public void setInventor_first_name(String inventor_first_name) {
   		this.inventor_first_name = inventor_first_name;
   	}
   	
   	/**
   	 * @return the inventors
   	 */
   	public String getInventor_last_name() {
   		return inventor_last_name;
   	}
   	
   	/**
   	 * @param inventors the inventors to set
   	 */
   	public void setInventor_last_name(String inventor_last_name) {
   		this.inventor_last_name = inventor_last_name;
   	}
}
