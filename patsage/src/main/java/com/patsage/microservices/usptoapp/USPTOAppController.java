/**
 * 
 */
package com.patsage.microservices.usptoapp;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.patsage.microservices.usptoapp.USPTOPatent;
import com.patsage.microservices.usptoapp.USPTOPatentDao;

/**
 * @author dprakash
 *
 */

@RestController
@RequestMapping(value="/rest/usptoapp")
public class USPTOAppController {
	
	// Define the logger object for this class
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private String usptoPatentHTMLUrl;
	private String apiURLMain;
	private String apiUrlFields;
	private String cryocathurl; 
	private String cryocorurl;
	private String assigneeurl;
	private String countryurl;
	private String appdate_keyurl;
	
	/*
	 * Method to read properties file
	 */
	public void setPatentProp() {
		
		try {
			Properties props = new Properties();
			InputStream resourceStream = 
				 Thread.currentThread().getContextClassLoader().getResourceAsStream("pat-config.properties");
			props.load(resourceStream);
			this.usptoPatentHTMLUrl = (String) props.getProperty("uspto.pat.url");
			this.apiURLMain = (String) props.getProperty("uspto.api.url");
			this.apiUrlFields = (String) props.getProperty("uspto.api.url.field");
			this.cryocathurl = (String) props.getProperty("uspto.cryocath.searchurl");
			this.cryocorurl = (String) props.getProperty("uspto.cryocor.searchurl");
			this.assigneeurl = (String) props.getProperty("uspto.assignee.searchurl");
			this.countryurl = (String) props.getProperty("uspto.country.searchurl");
			this.appdate_keyurl = (String) props.getProperty("uspto.appdate.url");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("apiUrlFields==>" + apiUrlFields);
	}

	
	//@Autowired
	//RestTemplate restTemplate;
	
	@Bean
    //@LoadBalanced
	@Autowired 
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
	
	protected USPTOPatentDao patentDao;
	
	@Autowired
	public USPTOAppController(USPTOPatentDao patentDao) {
		this.patentDao = patentDao;
		setPatentProp();
		logger.info("USPTOPatentDao has been initiated ");
	}

	/*
	 * method to fetch by given patent number
	 */
	@RequestMapping(value="/patentnum/{patnum}",method = RequestMethod.GET)
	@ResponseBody
	public USPTOPatentJSON getByPatentNumber(@PathVariable("patnum") String patnum) {
		System.out.println("<============= inside USPTOAppController: getByPatentNumber() invoked:========> " + patnum);
		USPTOPatentJSON patents = null;
		String urlparam = "{\"patent_number\":\""+ patnum + "\"}";
		RestTemplate restTemplate = restTemplate();
		try {
			//patent = restTemplate.getForObject("http://patentsview-service/query?q={urlparam}",
			//											USPTOPatentJSON.class, urlparam);
			patents = (USPTOPatentJSON) restTemplate.getForObject("http://patentsview.org"
														+ "/api/patents/query?q={urlparam}&f=" + apiUrlFields, 
														USPTOPatentJSON.class, urlparam);
			Patent patent = patents.getPatents()[0];
			System.out.println("patent number ==>" + patent.getPatent_number());
			System.out.println("patent title ==>" + patent.getPatent_title());
			System.out.println("patent assignee ==>" + patent.getAssignees()[0].getAssignee_organization());
			System.out.println("patent inventors ==>" + patent.getInventors()[0].getInventor_first_name());
			System.out.println("patent app_date ==>" + patent.getApplications()[0].getApp_date());
			System.out.println("patent ipc date ==>" + patent.getIPCs());
			String usptoURL = usptoPatentHTMLUrl + patnum + ".PN.&OS=PN/"+ patnum + "&RS=PN/" + patnum;
			System.out.println("patent URL ==>" + usptoURL);
			USPTOPatent dbPat = new USPTOPatent(patent, 5, usptoURL);
			patentDao.save(dbPat);
		}
	    catch (Exception ex) {
	    	ex.printStackTrace();
	    	return null;
	    }
		if (patents == null)
			return null;
		else
			return patents;
	}
	
	
	/*
	 * method to fetch all patents for a given keyword in using USPTO API
	 */
	@RequestMapping(value="/addPatent",method = RequestMethod.GET)
	@ResponseBody
	public void addPatent(USPTOPatentJSON pat) {
		
		USPTOPatent usptoPatent = new USPTOPatent();
		try {
			patentDao.save(usptoPatent);
	    }
	    catch (Exception ex) {
	    	ex.printStackTrace();
	    }
	}
	
	/*
	 * method to fetch all patents for a given assignee.
	 */
	@RequestMapping(value="/byassignee/{assignee}",method = RequestMethod.GET)
	@ResponseBody
	public USPTOPatentJSON getPatentsByAssignee(@PathVariable("assignee") String assignee) {
		System.out.println("<============= inside USPTOAppController: getPatentsByAssignee() invoked:========> " + assignee);
		USPTOPatentJSON patents = null;
		String page = "1";
		String per_page = "50";
		Map<String, String> uriParams = new HashMap<String, String>();
		uriParams.put("assignee", assignee);
		uriParams.put("page", page);
		uriParams.put("per_page", per_page);
		String urlparam = assigneeurl + "\""+ assignee + "\"" + countryurl; 
		
		System.out.println("api url   =====>" + apiURLMain + urlparam);
		RestTemplate restTemplate = restTemplate();
		try {
			//patent = restTemplate.getForObject("http://patentsview-service/query?q={urlparam}",
			//											USPTOPatentJSON.class, urlparam);
			while (1==1){
				System.out.println("Page=========>" + page);
				String pageInfo = "{\"page\":" + page + ",\"per_page\":" + per_page + "}";
				patents = (USPTOPatentJSON) restTemplate.getForObject("http://patentsview.org/api/patents/query?q={urlparam}&f="
																+ apiUrlFields + "&o={pageInfo}", USPTOPatentJSON.class, urlparam, pageInfo);
				Patent[] patentArray = patents.getPatents();
				int totalPatCount = patents.getCount();
				System.out.println("count of patents ==>" + totalPatCount );
				if(patentArray == null) {
					System.out.println("no more patents on the page ==>" + page);
					break;
				} else {
					System.out.println("count of patents in array ==>" + patentArray.length);
					for(int i=0; i< patentArray.length; i++) {
						Patent patent = patentArray[i];
						System.out.println("patent number ==>" + patent.getPatent_number());
						System.out.println("patent title ==>" + patent.getPatent_title());
						System.out.println("patent assignee ==>" + patent.getAssignees()[0].getAssignee_organization());
						System.out.println("patent inventors ==>" + patent.getInventors()[0].getInventor_first_name());
						System.out.println("patent app_date ==>" + patent.getApplications()[0].getApp_date());
						System.out.println("patent ipc date ==>" + patent.getIPCs());
						String usptoURL = usptoPatentHTMLUrl + patent.getPatent_number() + ".PN.&OS=PN/"+ patent.getPatent_number() + "&RS=PN/" 
												+ patent.getPatent_number();
						System.out.println("patent URL ==>" + usptoURL);
						USPTOPatent dbPat = new USPTOPatent(patent, 3, usptoURL);
						patentDao.save(dbPat);
						dbPat = null;
					}
					page = Integer.toString((Integer.parseInt(page) + 1));
					if(totalPatCount <= Integer.parseInt(per_page)){
						System.out.println("total count of patents were  less than per_page limit ==>" + per_page);
						break;
					} 
				} // end if block
				continue;
			}// end while loop
		}
	    catch (Exception ex) {
	    	ex.printStackTrace();
	    	return null;
	    }
		if (patents == null)
			return null;
		else
			return patents;
	}
	
	/*
	 * API call for Google Search and USPTO Search with Link Numbers, Keywords
	 * url for search by keywords:
	 * http://localhost:5555/rest/usptoapp/bykeyword/keyword1+keyword2+keyword3?num=25
	 */

	@RequestMapping(value="/bykeyword/{keywords}",method = RequestMethod.GET)
	@ResponseBody
	public USPTOPatentJSON getPatentsByKeywords(@PathVariable("keywords") String keywords,
												@RequestParam(value = "num", required = true) String num) {
		System.out.println("<============= inside USPTOAppController: getPatentsByKeywords() invoked:========> "
																						+ keywords + " num =>" +  num);
		USPTOPatentJSON patents = null;
		String page = "1";
		String per_page = num;
		keywords = keywords.replace("+", " ");
		Map<String, String> uriParams = new HashMap<String, String>();
		uriParams.put("keywords", keywords);
		uriParams.put("page", page);
		uriParams.put("per_page", per_page);
		String urlparam = appdate_keyurl + "\""+ keywords + "\"}},{\"_text_any\":{\"patent_abstract\":"
																				+ "\""+ keywords + "\"}}]}]}";
		
		RestTemplate restTemplate = restTemplate();
		try {
			//patent = restTemplate.getForObject("http://patentsview-service/query?q={urlparam}",
			//											USPTOPatentJSON.class, urlparam);
			while (1==1){
				System.out.println("Page=========>" + page);
				String pageInfo = "{\"page\":" + page + ",\"per_page\":" + per_page + "}";
				patents = (USPTOPatentJSON) restTemplate.getForObject("http://patentsview.org/api/patents/query?q={urlparam}&f="
																+ apiUrlFields + "&o={pageInfo}", USPTOPatentJSON.class, urlparam, pageInfo);
				Patent[] patentArray = patents.getPatents();
				int totalPatCount = patents.getCount();
				System.out.println("count of patents ==>" + totalPatCount );
				if(patentArray == null) {
					System.out.println("no more patents on the page ==>" + page);
					break;
				} else {
					System.out.println("count of patents in array ==>" + patentArray.length);
					for(int i=0; i< patentArray.length; i++) {
						Patent patent = patentArray[i];
						System.out.println("patent number ==>" + patent.getPatent_number());
						System.out.println("patent title ==>" + patent.getPatent_title());
						System.out.println("patent assignee ==>" + patent.getAssignees()[0].getAssignee_organization());
						System.out.println("patent inventors ==>" + patent.getInventors()[0].getInventor_first_name());
						System.out.println("patent app_date ==>" + patent.getApplications()[0].getApp_date());
						System.out.println("patent ipc date ==>" + patent.getIPCs());
						String usptoURL = usptoPatentHTMLUrl + patent.getPatent_number() + ".PN.&OS=PN/"+ patent.getPatent_number() + "&RS=PN/" 
												+ patent.getPatent_number();
						System.out.println("patent URL ==>" + usptoURL);
						USPTOPatent dbPat = new USPTOPatent(patent, 3, usptoURL);
						patentDao.save(dbPat);
						dbPat = null;
					}
					page = Integer.toString((Integer.parseInt(page) + 1));
					if(totalPatCount <= Integer.parseInt(per_page)){
						System.out.println("total count of patents were  less than per_page limit ==>" + per_page);
						break;
					} 
				} // end if block
				continue;
			}// end while loop
		}
	    catch (Exception ex) {
	    	ex.printStackTrace();
	    	return null;
	    }
		if (patents == null)
			return null;
		else
			return patents;
	}
	
	
	public static void main(String[] args) {
       

    }
}