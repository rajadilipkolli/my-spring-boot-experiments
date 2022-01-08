package com.example.archunit.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.example.archunit.model.Client;

@Repository
public interface ClientRepository extends PagingAndSortingRepository<Client, Long> {
	
}
