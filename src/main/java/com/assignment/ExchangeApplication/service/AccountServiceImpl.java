package com.assignment.ExchangeApplication.service;

import com.assignment.ExchangeApplication.helpers.BankHelper;
import com.assignment.ExchangeApplication.model.Account;
import com.assignment.ExchangeApplication.model.Client;
import com.assignment.ExchangeApplication.model.dto.AccountCreateRequest;
import com.assignment.ExchangeApplication.repository.AccountRepository;
import com.assignment.ExchangeApplication.repository.ClientRepository;
import com.assignment.ExchangeApplication.service.interfaces.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static com.assignment.ExchangeApplication.helpers.StatusMessages.ACCOUNT_NOT_FOUND_ERROR;

@Service
public class AccountServiceImpl implements AccountService {
    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;

    public AccountServiceImpl(AccountRepository accountRepository,
                              ClientRepository clientRepository
    ) {
        this.accountRepository = accountRepository;
        this.clientRepository = clientRepository;
    }

    public Account createAccountForClient(Principal principal, AccountCreateRequest request) {
        //TODO find by uuid
        String username = principal.getName();
        Client client = clientRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Client not found for username: " + username));
        try {
            BigDecimal initialAccountBalance = new BigDecimal(0);
            Account account = new Account();
            account.setCurrency(request.getCurrency());
            account.setBalance(initialAccountBalance);
            account.setClient(client);
            account.setIban(BankHelper.generateIban());

            log.info("Creating account for " + client.getName());
            return accountRepository.save(account);
        } catch (Exception e){
            log.warn("Request is invalid, failed to create account for " + client.getName());
            throw new IllegalArgumentException("Failed to create account");
        }
    }

    @Override
    public List<Account> getAccountsForLoggedInClient(Principal principal) {
        String username = principal.getName();

        Client client = clientRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Client not found for username: " + username));

        return client.getAccounts();
    }

    public Optional<Account> getAccountById(UUID id){
        return accountRepository.findById(id);
    }

    @Override
    public List<Account> getAccountsByClientId(UUID clientId) {
        Client client = clientRepository.findById(clientId)
                // TODO Change to Not found exception
                .orElseThrow(() -> new IllegalArgumentException("Client not found by UUID: " + clientId));
        return client.getAccounts();
    }

    @Override
    public Account getAccountByIban(String iban){
        Account account = accountRepository.findByIban(iban);
        if (account == null){
            throw new NoSuchElementException(ACCOUNT_NOT_FOUND_ERROR);
        }
        return account;
    }

    @Override
    public void updateAccount(Account account) {
        accountRepository.save(account);
    }
}
