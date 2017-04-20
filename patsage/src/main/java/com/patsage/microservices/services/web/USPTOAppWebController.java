package com.patsage.microservices.services.web;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.patsage.microservices.usptoapp.Patent;
import com.patsage.microservices.usptoapp.USPTOPatentJSON;
/**
 * Client controller, fetches Account info from the microservice via
 * {@link USPTOAppWebService}.
 * 
 * @author Divya Prakash
 */
@Controller
public class USPTOAppWebController {

	@Autowired
	protected USPTOAppWebService usptoAppWebService;
	
	// Define the logger object for this class
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public USPTOAppWebController(USPTOAppWebService usptoAppWebService) {
		this.usptoAppWebService = usptoAppWebService;
	}
	
	@RequestMapping("/usptoapp")
	public String goHome() {
		return "index";
	}
	
	@RequestMapping(value="/usptoapp/getpatent/{patentNumber}",method = RequestMethod.GET,produces = {"application/json"})
	public String byNumber(Model model,
			@PathVariable("patentNumber") String patentNumber) {
		logger.info("<============= inside Web-Service --- byNumber() invoked:========> " + patentNumber);

		USPTOPatentJSON patent = usptoAppWebService.getPatentByNumber(patentNumber);
		
		logger.info("<============ web-service byNumber() found:=====================> " + patent);
		model.addAttribute("patent", patent);
		return "patent";
	}
	
	@RequestMapping("/uspto/batchsearch")
	public String batchSearch(Model model) {
		logger.info("web-service batchSearch() invoked: ");
		
		List<Patent> patList = usptoAppWebService.runBatchSearch();
		logger.info("web-service byNumber() found: " + patList);
		model.addAttribute("patList", patList);
		return "patList";
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
