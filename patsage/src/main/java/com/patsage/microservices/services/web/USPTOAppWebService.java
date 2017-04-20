package com.patsage.microservices.services.web;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.patsage.microservices.usptoapp.Patent;
import com.patsage.microservices.usptoapp.USPTOPatentJSON;

@Service
public class USPTOAppWebService {
	
	@Autowired        // NO LONGER auto-created by Spring Cloud (see below)
    @LoadBalanced
    RestTemplate restLoadBalanced; 

	/*
	@Autowired  
    private RestTemplate restTemplate; 
	
	
	@Autowired
	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
	*/
	
	protected String serviceUrl;
	

	// Define the logger object for this class
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
    public USPTOAppWebService(String serviceUrl) {
        this.serviceUrl = serviceUrl.startsWith("http") ?
               serviceUrl : "http://" + serviceUrl;
    }
    
    @Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
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
		//RestTemplate restTemplate = restTemplate();
		logger.warn("The RestTemplate request factory is "
				+ restLoadBalanced.getRequestFactory().getClass());
	}

    	
	public USPTOPatentJSON getPatentByNumber(String patentNumber) {
		logger.info("<============= inside getPatentByNumber() invoked:========> " + patentNumber);
		System.out.println("<========= inside USPTOAppWebService: getPatentByNumber() invoked:=====> " + patentNumber);
		
		USPTOPatentJSON patents = null;
		//RestTemplate restTemplate = restTemplate();
		try {
			//String urlparam = "{\"patent_number\":\""+ patentNumber + "\"}";
			//patents = (USPTOPatentJSON) restTemplate.getForObject("http://patentsview.org"
			//											+ "/api/patents/query?q={urlparam}", USPTOPatentJSON.class, urlparam);
			//Patent patent = patents.getPatents()[0];
			//System.out.println("patent number ==>" + patent.getPatent_number());
			//System.out.println("patent title ==>" + patent.getPatent_title());
			//USPTOPatent dbPat = new USPTOPatent(patent, 5, "url");
			//patentDao.save(dbPat);
			System.out.println("serviceurl:=====> " + serviceUrl);
			patents = (USPTOPatentJSON) restLoadBalanced.getForObject(serviceUrl
											+ "/usptoapp/patentnum/{patnum}", USPTOPatentJSON.class, patentNumber);
		} catch (HttpClientErrorException e) { // 404
			// Nothing found
		}

		if (patents == null)
			return null;
		else
			return patents;
	}
	
	public List<Patent> runBatchSearch() {
		logger.info("runBatchSearch() invoked:  ");
		Patent[] patents = null;
		//RestTemplate restTemplate = restTemplate();
		try {
			patents = (Patent[])  restLoadBalanced.getForObject(serviceUrl
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
