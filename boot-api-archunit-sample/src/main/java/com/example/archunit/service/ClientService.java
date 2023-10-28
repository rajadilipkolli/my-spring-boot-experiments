package com.example.archunit.service;

import com.example.archunit.model.Client;
import com.example.archunit.repository.ClientRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

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
