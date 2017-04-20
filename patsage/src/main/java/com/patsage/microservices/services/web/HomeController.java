package com.patsage.microservices.services.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {
	
	@RequestMapping("/")
	public String home() {
		return "index";
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
