package com.example.archunit.services;

import com.example.archunit.entities.Client;
import com.example.archunit.exception.ClientNotFoundException;
import com.example.archunit.mapper.ClientMapper;
import com.example.archunit.model.query.FindClientsQuery;
import com.example.archunit.model.request.ClientRequest;
import com.example.archunit.model.response.ClientResponse;
import com.example.archunit.model.response.PagedResult;
import com.example.archunit.repositories.ClientRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    public PagedResult<ClientResponse> findAllClients(FindClientsQuery findClientsQuery) {

        // create Pageable instance
        Pageable pageable = createPageable(findClientsQuery);

        Page<Client> clientsPage = clientRepository.findAll(pageable);

        List<ClientResponse> clientResponseList = clientMapper.toResponseList(clientsPage.getContent());

        return new PagedResult<>(clientsPage, clientResponseList);
    }

    private Pageable createPageable(FindClientsQuery findClientsQuery) {
        int pageNo = Math.max(findClientsQuery.pageNo() - 1, 0);
        Sort sort = Sort.by(
                findClientsQuery.sortDir().equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.Order.asc(findClientsQuery.sortBy())
                        : Sort.Order.desc(findClientsQuery.sortBy()));
        return PageRequest.of(pageNo, findClientsQuery.pageSize(), sort);
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
