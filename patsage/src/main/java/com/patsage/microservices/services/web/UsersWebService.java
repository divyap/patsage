package com.patsage.microservices.services.web;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class UsersWebService {

	@Autowired        // NO LONGER auto-created by Spring Cloud (see below)
    @LoadBalanced
    protected RestTemplate restTemplate; 
	
	protected String serviceUrl;

	protected Logger logger = LoggerFactory.getLogger(UsersWebService.class
			.getName());
	
	private UsersWebService() {
		
	}
	
    public UsersWebService(String serviceUrl) {
        this.serviceUrl = serviceUrl.startsWith("http") ?
               serviceUrl : "http://" + serviceUrl;
    }
    
    /**
	 * The RestTemplate works because it uses a custom request-factory that uses
	 * Ribbon to look-up the service to use. This method simply exists to show
	 * this.
	 */
	@PostConstruct
	public void demoOnly() {
		// Can't do this in the constructor because the RestTemplate injection
		// happens afterwards.
		logger.warn("The RestTemplate request factory is "
				+ restTemplate.getRequestFactory().getClass());
	}

    public WebUser getByUserName(String username) throws Exception {
		logger.info("getByUserName() invoked:  for " + username);
		WebUser user = null;
		try {
			user = restTemplate.getForObject(serviceUrl
	                + "/users/getuser", WebUser.class, username);
		}catch (HttpClientErrorException e) { // 404
			// Nothing found
		}
        if (user == null)
            throw new Exception(username);
        else
            return user;
    }
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
