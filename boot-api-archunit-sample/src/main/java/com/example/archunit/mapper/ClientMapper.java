package com.example.archunit.mapper;

import com.example.archunit.entities.Client;
import com.example.archunit.model.request.ClientRequest;
import com.example.archunit.model.response.ClientResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ClientMapper {

    public Client toEntity(ClientRequest clientRequest) {
        Client client = new Client();
        client.setText(clientRequest.text());
        return client;
    }

    public void mapClientWithRequest(Client client, ClientRequest clientRequest) {
        client.setText(clientRequest.text());
    }

    public ClientResponse toResponse(Client client) {
        return new ClientResponse(client.getId(), client.getText());
    }

    public List<ClientResponse> toResponseList(List<Client> clientList) {
        return clientList.stream().map(this::toResponse).toList();
    }
}
