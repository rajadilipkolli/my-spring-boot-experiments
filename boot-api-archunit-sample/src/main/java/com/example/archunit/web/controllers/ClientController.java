package com.example.archunit.web.controllers;

import com.example.archunit.exception.ClientNotFoundException;
import com.example.archunit.model.query.FindClientsQuery;
import com.example.archunit.model.request.ClientRequest;
import com.example.archunit.model.response.ClientResponse;
import com.example.archunit.model.response.PagedResult;
import com.example.archunit.services.ClientService;
import com.example.archunit.utils.AppConstants;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public ResponseEntity<PagedResult<ClientResponse>> getAllClients(
            @RequestParam(value = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false)
                    int pageNo,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false)
                    int pageSize,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false)
                    String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false)
                    String sortDir) {
        FindClientsQuery findClientsQuery = new FindClientsQuery(pageNo, pageSize, sortBy, sortDir);
        return ResponseEntity.ok(clientService.findAllClients(findClientsQuery));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getClientById(@PathVariable Long id) {
        return clientService
                .findClientById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ClientNotFoundException(id));
    }

    @PostMapping
    public ResponseEntity<ClientResponse> createClient(@RequestBody @Validated ClientRequest clientRequest) {
        ClientResponse response = clientService.saveClient(clientRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/api/clients/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> updateClient(
            @PathVariable Long id, @RequestBody @Valid ClientRequest clientRequest) {
        return ResponseEntity.ok(clientService.updateClient(id, clientRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ClientResponse> deleteClient(@PathVariable Long id) {
        return clientService
                .findClientById(id)
                .map(client -> {
                    clientService.deleteClientById(id);
                    return ResponseEntity.ok(client);
                })
                .orElseThrow(() -> new ClientNotFoundException(id));
    }
}
