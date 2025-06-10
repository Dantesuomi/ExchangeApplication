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

import java.math.BigDecimal;
import java.security.Principal;

@Service
public class AccountServiceImpl implements AccountService {
    private static final Logger log = LoggerFactory.getLogger(ClientServiceImpl.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ClientRepository clientRepository;

    public Account createAccountForClient(Principal principal, AccountCreateRequest request) {
        //TODO
        Client client = new Client();
        try {
            BigDecimal initialAccountBalance = new BigDecimal(0);
            Account account = new Account();
            account.setCurrency(request.getCurrency());
            account.setBalance(initialAccountBalance);
            account.setClient(client);
            log.info("Creating account for " + client.getName());
            return accountRepository.save(account);
        } catch (Exception e){
            log.warn("Request is invalid, failed to create account for " + client.getName());
            throw new IllegalArgumentException("Failed to create account");
        }
    }


}
