package com.patsage.microservices.services.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

import com.patsage.microservices.usptoapp.USPTOPatentJSON;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(useDefaultFilters = false)  // Disable component scanner

public class WebServer {
	
	// Define the logger object for this class
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * URL uses the logical name of account-service - upper or lower case,
	 * doesn't matter.
	 */
	public static final String USPTOPATENT_SERVICE_URL = "http://USPTOPATENT-SERVICE";

	public static final String USER_SERVICE_URL = "http://USER-SERVICE";
	
	public static final String USPTOAPP_SERVICE_URL = "http://USPTOAPP-SERVICE";
	
	@LoadBalanced
	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
	
	/**
	 * The UsersWebService encapsulates the interaction with the micro-service.
	 * 
	 * @return A new service instance.
	 */
	@Bean
	public UsersWebService userService() {
		return new UsersWebService(USER_SERVICE_URL);
	}
	
	/**
	 * The USPTOPatentWebService encapsulates the interaction with the micro-service.
	 * 
	 * @return A new service instance.
	 */
	@Bean
	public USPTOPatentWebService patentService() {
		return new USPTOPatentWebService(USPTOPATENT_SERVICE_URL);
	}
	
	/**
	 * The USPTOAppWebService encapsulates the interaction with the micro-service.
	 * 
	 * @return A new service instance.
	 */
	@Bean
	public USPTOAppWebService usptoService() {
		return new USPTOAppWebService(USPTOAPP_SERVICE_URL);
	}
	
	@Bean
	public UserWebController userController() {
		// 1. Value should not be hard-coded, just to keep things simple
	    //        in this example.
	    // 2. Case insensitive: could also use: http://accounts-service
	    return new UserWebController(userService());  // serviceUrl
	}
	
	@Bean
	public USPTOPatentWebController patentController() {
		// 1. Value should not be hard-coded, just to keep things simple
	    //        in this example.
	    // 2. Case insensitive: could also use: http://accounts-service
	    return new USPTOPatentWebController
	    			(patentService());  // serviceUrl
	}
	
	@Bean
	public USPTOAppWebController usptoAppWebController() {
		// 1. Value should not be hard-coded, just to keep things simple
	    //        in this example.
	    // 2. Case insensitive: could also use: http://accounts-service
	    return new USPTOAppWebController
	    			(usptoService());  // serviceUrl
	}
	
	@Bean
	public HomeController homeController() {
		return new HomeController();
	}
	
	/*
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	@Bean
	public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
		return args -> {
			String urlparam = "{\"patent_number\":\""+ patnum + "\"}";
			String apiURL = "http://patentsview.org/api/patents/query?q={urlparam}";

			USPTOPatentJSON patent = (USPTOPatentJSON) restTemplate.getForObject(
					"http://patentsview.org/api/patents/query?q={urlparam}", USPTOPatentJSON.class, urlparam);
			logger.info(patent.toString());
		};
	}
	*/
	public static void main(String[] args) {
		
		// Will configure using web-server.yml
        System.setProperty("spring.config.name", "web-server");
        SpringApplication.run(WebServer.class, args);
	}

}
