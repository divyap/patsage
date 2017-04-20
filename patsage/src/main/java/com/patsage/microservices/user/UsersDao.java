package com.patsage.microservices.user;

import javax.transaction.Transactional;

import org.springframework.data.repository.CrudRepository;

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
public interface UsersDao extends CrudRepository<Users, Long> {

	public Users findByUsername(String username);
	//public Users findByUserId(long userId);
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
