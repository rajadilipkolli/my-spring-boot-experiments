package com.example.archunit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.archunit.service.ClientService;

@RestController
@RequestMapping(value = "/client")
public class ClientController {

	ClientService clientService;
	
	@Autowired
	public ClientController(ClientService clientService) {
		this.clientService = clientService;
	}
	
	@DeleteMapping(value = "/{id}")
	public @ResponseBody ResponseEntity<Boolean> delete(@PathVariable("id") Long id) {
		return new ResponseEntity<>(clientService.delete(id), HttpStatus.OK);
	}
}
