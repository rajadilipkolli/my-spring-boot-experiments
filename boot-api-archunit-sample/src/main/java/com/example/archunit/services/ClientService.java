package com.example.archunit.services;

import com.example.archunit.entities.Client;
import com.example.archunit.exception.ClientNotFoundException;
import com.example.archunit.mapper.ClientMapper;
import com.example.archunit.model.query.FindClientsQuery;
import com.example.archunit.model.request.ClientRequest;
import com.example.archunit.model.response.ClientResponse;
import com.example.archunit.model.response.PagedResult;
import com.example.archunit.repositories.ClientRepository;
import com.example.archunit.utils.PageUtils;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    public PagedResult<ClientResponse> findAllClients(FindClientsQuery findClientsQuery) {

        // create Pageable instance
        Pageable pageable = PageUtils.createPageable(findClientsQuery);

        Page<Client> clientsPage = clientRepository.findAll(pageable);

        List<ClientResponse> clientResponseList = clientMapper.toResponseList(clientsPage.getContent());

        return new PagedResult<>(clientsPage, clientResponseList);
    }

    public Optional<ClientResponse> findClientById(Long id) {
        return clientRepository.findById(id).map(clientMapper::toResponse);
    }

    @Transactional
    public ClientResponse saveClient(ClientRequest clientRequest) {
        Client client = clientMapper.toEntity(clientRequest);
        Client savedClient = clientRepository.save(client);
        return clientMapper.toResponse(savedClient);
    }

    @Transactional
    public ClientResponse updateClient(Long id, ClientRequest clientRequest) {
        Client client = clientRepository.findById(id).orElseThrow(() -> new ClientNotFoundException(id));

        // Update the client object with data from clientRequest
        clientMapper.mapClientWithRequest(client, clientRequest);

        // Save the updated client object
        Client updatedClient = clientRepository.save(client);

        return clientMapper.toResponse(updatedClient);
    }

    @Transactional
    public void deleteClientById(Long id) {
        clientRepository.deleteById(id);
    }
}
