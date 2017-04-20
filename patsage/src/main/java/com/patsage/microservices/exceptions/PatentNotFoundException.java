package com.patsage.microservices.exceptions;


import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

/**
 * Allow the controller to return a 404 if an patent is not found by simply
 * throwing this exception. The @ResponseStatus causes Spring MVC to return a
 * 404 instead of the usual 500.
 * 
 * @author Divya Prakash
 */

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PatentNotFoundException extends RuntimeException  {

	private static final long serialVersionUID = 1L;

	public PatentNotFoundException(String patNumber) {
		super("No such patent found: " + patNumber);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
