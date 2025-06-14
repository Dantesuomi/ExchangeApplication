package com.assignment.ExchangeApplication;

import com.assignment.ExchangeApplication.enums.CurrencyCode;
import com.assignment.ExchangeApplication.enums.TransferStatus;
import com.assignment.ExchangeApplication.enums.TransferType;
import com.assignment.ExchangeApplication.exceptions.NegativeAmountException;
import com.assignment.ExchangeApplication.exceptions.PermissionDeniedException;
import com.assignment.ExchangeApplication.model.Account;
import com.assignment.ExchangeApplication.model.Client;
import com.assignment.ExchangeApplication.model.Transaction;
import com.assignment.ExchangeApplication.model.dto.AccountResponseDto;
import com.assignment.ExchangeApplication.model.dto.TransactionRequest;
import com.assignment.ExchangeApplication.model.dto.TransferRequest;
import com.assignment.ExchangeApplication.model.dto.TransferResult;
import com.assignment.ExchangeApplication.repository.TransactionRepository;
import com.assignment.ExchangeApplication.service.TransactionServiceImpl;
import com.assignment.ExchangeApplication.service.interfaces.AccountService;
import com.assignment.ExchangeApplication.service.interfaces.CurrencyExchangeService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.*;

import static com.assignment.ExchangeApplication.helpers.StatusMessages.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTests {
    @Mock
    private AccountService accountServiceMock;
    @Mock
    private CurrencyExchangeService currencyExchangeServiceMock;
    @Mock
    private TransactionRepository transactionRepositoryMock;
    @Mock
    private Authentication authenticationMock;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Client getTestClient() {
        Client client = new Client();
        client.setId(UUID.fromString("50b24f6f-5c42-488d-9257-0329347e6da7"));
        client.setName("John Doe");
        client.setEmail("john.doe@example.com");
        client.setUsername("johndoe");
        return client;
    }

    private Client getUnauthorizedClient() {
        Client client = new Client();
        client.setId(UUID.fromString("9f2adcfa-ab5a-424f-b1b1-6e3106a2104e"));
        client.setName("Jane Doe");
        client.setEmail("jane.doe@example.com");
        client.setUsername("janedoe");
        return client;
    }

    private TransactionRequest getTestTransactionRequest() {
        TransactionRequest request = new TransactionRequest();
        request.setAccountIban("LV23HABASAXMQ749DHCA1");
        request.setAmount(BigDecimal.valueOf(100.00));
        return request;
    }

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

    @Test
    void withdrawAccount_successfulWithdrawal() {
        Client client = getTestClient();
        TransactionRequest transactionRequest = getTestTransactionRequest();
        Account account = getTestAccount();
        when(authenticationMock.getPrincipal()).thenReturn(client);
        when(accountServiceMock.getAccountByIban(transactionRequest.getAccountIban())).thenReturn(account);

        AccountResponseDto response = transactionService.withdrawAccount(authenticationMock, transactionRequest);

        assertEquals(new BigDecimal("100.2"), response.getBalance());
        verify(transactionRepositoryMock, times(1)).save(any(Transaction.class));
        verify(accountServiceMock, times(1)).updateAccount(account);
    }

    @Test
    void withdrawAccount_unauthorizedUser_throwsException() {
        TransactionRequest transactionRequest = getTestTransactionRequest();
        Account account = getTestAccount();
        Client unauthorizedClient = getUnauthorizedClient();

        when(authenticationMock.getPrincipal()).thenReturn(unauthorizedClient);
        when(accountServiceMock.getAccountByIban(transactionRequest.getAccountIban())).thenReturn(account);

        assertThrows(PermissionDeniedException.class, () ->
                transactionService.withdrawAccount(authenticationMock, transactionRequest));
        verify(accountServiceMock, never()).updateAccount(any());
        verify(transactionRepositoryMock, never()).save(any());
    }

    @Test
    void withdrawAccount_insufficientBalance_throwsException() {
        Client client = getTestClient();
        Account account = getTestAccount();
        TransactionRequest transactionRequest = getTestTransactionRequest();
        TransactionRequest insufficientRequest = new TransactionRequest();
        insufficientRequest.setAccountIban("LV23HABASAXMQ749DHCA1");
        insufficientRequest.setAmount(BigDecimal.valueOf(1000.00));

        when(authenticationMock.getPrincipal()).thenReturn(client);
        when(accountServiceMock.getAccountByIban(transactionRequest.getAccountIban())).thenReturn(account);

        assertThrows(NegativeAmountException.class, () ->
                transactionService.withdrawAccount(authenticationMock, insufficientRequest));
        verify(accountServiceMock, never()).updateAccount(any());
        verify(transactionRepositoryMock, never()).save(any());
    }

    @Test
    void transferBetweenAccounts_successfulTransferSameCurrency() {
        Client client = getTestClient();
        Account account = getTestAccount();
        Account destinationAccount = new Account();
        destinationAccount.setId(UUID.fromString("679a39db-28be-4633-a69a-37d33440e1ac"));
        destinationAccount.setIban("LV18HABA4P32VIMESXWV6");
        destinationAccount.setCurrency(CurrencyCode.EUR);
        destinationAccount.setBalance(BigDecimal.valueOf(100));

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setSourceAccountNumber(account.getIban());
        transferRequest.setDestinationAccountNumber(destinationAccount.getIban());
        transferRequest.setAmount(BigDecimal.valueOf(50));
        transferRequest.setDestinationCurrency(CurrencyCode.EUR);

        when(accountServiceMock.getAccountByIban(account.getIban())).thenReturn(account);
        when(accountServiceMock.getAccountByIban(destinationAccount.getIban())).thenReturn(destinationAccount);
        when(authenticationMock.getPrincipal()).thenReturn(client);

        TransferResult result = transactionService.transferBetweenAccounts(authenticationMock, transferRequest);

        assertEquals(TransferStatus.SUCCESSFUL, result.getTransferStatus());
        assertEquals(TRANSFER_SUCCEEDED, result.getMessage());
        verify(transactionRepositoryMock, times(1)).save(any(Transaction.class));
    }

    @Test
    void transferBetweenAccounts_successfulTransferDifferentCurrency() {
        Client client = getTestClient();
        Account account = getTestAccount();
        Account destinationAccount = new Account();
        destinationAccount.setId(UUID.fromString("679a39db-28be-4633-a69a-37d33440e1ac"));
        destinationAccount.setIban("LV18HABA4P32VIMESXWV6");
        destinationAccount.setCurrency(CurrencyCode.GBP);
        destinationAccount.setBalance(BigDecimal.valueOf(100));

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setSourceAccountNumber(account.getIban());
        transferRequest.setDestinationAccountNumber(destinationAccount.getIban());
        transferRequest.setAmount(BigDecimal.valueOf(50));
        transferRequest.setDestinationCurrency(CurrencyCode.GBP);

        Map<CurrencyCode, BigDecimal> exchangeRates = new HashMap<>();
        exchangeRates.put(CurrencyCode.EUR, new BigDecimal("1.175344"));

        when(accountServiceMock.getAccountByIban(account.getIban())).thenReturn(account);
        when(accountServiceMock.getAccountByIban(destinationAccount.getIban())).thenReturn(destinationAccount);
        when(authenticationMock.getPrincipal()).thenReturn(client);
        when(currencyExchangeServiceMock.getExchangeRates(CurrencyCode.GBP)).thenReturn(exchangeRates);

        TransferResult result = transactionService.transferBetweenAccounts(authenticationMock, transferRequest);

        assertEquals(TransferStatus.SUCCESSFUL, result.getTransferStatus());
        assertEquals(TRANSFER_SUCCEEDED, result.getMessage());
        verify(transactionRepositoryMock, times(1)).save(any(Transaction.class));
    }

    @Test
    void transferBetweenAccounts_sourceAccountNotFound() {
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setSourceAccountNumber("LV83HABA397NC9TA6ERJS");
        transferRequest.setDestinationAccountNumber("LV18HABA4P32VIMESXWV6");
        transferRequest.setAmount(BigDecimal.valueOf(50));
        transferRequest.setDestinationCurrency(CurrencyCode.GBP);

        when(accountServiceMock.getAccountByIban("LV83HABA397NC9TA6ERJS")).thenReturn(null);
        TransferResult result = transactionService.transferBetweenAccounts(authenticationMock, transferRequest);
        assertEquals(TransferStatus.FAILED, result.getTransferStatus());
        assertEquals(SOURCE_ACCOUNT_NOT_FOUND_ERROR, result.getMessage());
        verify(transactionRepositoryMock, never()).save(any());
    }

    @Test
    void transferBetweenAccounts_destinationAccountNotFound() {
        Account account = getTestAccount();
        Client client = getTestClient();
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setSourceAccountNumber(account.getIban());
        transferRequest.setDestinationAccountNumber("LV18HABA4P32VIMESXWV6");
        transferRequest.setAmount(BigDecimal.valueOf(50));
        transferRequest.setDestinationCurrency(CurrencyCode.GBP);

        when(authenticationMock.getPrincipal()).thenReturn(client);
        when(accountServiceMock.getAccountByIban(account.getIban())).thenReturn(account);
        when(accountServiceMock.getAccountByIban("LV18HABA4P32VIMESXWV6")).thenReturn(null);
        TransferResult result = transactionService.transferBetweenAccounts(authenticationMock, transferRequest);
        assertEquals(TransferStatus.FAILED, result.getTransferStatus());
        assertEquals(DESTINATION_ACCOUNT_NOT_FOUND_ERROR, result.getMessage());
        verify(transactionRepositoryMock, never()).save(any());
    }

    @Test
    void transferBetweenAccounts_unauthorizedClient() {
        Account account = getTestAccount();
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setSourceAccountNumber(account.getIban());
        transferRequest.setDestinationAccountNumber("LV18HABA4P32VIMESXWV6");
        transferRequest.setAmount(BigDecimal.valueOf(50));
        transferRequest.setDestinationCurrency(CurrencyCode.GBP);
        Client unauthorizedClient = getUnauthorizedClient();

        when(authenticationMock.getPrincipal()).thenReturn(unauthorizedClient);
        when(accountServiceMock.getAccountByIban(account.getIban())).thenReturn(account);
        TransferResult result = transactionService.transferBetweenAccounts(authenticationMock, transferRequest);
        assertEquals(TransferStatus.FAILED, result.getTransferStatus());
        assertEquals(UNAUTHORIZED_ACCOUNT_ERROR, result.getMessage());
        verify(transactionRepositoryMock, never()).save(any());
    }

    @Test
    void transferBetweenAccounts_identicalAccounts() {
        Client client = getTestClient();
        Account account = getTestAccount();
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setSourceAccountNumber(account.getIban());
        transferRequest.setDestinationAccountNumber(account.getIban());
        transferRequest.setAmount(BigDecimal.valueOf(50));
        transferRequest.setDestinationCurrency(CurrencyCode.GBP);

        when(authenticationMock.getPrincipal()).thenReturn(client);
        when(accountServiceMock.getAccountByIban(account.getIban())).thenReturn(account);
        TransferResult result = transactionService.transferBetweenAccounts(authenticationMock, transferRequest);
        assertEquals(TransferStatus.FAILED, result.getTransferStatus());
        assertEquals(IDENTICAL_SOURCE_AND_DESTINATION_ACCOUNT_ERROR, result.getMessage());
        verify(transactionRepositoryMock, never()).save(any());
    }

    @Test
    void transferBetweenAccounts_currencyMismatch() {
        Client client = getTestClient();
        Account account = getTestAccount();

        Account destinationAccount = new Account();
        destinationAccount.setId(UUID.fromString("679a39db-28be-4633-a69a-37d33440e1ac"));
        destinationAccount.setIban("LV18HABA4P32VIMESXWV6");
        destinationAccount.setCurrency(CurrencyCode.GBP);
        destinationAccount.setBalance(BigDecimal.valueOf(100));

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setSourceAccountNumber(account.getIban());
        transferRequest.setDestinationAccountNumber(destinationAccount.getIban());
        transferRequest.setAmount(BigDecimal.valueOf(50));
        transferRequest.setDestinationCurrency(CurrencyCode.UAH);

        when(authenticationMock.getPrincipal()).thenReturn(client);
        when(accountServiceMock.getAccountByIban(account.getIban())).thenReturn(account);
        when(accountServiceMock.getAccountByIban(destinationAccount.getIban())).thenReturn(destinationAccount);
        TransferResult result = transactionService.transferBetweenAccounts(authenticationMock, transferRequest);
        assertEquals(TransferStatus.FAILED, result.getTransferStatus());
        assertEquals(INVALID_CURRENCY_ERROR, result.getMessage());
        verify(transactionRepositoryMock, never()).save(any());
    }

    @Test
    void transferBetweenAccounts_insufficientBalance() {
        Client client = getTestClient();
        Account account = getTestAccount();

        Account destinationAccount = new Account();
        destinationAccount.setId(UUID.fromString("679a39db-28be-4633-a69a-37d33440e1ac"));
        destinationAccount.setIban("LV18HABA4P32VIMESXWV6");
        destinationAccount.setCurrency(CurrencyCode.EUR);
        destinationAccount.setBalance(BigDecimal.valueOf(100));

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setSourceAccountNumber(account.getIban());
        transferRequest.setDestinationAccountNumber(destinationAccount.getIban());
        transferRequest.setAmount(BigDecimal.valueOf(50000));
        transferRequest.setDestinationCurrency(CurrencyCode.EUR);

        when(authenticationMock.getPrincipal()).thenReturn(client);
        when(accountServiceMock.getAccountByIban(account.getIban())).thenReturn(account);
        when(accountServiceMock.getAccountByIban(destinationAccount.getIban())).thenReturn(destinationAccount);
        TransferResult result = transactionService.transferBetweenAccounts(authenticationMock, transferRequest);
        assertEquals(TransferStatus.FAILED, result.getTransferStatus());
        assertEquals(INSUFFICIENT_BALANCE_ERROR, result.getMessage());
        verify(transactionRepositoryMock, never()).save(any());
    }

    @Test
    void getTransactionsForAccount_successfulRetrieval() {
        Client client = getTestClient();
        Account account = getTestAccount();
        UUID accountId = account.getId();
        Pageable pageable = mock(Pageable.class);
        Page<Transaction> pageMock = mock(Page.class);

        Transaction sentTransaction = new Transaction();
        sentTransaction.setSourceAccount(account);
        sentTransaction.setDestinationAccount(null);

        Transaction receivedTransaction = new Transaction();
        receivedTransaction.setSourceAccount(null);
        receivedTransaction.setDestinationAccount(account);

        List<Transaction> transactionList = List.of(sentTransaction, receivedTransaction);

        when(accountServiceMock.getAccountById(accountId)).thenReturn(Optional.of(account));
        when(authenticationMock.getPrincipal()).thenReturn(client);
        when(transactionRepositoryMock.findBySourceOrDestinationAccount(accountId, pageable)).thenReturn(pageMock);
        when(pageMock.getContent()).thenReturn(transactionList);

        Page<Transaction> result = transactionService.getTransactionsForAccount(authenticationMock, accountId, pageable);

        assertEquals(2, result.getContent().size());
        assertEquals(TransferType.SENT, result.getContent().get(0).getTransferType());
        assertEquals(TransferType.RECEIVED, result.getContent().get(1).getTransferType());
    }

    @Test
    void getTransactionsForAccount_accountNotFound_throwsException() {
        UUID accountId = getTestAccountId();
        Pageable pageable = mock(Pageable.class);

        when(accountServiceMock.getAccountById(accountId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                transactionService.getTransactionsForAccount(authenticationMock, accountId, pageable));
        verify(transactionRepositoryMock, never()).findBySourceOrDestinationAccount(any(), any());
    }

    @Test
    void getTransactionsForAccount_unauthorizedUser_throwsException() {
        Account account = getTestAccount();
        UUID accountId = account.getId();
        Pageable pageable = mock(Pageable.class);
        Client unauthorizedClient = getUnauthorizedClient();

        when(accountServiceMock.getAccountById(accountId)).thenReturn(Optional.of(account));
        when(authenticationMock.getPrincipal()).thenReturn(unauthorizedClient);

        assertThrows(PermissionDeniedException.class, () ->
                transactionService.getTransactionsForAccount(authenticationMock, accountId, pageable));
        verify(transactionRepositoryMock, never()).findBySourceOrDestinationAccount(any(), any());
    }

    @Test
    void depositAccount_successfulDeposit() {
        Client client = getTestClient();
        TransactionRequest transactionRequest = getTestTransactionRequest();
        Account account = getTestAccount();

        when(authenticationMock.getPrincipal()).thenReturn(client);
        when(accountServiceMock.getAccountByIban(transactionRequest.getAccountIban())).thenReturn(account);

        AccountResponseDto response = transactionService.depositAccount(authenticationMock, transactionRequest);

        assertEquals(new BigDecimal("300.2"), response.getBalance());
        verify(transactionRepositoryMock, times(1)).save(any(Transaction.class));
        verify(accountServiceMock, times(1)).updateAccount(account);
    }

    @Test
    void depositAccount_unauthorizedUser_throwsException() {
        TransactionRequest transactionRequest = getTestTransactionRequest();
        Account account = getTestAccount();
        Client unauthorizedClient = getUnauthorizedClient();

        when(authenticationMock.getPrincipal()).thenReturn(unauthorizedClient);
        when(accountServiceMock.getAccountByIban(transactionRequest.getAccountIban())).thenReturn(account);

        assertThrows(PermissionDeniedException.class, () ->
                transactionService.depositAccount(authenticationMock, transactionRequest));
        verify(accountServiceMock, never()).updateAccount(any());
        verify(transactionRepositoryMock, never()).save(any());
    }
}
