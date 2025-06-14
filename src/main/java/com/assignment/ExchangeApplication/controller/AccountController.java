package com.assignment.ExchangeApplication.controller;

import com.assignment.ExchangeApplication.model.Account;
import com.assignment.ExchangeApplication.model.Client;
import com.assignment.ExchangeApplication.model.dto.AccountCreateRequest;
import com.assignment.ExchangeApplication.model.dto.AccountResponseDto;
import com.assignment.ExchangeApplication.service.interfaces.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/create")
    public ResponseEntity<AccountResponseDto> createAccount(Principal principal, @RequestBody AccountCreateRequest request) {
        Account account = accountService.createAccountForClient(principal, request);
        return ResponseEntity.status(HttpStatus.OK).body(new AccountResponseDto(account));
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<List<AccountResponseDto>> getAccountsById(Authentication authentication, @PathVariable UUID clientId) {
        Client client = (Client) authentication.getPrincipal();
        UUID authorizedClientId = client.getId();
        if (!authorizedClientId.equals(clientId)){
            throw new AccessDeniedException("You are not authorized to perform this action");
        }
        List<AccountResponseDto> responseDtos = accountService.getAccountsByClientId(clientId)
                .stream()
                .map(AccountResponseDto::new)
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(responseDtos);
    }
}
