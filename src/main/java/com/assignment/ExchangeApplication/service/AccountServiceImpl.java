package com.assignment.ExchangeApplication.service;

import com.assignment.ExchangeApplication.model.Account;
import com.assignment.ExchangeApplication.model.Client;
import com.assignment.ExchangeApplication.model.dto.AccountCreateRequest;
import com.assignment.ExchangeApplication.repository.AccountRepository;
import com.assignment.ExchangeApplication.repository.ClientRepository;
import com.assignment.ExchangeApplication.service.interfaces.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl implements AccountService {
    private static final Logger log = LoggerFactory.getLogger(ClientServiceImpl.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ClientRepository clientRepository;

    public Account createAccountForCurrentClient(AccountCreateRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Client client = (Client) authentication.getPrincipal();
        try {
            Account account = new Account();
            account.setCurrency(request.getCurrency());
            account.setBalance(request.getBalance());
            account.setClient(client);
            log.info("Creating account for " + client.getName());
            return accountRepository.save(account);
        } catch (Exception e){
            log.warn("Request is invalid, failed to create account for " + client.getName());
            throw new IllegalArgumentException("Failed to create account");
        }
    }


}
