package com.assignment.ExchangeApplication;

import com.assignment.ExchangeApplication.enums.UserRole;
import com.assignment.ExchangeApplication.exceptions.EmailExistsException;
import com.assignment.ExchangeApplication.exceptions.UsernameExistsException;
import com.assignment.ExchangeApplication.model.Client;
import com.assignment.ExchangeApplication.model.dto.ClientDto;
import com.assignment.ExchangeApplication.repository.ClientRepository;
import com.assignment.ExchangeApplication.service.ClientServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.assignment.ExchangeApplication.helpers.StatusMessages.EMAIL_IN_USE_ERROR;
import static com.assignment.ExchangeApplication.helpers.StatusMessages.USERNAME_IN_USE_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTests {
    @Mock
    private ClientRepository clientRepositoryMock;

    @Mock
    private PasswordEncoder passwordEncoderMock;

    @InjectMocks
    private ClientServiceImpl clientService;

    private ClientDto getTestClientDto() {
        ClientDto clientDto = new ClientDto();
        clientDto.setEmail("john.doe@example.com");
        clientDto.setPassword("TestPassword123");
        clientDto.setName("John Doe");
        clientDto.setUsername("johndoe");
        return clientDto;
    }

    @Test
    void registerClient_successfulRegistration() {
        ClientDto clientDto = getTestClientDto();
        when(clientRepositoryMock.existsByEmail(clientDto.getEmail())).thenReturn(false);
        when(clientRepositoryMock.existsByUsername(clientDto.getUsername())).thenReturn(false);
        when(clientRepositoryMock.save(any(Client.class))).thenReturn(null);

        Client client = clientService.registerClient(clientDto);

        assertEquals(clientDto.getEmail(), client.getEmail());
        assertEquals(clientDto.getName(), client.getName());
        assertEquals(clientDto.getUsername(), client.getUsername());
        assertEquals(UserRole.USER, client.getRole());
        verify(clientRepositoryMock).save(any(Client.class));
    }

    @Test
    void registerClient_emailExists_throwsException() {
        ClientDto clientDto = getTestClientDto();
        when(clientRepositoryMock.existsByEmail(clientDto.getEmail())).thenReturn(true);

        EmailExistsException thrown = assertThrows(EmailExistsException.class, () -> clientService.registerClient(clientDto));
        assertEquals(EMAIL_IN_USE_ERROR, thrown.getMessage());
        verify(clientRepositoryMock, never()).save(any());
    }

    @Test
    void registerClient_usernameExists_throwsException() {
        ClientDto clientDto = getTestClientDto();
        when(clientRepositoryMock.existsByEmail(clientDto.getEmail())).thenReturn(false);
        when(clientRepositoryMock.existsByUsername(clientDto.getUsername())).thenReturn(true);

        UsernameExistsException thrown = assertThrows(UsernameExistsException.class, () -> clientService.registerClient(clientDto));
        assertEquals(USERNAME_IN_USE_ERROR, thrown.getMessage());
        verify(clientRepositoryMock, never()).save(any());
    }

    @Test
    void registerClient_invalidEmail_throwsException() {
        ClientDto invalidClientDto = new ClientDto();
        invalidClientDto.setEmail("invalidemail.com");
        invalidClientDto.setPassword("TestPassword123");
        invalidClientDto.setName("John Doe");
        invalidClientDto.setUsername("johndoe");

        assertThrows(IllegalArgumentException.class, () -> clientService.registerClient(invalidClientDto));
        verify(clientRepositoryMock, never()).save(any());
    }

    @Test
    void registerClient_insecurePassword_throwsException() {
        ClientDto invalidClientDto = new ClientDto();
        invalidClientDto.setEmail("john.doe@example.com");
        invalidClientDto.setPassword("12345");
        invalidClientDto.setName("John Doe");
        invalidClientDto.setUsername("johndoe");

        assertThrows(IllegalArgumentException.class, () -> clientService.registerClient(invalidClientDto));
        verify(clientRepositoryMock, never()).save(any());
    }

    @Test
    void registerClient_saveThrowsException_throwsIllegalArgument() {
        ClientDto clientDto = getTestClientDto();
        when(clientRepositoryMock.existsByEmail(clientDto.getEmail())).thenReturn(false);
        when(clientRepositoryMock.existsByUsername(clientDto.getUsername())).thenReturn(false);
        doThrow(new RuntimeException("DB error")).when(clientRepositoryMock).save(any(Client.class));

        assertThrows(RuntimeException.class, () -> clientService.registerClient(clientDto));
    }
}
