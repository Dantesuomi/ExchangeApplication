package com.assignment.ExchangeApplication.controller;

import com.assignment.ExchangeApplication.model.Account;
import com.assignment.ExchangeApplication.model.Client;
import com.assignment.ExchangeApplication.model.dto.AccountCreateRequest;
import com.assignment.ExchangeApplication.model.dto.AccountResponseDto;
import com.assignment.ExchangeApplication.service.interfaces.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.assignment.ExchangeApplication.helpers.StatusMessages.UNAUTHORIZED_ACCOUNT_ERROR;

@RestController
@RequestMapping("api/account")
public class AccountController {
    private static final Logger log = LoggerFactory.getLogger(AccountController.class);


    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/create")
    public ResponseEntity<AccountResponseDto> createAccount(Authentication authentication, @RequestBody AccountCreateRequest request) {

        log.info("Received account creation request with currency: {}", request.getCurrency());

        Account account = accountService.createAccountForClient(authentication, request);

        log.debug("Account created with IBAN: {} ", account.getIban());

        return ResponseEntity.status(HttpStatus.OK).body(new AccountResponseDto(account));
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<List<AccountResponseDto>> getAccountsById(Authentication authentication, @PathVariable UUID clientId) {
        Client client = (Client) authentication.getPrincipal();
        UUID authorizedClientId = client.getId();
        if (!authorizedClientId.equals(clientId)){
            log.warn("Unauthorized access attempt: authenticated client {} tried to gain access to accounts related to client {}",
                    authorizedClientId, clientId);
            throw new AccessDeniedException(UNAUTHORIZED_ACCOUNT_ERROR);
        }
        List<AccountResponseDto> responseDtos = accountService.getAccountsByClientId(clientId)
                .stream()
                .map(AccountResponseDto::new)
                .collect(Collectors.toList());

        log.info("Returning accounts for client {}", clientId);

        return ResponseEntity.status(HttpStatus.OK).body(responseDtos);
    }
}
