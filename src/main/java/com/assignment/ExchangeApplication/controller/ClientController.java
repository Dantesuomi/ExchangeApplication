package com.assignment.ExchangeApplication.controller;

import com.assignment.ExchangeApplication.exceptions.UsernameExistsException;
import com.assignment.ExchangeApplication.model.Account;
import com.assignment.ExchangeApplication.model.Client;
import com.assignment.ExchangeApplication.model.dto.ClientDto;
import com.assignment.ExchangeApplication.service.interfaces.ClientService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/client")
public class ClientController {

    private static final Logger log = LoggerFactory.getLogger(ClientController.class);

    @Autowired
    private ClientService clientService;

    @PostMapping("/register")
    public ResponseEntity<Client> registerUser(@RequestBody @Valid ClientDto clientDto) {
        try {
            Client savedClient = clientService.registerClient(clientDto);
            log.info("User registered successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(savedClient);
        } catch (UsernameExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }

    @PostMapping("/{clientId}/accounts")
    public ResponseEntity<Account> addAccount(@PathVariable UUID clientId,
                                              @RequestBody Account account) {
        Account createdAccount = clientService.addAccountToClient(clientId, account);
        return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
    }

    @GetMapping("/{clientId}/accounts")
    public ResponseEntity<List<Account>> getClientAccounts(@PathVariable UUID clientId) {
        List<Account> accounts = clientService.getAccountsByClientId(clientId);
        return ResponseEntity.ok(accounts);
    }




}
