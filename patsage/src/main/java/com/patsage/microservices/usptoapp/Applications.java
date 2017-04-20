package com.patsage.microservices.usptoapp;

public class Applications {
    
	protected Applications() {
		
	}
	protected String app_number;
    protected String app_date;
    protected String app_country;
    /**
	 * @return the appNumber
	 */
	public String getApp_number() {
		return app_number;
	}
	
	/**
	 * @param appNumber the appNumber to set
	 */
	public void setApp_number(String app_number) {
		this.app_number = app_number;
	}
	/**
	 * @return the filingDate
	 */
	public String getApp_date() {
		return app_date;
	}
	
	/**
	 * @param filingDate the filingDate to set
	 */
	public void setApp_date(String app_date) {
		this.app_date = app_date;
	}
	/**
	 * @return the country
	 */
	public String getApp_country() {
		return app_country;
	}
	
	/**
	 * @param country the country to set
	 */
	public void setApp_country(String country) {
		this.app_country = country;
	}
}
