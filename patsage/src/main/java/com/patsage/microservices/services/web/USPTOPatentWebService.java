package com.patsage.microservices.services.web;

import java.util.ArrayList;
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

import com.patsage.microservices.patent.USPTOPatent;
import com.patsage.microservices.usptoapp.Patent;

@Service
public class USPTOPatentWebService {

	@Autowired        // NO LONGER auto-created by Spring Cloud (see below)
    @LoadBalanced
    protected RestTemplate restTemplate; 
	
	protected String serviceUrl;

	// Define the logger object for this class
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
    public USPTOPatentWebService(String serviceUrl) {
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

    public USPTOPatent getByPatentNumber(String patentNumber) throws Exception {
    	USPTOPatent pat = restTemplate.getForObject(serviceUrl
                + "/uspto/patentnum/{patentNumber}", USPTOPatent.class, patentNumber);

        if (pat == null)
            throw new Exception(patentNumber);
        else
            return pat;
    }
	

	public List<USPTOPatent> byTitleContains(String name) {
		logger.info("byTitleContains() invoked:  for " + name);
		USPTOPatent[] patents = null;

		try {
			patents = (USPTOPatent[]) restTemplate.getForObject(serviceUrl
					+ "/uspto/owner/{name}", USPTOPatent[].class, name);
		} catch (HttpClientErrorException e) { // 404
			// Nothing found
		}

		if (patents == null || patents.length == 0)
			return null;
		else
			return Arrays.asList(patents);
	}
	
	
	public Patent findByPatentNumber(String patentNumber) {
		logger.info("findByPatentNumber() invoked:  for " + patentNumber);
		Patent patent = null;

		try {
			patent = (Patent) restTemplate.getForObject(serviceUrl
					+ "/uspto/patentnum/{name}", Patent.class, patentNumber);
		} catch (HttpClientErrorException e) { // 404
			// Nothing found
		}

		if (patent == null)
			return null;
		else
			return patent;
	}
	
	public List<Patent> runBatchSearch() {
		logger.info("runBatchSearch() invoked:  ");
		Patent[] patents = null;

		try {
			patents = (Patent[])  restTemplate.getForObject(serviceUrl
									+ "/uspto/batchsearch", Patent[].class);
		} catch (HttpClientErrorException e) { // 404
			// Nothing found
		}

		if (patents == null)
			return null;
		else
			return Arrays.asList(patents);
	}

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
