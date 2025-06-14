package com.assignment.ExchangeApplication.controller;

import com.assignment.ExchangeApplication.exceptions.EmailExistsException;
import com.assignment.ExchangeApplication.exceptions.UsernameExistsException;
import com.assignment.ExchangeApplication.model.Client;
import com.assignment.ExchangeApplication.model.ErrorResponse;
import com.assignment.ExchangeApplication.model.dto.ClientDto;
import com.assignment.ExchangeApplication.service.interfaces.ClientService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client")
public class ClientController {

    private static final Logger log = LoggerFactory.getLogger(ClientController.class);

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping("/register")
    public ResponseEntity<Object> registerUser(@RequestBody @Valid ClientDto clientDto) {
        try {
            Client savedClient = clientService.registerClient(clientDto);
            log.info("User registered successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(savedClient);
        } catch (UsernameExistsException | EmailExistsException e) {
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.CONFLICT, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (IllegalArgumentException e) {
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/")
    public ResponseEntity<Client> getClientInfo(Authentication authentication) {
        Client client = (Client) authentication.getPrincipal();
        log.debug("Got client info for user ID: {}", client.getId());
        return ResponseEntity.status(HttpStatus.OK).body(client);
    }
}
