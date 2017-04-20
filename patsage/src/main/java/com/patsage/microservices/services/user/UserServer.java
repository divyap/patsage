/**
 * 
 */
package com.patsage.microservices.services.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

import com.patsage.microservices.patent.USPTOPatentDao;
import com.patsage.microservices.user.UserConfiguration;
import com.patsage.microservices.user.UsersDao;

/**
 * @author dprakash
 *
 */
@EnableAutoConfiguration
@EnableDiscoveryClient
@Import(UserConfiguration.class)
public class UserServer {

	// Define the logger object for this class
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
		
	@Autowired
	protected UsersDao userDao;

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
        System.setProperty("spring.config.name", "user-server");

        SpringApplication.run(UserServer.class, args);
	}

}
