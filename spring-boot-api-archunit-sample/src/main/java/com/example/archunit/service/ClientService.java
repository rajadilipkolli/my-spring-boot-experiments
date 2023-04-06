package com.example.archunit.service;

import com.example.archunit.model.Client;
import com.example.archunit.repository.ClientRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClientService {

    ClientRepository clientRepository;

    @Autowired
    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Boolean delete(Long id) {

        Optional<Client> client = clientRepository.findById(id);

        if (client.isPresent()) {
            client.get().setActive(Boolean.FALSE);
            clientRepository.save(client.get());
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }
}
