package com.patsage.microservices.services.web;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.patsage.microservices.patent.USPTOPatent;
import com.patsage.microservices.usptoapp.Patent;

public class USPTOPatentWebController {

	@Autowired
	protected USPTOPatentWebService patentService;
	
	// Define the logger object for this class
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public USPTOPatentWebController(USPTOPatentWebService patentService) {
		this.patentService = patentService;
	}
	
	@RequestMapping("/uspto")
	public String goHome() {
		return "index";
	}
	
	@RequestMapping("/uspto/patentnum/{patentNumber}")
	public String byNumber(Model model,
			@PathVariable("patentNumber") String patentNumber) {

		logger.info("web-service byNumber() invoked: " + patentNumber);

		Patent patent = patentService.findByPatentNumber(patentNumber);
		logger.info("web-service byNumber() found: " + patent);
		model.addAttribute("patent", patent);
		return "patent";
	}
	
	@RequestMapping("/uspto/batchsearch")
	public String batchSearch(Model model) {
		logger.info("web-service batchSearch() invoked: ");
		
		List<Patent> patList = patentService.runBatchSearch();
		logger.info("web-service byNumber() found: " + patList);
		model.addAttribute("patList", patList);
		return "patList";
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
