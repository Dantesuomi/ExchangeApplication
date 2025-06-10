package com.assignment.ExchangeApplication.controller;

import com.assignment.ExchangeApplication.model.Account;
import com.assignment.ExchangeApplication.model.dto.AccountCreateRequest;
import com.assignment.ExchangeApplication.service.interfaces.AccountService;
import com.assignment.ExchangeApplication.service.interfaces.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private ClientService clientService;

    @PostMapping("/create")
    public ResponseEntity<Account> createAccount(Principal principal, @RequestBody AccountCreateRequest request) {
        UUID clientUUID = clientService.getLoggedInClientUid();
        Account account = accountService.createAccountForClient(principal, request);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<List<Account>> getClientAccounts(@PathVariable UUID clientId) {
        List<Account> accounts = clientService.getAccountsByClientId(clientId);
        return ResponseEntity.ok(accounts);
    }
}
