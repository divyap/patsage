package com.patsage.microservices.patent;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

/**
* A DAO for the entity User is simply created by extending the CrudRepository
* interface provided by spring. The following methods are some of the ones
* available from such interface: save, delete, deleteAll, findOne and findAll.
* The magic is that such methods must not be implemented, and moreover it is
* possible create new query methods working only by defining their signature!
* 
* @author dprakash
*/

@Transactional
public interface USPTOPatentDao extends CrudRepository<USPTOPatent, Integer> {
	
	public USPTOPatent findByPatentnumber(String patentnumber);
	public List<USPTOPatent> findBySearchId(int searchId);
	public List<USPTOPatent> findByAssigneeContaining(String assigneeTxt);
	public List<USPTOPatent> findByInventorsContaining(String inventorsTxt);
	public List<USPTOPatent> findByTitleContaining(String titleTxt);
	public List<USPTOPatent> findByPatentabstractContaining(String abstractTxt);
	public List<USPTOPatent> findByFilingdateGreaterThan(Date fileDate);
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
