package com.assignment.ExchangeApplication.service.interfaces;

import com.assignment.ExchangeApplication.model.Account;
import com.assignment.ExchangeApplication.model.dto.AccountCreateRequest;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
public interface AccountService {

    Account createAccountForClient(Principal principal, AccountCreateRequest request);

}
