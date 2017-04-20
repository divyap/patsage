package com.patsage.microservices.services.web;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

public class UserWebController {

	@Autowired
	protected UsersWebService userService;
	
	// Define the logger object for this class
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public UserWebController(UsersWebService userService) {
		this.userService = userService;
	}
	
	@RequestMapping("/users")
	public String goHome() {
		return "index";
	}
	
	@RequestMapping("/users/{userName}")
	public String byUserName(Model model,
			@PathVariable("userName") String userName) {

		logger.info("web-service byUserName() invoked: " + userName);

		WebUser user;
		try {
			user = userService.getByUserName(userName);
			logger.info("web-service getByUserName() found: " + user);
			model.addAttribute("user", user);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "user";
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
