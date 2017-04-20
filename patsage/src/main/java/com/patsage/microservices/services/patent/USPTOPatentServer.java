/**
 * 
 */
package com.patsage.microservices.services.patent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

import com.patsage.microservices.patent.USPTOPatentConfiguration;
import com.patsage.microservices.patent.USPTOPatentDao;

/**
 * @author dprakash
 *
 */
@EnableAutoConfiguration
@EnableDiscoveryClient
@Import(USPTOPatentConfiguration.class)
public class USPTOPatentServer {

	// Define the logger object for this class
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
		
	@Autowired
	protected USPTOPatentDao patentDao;

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
        System.setProperty("spring.config.name", "usptopatent-server");

        SpringApplication.run(USPTOPatentServer.class, args);
	}

}
