package com.assignment.ExchangeApplication.controller;

import com.assignment.ExchangeApplication.exceptions.UsernameExistsException;
import com.assignment.ExchangeApplication.model.Client;
import com.assignment.ExchangeApplication.model.dto.ClientDto;
import com.assignment.ExchangeApplication.service.interfaces.ClientService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/api/client")
public class ClientController {

    private static final Logger log = LoggerFactory.getLogger(ClientController.class);

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

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

    @GetMapping("/")
    public ResponseEntity<Optional<Client>> getClientInfo(Principal principal) {
        String userName = principal.getName();
        Optional<Client> client = clientService.getClientByUsername(userName);
        return ResponseEntity.status(HttpStatus.OK).body(client);
    }
}
