package com.patsage.microservices.services.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(useDefaultFilters = false)  // Disable component scanner
public class USPTOWebServer {
	
	/**
	 * URL uses the logical name of usptoapp-service - upper or lower case,
	 * doesn't matter.
	 */

	public static final String USPTOAPP_SERVICE_URL = "http://USPTOAPP-SERVICE";
	
	@LoadBalanced
	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
	
	/**
	 * The USPTOAppWebService encapsulates the interaction with the micro-service.
	 * 
	 * @return A new service instance.
	 */
	@Bean
	public USPTOAppWebService usptoAppWebService() {
		return new USPTOAppWebService(USPTOAPP_SERVICE_URL);
	}
	
	@Bean
	public USPTOAppWebController usptoAppWebController() {
	    return new USPTOAppWebController (usptoAppWebService());  // serviceUrl
	}
	
	@Bean
	public HomeController homeController() {
		return new HomeController();
	}
	
	public static void main(String[] args) {
		
		// Will configure using web-server.yml
        System.setProperty("spring.config.name", "usptoweb-server");
        SpringApplication.run(USPTOWebServer.class, args);
	}

}
