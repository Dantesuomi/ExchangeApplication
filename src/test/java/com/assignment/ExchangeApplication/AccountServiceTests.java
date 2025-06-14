package com.assignment.ExchangeApplication;

import com.assignment.ExchangeApplication.enums.CurrencyCode;
import com.assignment.ExchangeApplication.model.Account;
import com.assignment.ExchangeApplication.model.Client;
import com.assignment.ExchangeApplication.model.dto.AccountCreateRequest;
import com.assignment.ExchangeApplication.repository.AccountRepository;
import com.assignment.ExchangeApplication.repository.ClientRepository;
import com.assignment.ExchangeApplication.service.AccountServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static com.assignment.ExchangeApplication.helpers.StatusMessages.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTests {
    @Mock
    private AccountRepository accountRepositoryMock;

    @Mock
    private ClientRepository clientRepositoryMock;

    @Mock
    private Authentication authenticationMock;

    @InjectMocks
    private AccountServiceImpl accountService;

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
    public void testGetAccountById_Found() {
        UUID accountId = getTestAccountId();
        Account account = getTestAccount();
        when(accountRepositoryMock.findById(accountId)).thenReturn(Optional.of(account));

        Optional<Account> result = accountService.getAccountById(accountId);

        assertTrue(result.isPresent());
        assertEquals(accountId, result.get().getId());
        verify(accountRepositoryMock).findById(accountId);
    }

    @Test
    public void testGetAccountById_NotFound() {
        UUID accountId = getTestAccountId();
        when(accountRepositoryMock.findById(accountId)).thenReturn(Optional.empty());

        Optional<Account> result = accountService.getAccountById(accountId);

        assertFalse(result.isPresent());
        verify(accountRepositoryMock).findById(accountId);
    }

    @Test
    public void testCreateAccountForClient_Success() {
        Account account = getTestAccount();
        AccountCreateRequest accountCreateRequest = getTestAccountCreateRequest();
        Client client = getTestClient();
        when(authenticationMock.getPrincipal()).thenReturn(client);
        when(accountRepositoryMock.save(any(Account.class))).thenReturn(account);

        Account result = accountService.createAccountForClient(authenticationMock, accountCreateRequest);

        assertNotNull(result);
        assert(result.equals(account));
        verify(accountRepositoryMock).save(any(Account.class));
    }

    @Test
    public void testCreateAccountForClient_Exception() {
        Client client = getTestClient();
        AccountCreateRequest accountCreateRequest = getTestAccountCreateRequest();
        when(authenticationMock.getPrincipal()).thenReturn(client);
        when(accountRepositoryMock.save(any(Account.class)))
                .thenThrow(new RuntimeException("Failed to create account"));

        RuntimeException thrown = assertThrows(IllegalArgumentException.class, () ->
                accountService.createAccountForClient(authenticationMock, accountCreateRequest));
        assertEquals(CREATE_ACCOUNT_ERROR, thrown.getMessage());
    }

    @Test
    public void testGetAccountsByClientId_Success() {
        Account account = getTestAccount();
        Client client = getTestClient();
        UUID clientId = client.getId();
        List<Account> accounts = List.of(account);
        client.setAccounts(accounts);

        when(clientRepositoryMock.findById(clientId)).thenReturn(Optional.of(client));

        List<Account> result = accountService.getAccountsByClientId(clientId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(account, result.getFirst());
        verify(clientRepositoryMock).findById(clientId);
    }

    @Test
    public void testGetAccountsByClientId_ClientNotFound() {
        Client client = getTestClient();
        UUID clientId = client.getId();
        when(clientRepositoryMock.findById(clientId)).thenReturn(Optional.empty());

        NoSuchElementException thrown = assertThrows(NoSuchElementException.class, () ->
                accountService.getAccountsByClientId(clientId));
        assertEquals(CLIENT_NOT_FOUND_ERROR, thrown.getMessage());
        verify(clientRepositoryMock).findById(clientId);
    }

    @Test
    public void testGetAccountByIban_Found() {
        Account account = getTestAccount();
        String iban = account.getIban();
        when(accountRepositoryMock.findByIban(iban)).thenReturn(account);

        Account result = accountService.getAccountByIban(iban);

        assertNotNull(result);
        assertEquals(account, result);
        verify(accountRepositoryMock).findByIban(iban);
    }

    @Test
    public void testGetAccountByIban_NotFound() {
        String iban = "NONEXISTENTIBAN";
        when(accountRepositoryMock.findByIban(iban)).thenReturn(null);

        NoSuchElementException thrown = assertThrows(NoSuchElementException.class, () ->
                accountService.getAccountByIban(iban));
        assertEquals(ACCOUNT_NOT_FOUND_ERROR, thrown.getMessage());
        verify(accountRepositoryMock).findByIban(iban);
    }

    @Test
    public void testUpdateAccount_Success() {
        Account account = getTestAccount();
        when(accountRepositoryMock.save(account)).thenReturn(null);
        accountService.updateAccount(account);
        verify(accountRepositoryMock).save(account);
    }
}
