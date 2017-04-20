package com.patsage.microservices.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * The USPTOPatent Spring configuration.
 * 
 * @author Divya Prakash
 */

@Configuration
@ComponentScan
@EntityScan("com.patsage.microservices.user")
@EnableJpaRepositories("com.patsage.microservices.user")
//@PropertySource("classpath:application.properties")
public class UserConfiguration {

	private final Logger logger;

	public UserConfiguration() {
		// Define the logger object for this class
		logger = LoggerFactory.getLogger(this.getClass());
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
