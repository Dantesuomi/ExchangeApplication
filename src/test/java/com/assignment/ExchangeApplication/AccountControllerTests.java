package com.assignment.ExchangeApplication;

import com.assignment.ExchangeApplication.controller.AccountController;
import com.assignment.ExchangeApplication.enums.CurrencyCode;
import com.assignment.ExchangeApplication.model.Account;
import com.assignment.ExchangeApplication.model.Client;
import com.assignment.ExchangeApplication.model.dto.AccountCreateRequest;
import com.assignment.ExchangeApplication.model.dto.AccountResponseDto;
import com.assignment.ExchangeApplication.service.interfaces.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.assignment.ExchangeApplication.helpers.StatusMessages.UNAUTHORIZED_ACCOUNT_ERROR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerTests {

    @Mock
    private AccountService accountServiceMock;

    @Mock
    private Authentication authenticationMock;

    @InjectMocks
    private AccountController accountController;

    private UUID getTestAccountId() {
        return UUID.fromString("df0d2ac6-d0d3-4120-93e0-b20f3f00c0b3");
    }

    private Account getTestAccount() {
        Account account = new Account();
        account.setId(getTestAccountId());
        account.setCurrency(CurrencyCode.EUR);
        account.setBalance(BigDecimal.valueOf(200.20));
        account.setIban("LV23HABASAXMQ749DHCA1");
        account.setClient(getTestClient());
        return account;
    }

    private Client getTestClient() {
        Client client = new Client();
        client.setId(UUID.fromString("50b24f6f-5c42-488d-9257-0329347e6da7"));
        client.setName("John Doe");
        client.setEmail("john.doe@example.com");
        client.setUsername("johndoe");
        return client;
    }

    private AccountCreateRequest getTestAccountCreateRequest() {
        return new AccountCreateRequest(CurrencyCode.EUR);
    }


    @Test
    void createAccount_ShouldReturnAccountResponseDto() {
        AccountCreateRequest request = getTestAccountCreateRequest();
        Account account = getTestAccount();

        when(accountServiceMock.createAccountForClient(authenticationMock, request)).thenReturn(account);

        ResponseEntity<AccountResponseDto> response = accountController.createAccount(authenticationMock, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(account.getIban(), response.getBody().getIban());
        verify(accountServiceMock, times(1)).createAccountForClient(authenticationMock, request);
    }

    @Test
    void getAccountsById_ShouldReturnAccounts_WhenAuthorized() {
        UUID clientId = getTestClient().getId();
        Client client = getTestClient();
        Account account = getTestAccount();

        when(authenticationMock.getPrincipal()).thenReturn(client);
        when(accountServiceMock.getAccountsByClientId(clientId)).thenReturn(List.of(account));

        ResponseEntity<List<AccountResponseDto>> response = accountController.getAccountsById(authenticationMock, clientId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(account.getIban(), response.getBody().getFirst().getIban());
        verify(accountServiceMock, times(1)).getAccountsByClientId(clientId);
    }

    @Test
    void getAccountsById_ShouldThrowAccessDeniedException_WhenUnauthorized() {
        UUID clientId = UUID.randomUUID(); // Not the same as test client
        Client client = getTestClient();

        when(authenticationMock.getPrincipal()).thenReturn(client);

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> accountController.getAccountsById(authenticationMock, clientId)
        );
        assertEquals(UNAUTHORIZED_ACCOUNT_ERROR, exception.getMessage());
    }
}