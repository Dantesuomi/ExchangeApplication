package com.assignment.ExchangeApplication.service.interfaces;

import com.assignment.ExchangeApplication.model.Account;
import com.assignment.ExchangeApplication.model.dto.AccountCreateRequest;
import org.springframework.stereotype.Service;

@Service
public interface AccountService {

    Account createAccountForCurrentClient(AccountCreateRequest request);

}
