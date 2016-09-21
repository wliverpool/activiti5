package com.activiti.controller;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserRestController {
	
	private IdentityService identityService;

	public IdentityService getIdentityService() {
		return identityService;
	}

	@Autowired
	public void setIdentityService(IdentityService identityService) {
		this.identityService = identityService;
	}
	
	@RequestMapping(value="/userService/getUser/{userId}.ws",method=RequestMethod.GET)
	public ResponseEntity<User> getUser(@PathVariable("userId")String userId){
		User user = identityService.createUserQuery().userId(userId).singleResult();
		if(user==null){
			return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<User>(user,HttpStatus.OK);
	}

}
