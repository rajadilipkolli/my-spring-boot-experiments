package com.example.archunit.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willDoNothing;

import com.example.archunit.entities.Client;
import com.example.archunit.mapper.ClientMapper;
import com.example.archunit.model.response.ClientResponse;
import com.example.archunit.repositories.ClientRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientService clientService;

    @Test
    void findClientById() {
        // given
        given(clientRepository.findById(1L)).willReturn(Optional.of(getClient()));
        given(clientMapper.toResponse(any(Client.class))).willReturn(getClientResponse());
        // when
        Optional<ClientResponse> optionalClient = clientService.findClientById(1L);
        // then
        assertThat(optionalClient).isPresent();
        ClientResponse client = optionalClient.get();
        assertThat(client.id()).isOne();
        assertThat(client.text()).isEqualTo("junitTest");
    }

    @Test
    void deleteClientById() {
        // given
        willDoNothing().given(clientRepository).deleteById(1L);
        // when
        clientService.deleteClientById(1L);
        // then
        verify(clientRepository, times(1)).deleteById(1L);
    }

    private Client getClient() {
        Client client = new Client();
        client.setId(1L);
        client.setText("junitTest");
        return client;
    }

    private ClientResponse getClientResponse() {
        return new ClientResponse(1L, "junitTest");
    }
}
