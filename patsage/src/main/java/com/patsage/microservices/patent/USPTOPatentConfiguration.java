package com.patsage.microservices.patent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * The USPTOPatent Spring configuration.
 * 
 * @author Divya Prakash
 */

@Configuration
@ComponentScan
@EntityScan("com.patsage.microservices.patent")
@EnableJpaRepositories("com.patsage.microservices.patent")
//@PropertySource("classpath:db-config.properties")
public class USPTOPatentConfiguration {

	private final Logger logger;

	public USPTOPatentConfiguration() {
		// Define the logger object for this class
		logger = LoggerFactory.getLogger(this.getClass());
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
