/**
 * 
 */
package com.patsage.microservices.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.patsage.microservices.patent.USPTOPatentDao;

/**
 * A class to test interactions with the MySQL database using the UsersDao class.
 * 
 * @author dprakash
 *
 */

@RestController
@RequestMapping(value="/rest/users")
public class UserController {
	
	// ------------------------
	// PRIVATE FIELDS
	// ------------------------
	// Define the logger object for this class
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected UsersDao userDao;
	
	@Autowired
	public UserController(UsersDao userDao) {
		this.userDao = userDao;

		logger.info("UsersDao has been initiated ");
	}

	
	// ------------------------
	// PUBLIC METHODS
	// ------------------------
	  
	  /**
	   * /create  --> Create a new user and save it in the database.
	   * 
	   * @param email User's email
	   * @param name User's name
	   * @return A string describing if the user is succesfully created or not.
	   */
	@RequestMapping(value="/create",method = RequestMethod.GET)
	@ResponseBody
	public String create(String username, String firstname, String lastname, String password) {
		Users user = null;
	    try {
	    	user = new Users(username, firstname, lastname,password );
	    	userDao.save(user);
	    }
	    catch (Exception ex) {
	    	return "Error creating the user: " + ex.toString();
	    }
	    return "User succesfully created! (id = " + user.getId() + ")";
	}
	  
	/**
	 * /delete  --> Delete the user having the passed id.
	 * 
	 * @param id The id of the user to delete
	 * @return A string describing if the user is succesfully deleted or not.
	 */
	@RequestMapping("/delete")
	@ResponseBody
	public String delete(long id) {
		try {
			Users user = new Users(id);
			userDao.delete(user);
	    }
	    catch (Exception ex) {
	    	return "Error deleting the user: " + ex.toString();
	    }
	    return "User succesfully deleted!";
	}
	  
	/**
	 * /get-by-username  --> Return the id for the user having the passed username.
	 * 
	 * @param email The username to search in the database.
	 * @return The user id or a message error if the user is not found.
	 */
	@RequestMapping(value="/getuser",method = RequestMethod.GET)
	@ResponseBody
	public String getByUserName(@RequestParam(value="name") String username) {
	    String userId;
	    String firstname;
	    try {
	    	Users user = userDao.findByUsername(username);
	    	userId = String.valueOf(user.getId());
	    	firstname = user.getFirstName();
	    }
	    catch (Exception ex) {
	    	ex.printStackTrace();
	    	return "User not found";
	    }
	    return "The user id is: " + userId + " -- " + firstname;
	}
	  
	/**
	 * /update  --> Update the email and the name for the user in the database 
	 * having the passed id.
	 * 
	 * @param id The id for the user to update.
	 * @param email The new email.
	 * @param name The new name.
	 * @return A string describing if the user is succesfully updated or not.
	 */
	@RequestMapping("/update")
	@ResponseBody
	public String updateUser(long userId, String firstname) {
		try {
			Users user = userDao.findOne(userId);
			user.setFirstName(firstname);
			userDao.save(user);
		}
	    catch (Exception ex) {
	    	return "Error updating the user: " + ex.toString();
	    }
	    return "User succesfully updated!";
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
        // Will configure using accounts-server.yml
        System.setProperty("spring.config.name", "user-server");

        SpringApplication.run(UserController.class, args);
	}

}
