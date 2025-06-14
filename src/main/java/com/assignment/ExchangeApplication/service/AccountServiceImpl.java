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
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static com.assignment.ExchangeApplication.helpers.StatusMessages.*;

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

    @Override
    public Account createAccountForClient(Authentication authentication, AccountCreateRequest request) {

        Client client = (Client) authentication.getPrincipal();

        try {
            BigDecimal initialAccountBalance = BigDecimal.ZERO;
            Account account = new Account();
            account.setCurrency(request.getCurrency());
            account.setBalance(initialAccountBalance);
            account.setClient(client);
            account.setIban(BankHelper.generateIban());

            log.info("Creating account for {}", client.getName());
            return accountRepository.save(account);
        } catch (Exception e){
            log.warn("Request is invalid, failed to create account for {}", client.getName());
            throw new IllegalArgumentException(CREATE_ACCOUNT_ERROR);
        }
    }

    @Override
    public Optional<Account> getAccountById(UUID id){
        return accountRepository.findById(id);
    }

    @Override
    public List<Account> getAccountsByClientId(UUID clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new NoSuchElementException(CLIENT_NOT_FOUND_ERROR));
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
