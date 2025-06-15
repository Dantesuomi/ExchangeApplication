package com.assignment.ExchangeApplication;

import com.assignment.ExchangeApplication.controller.ClientController;
import com.assignment.ExchangeApplication.exceptions.EmailExistsException;
import com.assignment.ExchangeApplication.exceptions.UsernameExistsException;
import com.assignment.ExchangeApplication.model.Client;
import com.assignment.ExchangeApplication.model.dto.ClientDto;
import com.assignment.ExchangeApplication.service.interfaces.ClientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.UUID;

import static com.assignment.ExchangeApplication.helpers.StatusMessages.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClientControllerTests {

    @Mock
    private ClientService clientServiceMock;

    @Mock
    private Authentication authenticationMock;

    @InjectMocks
    private ClientController clientController;

    private Client getTestClient() {
        Client client = new Client();
        client.setId(UUID.fromString("50b24f6f-5c42-488d-9257-0329347e6da7"));
        client.setName("John Doe");
        client.setEmail("john.doe@example.com");
        client.setUsername("johndoe");
        return client;
    }

    private ClientDto getTestClientDto() {
        ClientDto clientDto = new ClientDto();
        clientDto.setEmail("john.doe@example.com");
        clientDto.setPassword("TestPassword123");
        clientDto.setName("John Doe");
        clientDto.setUsername("johndoe");
        return clientDto;
    }

    @Test
    void registerUser_success() {
        ClientDto clientDto = getTestClientDto();
        Client client = getTestClient();
        when(clientServiceMock.registerClient(any(ClientDto.class))).thenReturn(client);

        ResponseEntity<Object> response = clientController.registerUser(clientDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(client, response.getBody());
    }

    @Test
    void registerUser_usernameExists() {
        ClientDto clientDto = getTestClientDto();
        when(clientServiceMock.registerClient(any(ClientDto.class)))
                .thenThrow(new UsernameExistsException(USERNAME_IN_USE_ERROR));

        ResponseEntity<Object> response = clientController.registerUser(clientDto);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toString().contains(USERNAME_IN_USE_ERROR));
    }

    @Test
    void registerUser_emailExists() {
        ClientDto clientDto = getTestClientDto();
        when(clientServiceMock.registerClient(any(ClientDto.class)))
                .thenThrow(new EmailExistsException(EMAIL_IN_USE_ERROR));

        ResponseEntity<Object> response = clientController.registerUser(clientDto);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toString().contains(EMAIL_IN_USE_ERROR));
    }

    @Test
    void registerUser_illegalArgument() {
        ClientDto clientDto = new ClientDto();
        when(clientServiceMock.registerClient(any(ClientDto.class)))
                .thenThrow(new IllegalArgumentException(PASSWORD_POLICY_ERROR));

        ResponseEntity<Object> response = clientController.registerUser(clientDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toString().contains(PASSWORD_POLICY_ERROR));
    }

    @Test
    void getClientInfo_returnsClientInfo() {
        Client client = getTestClient();
        when(authenticationMock.getPrincipal()).thenReturn(client);

        ResponseEntity<Client> response = clientController.getClientInfo(authenticationMock);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(client, response.getBody());
    }
}
