package com.assignment.ExchangeApplication.service.interfaces;

import com.assignment.ExchangeApplication.model.Account;
import com.assignment.ExchangeApplication.model.dto.AccountCreateRequest;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public interface AccountService {

    Account createAccountForClient(Principal principal, AccountCreateRequest request);

    List<Account> getAccountsForLoggedInClient(Principal principal);

    List<Account> getAccountsByClientId(UUID clientId);

    Account getAccountByIban(String iban);

    void updateAccount(Account account);

    Optional<Account> getAccountById(UUID id);
}
